package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.utils.DistributedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DistributedLock distributedLock;

    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "index:cates:";

    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> categoryResponseVo = this.pmsClient.queryCategoriesByPid(0l);
        return categoryResponseVo.getData();
    }

    @GmallCache(prefix = KEY_PREFIX, timeout = 43200, random = 7200, lock = "index:cates:lock:")
    public List<CategoryEntity> queryLvl2CategoriesWithSubsByPid(Long pid) {
        ResponseVo<List<CategoryEntity>> responseVo = this.pmsClient.queryLvl2CatesWithSubsByPid(pid);
        return responseVo.getData();
    }

    public List<CategoryEntity> queryLvl2CategoriesWithSubsByPid2(Long pid) {

        // 先查询缓存，缓存中有，直接返回
        String json = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json)){
            return JSON.parseArray(json, CategoryEntity.class);
        }

        // 为了防止缓存击穿，添加分布式锁
        RLock lock = this.redissonClient.getLock("index:cates:lock:" + pid);
        lock.lock();

        // 再次查询缓存，因为在请求等待获取锁的过程中，可能有其他请求已把数据放入缓存
        String json2 = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json2)){
            return JSON.parseArray(json2, CategoryEntity.class);
        }

        // 再去查询数据库，并放入缓存
        ResponseVo<List<CategoryEntity>> responseVo = this.pmsClient.queryLvl2CatesWithSubsByPid(pid);
        List<CategoryEntity> categoryEntities = responseVo.getData();

        // 为了防止缓存穿透，数据即使为null也缓存（布隆过滤器）
        if (CollectionUtils.isEmpty(categoryEntities)){
            this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 5, TimeUnit.MINUTES);
        } else {
            // 为了防止缓存雪崩，给缓存时间添加随机值
            this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 30 + new Random(10).nextInt(), TimeUnit.DAYS);
        }

        return categoryEntities;
    }

    public void testLock() {
        // 加锁
        RLock lock = this.redissonClient.getLock("lock");
        lock.lock(10, TimeUnit.SECONDS);

        try {
            // 获取到锁执行业务逻辑
            String number = this.redisTemplate.opsForValue().get("number");
            if (number == null){
                return;
            }
            int num = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number", String.valueOf(++num));

//            try {
//                TimeUnit.SECONDS.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        } finally {
            // 释放锁
            //lock.unlock();
        }
    }

    public void testLock3() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = this.distributedLock.tryLock("lock", uuid, 30);
        if (lock){
            // 获取到锁执行业务逻辑
            String number = this.redisTemplate.opsForValue().get("number");
            if (number == null){
                return;
            }
            int num = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number", String.valueOf(++num));

            try {
                TimeUnit.SECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //this.testSubLock(uuid);

            this.distributedLock.unlock("lock", uuid);
        }
    }

    public void testSubLock(String uuid){
        this.distributedLock.tryLock("lock", uuid, 30);
        System.out.println("测试可重入锁");
        this.distributedLock.unlock("lock", uuid);
    }

    public void testLock2() {
        // 为了防误删，给锁添加唯一标识
        String uuid = UUID.randomUUID().toString();
        // 加锁
        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        if (!lock){
            try {
                // 如果获取锁失败，睡一会儿再去尝试获取锁
                Thread.sleep(50);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            // 为了防止死锁的发生，给锁设置过期时间
            //this.redisTemplate.expire("lock", 3, TimeUnit.SECONDS);
            // 获取到锁执行业务逻辑
            String number = this.redisTemplate.opsForValue().get("number");
            if (number == null){
                return;
            }
            int num = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number", String.valueOf(++num));

            //释放锁
            // 为了防止误删，删除时需要判断是否自己的锁
            String script = "if(redis.call('get', KEYS[1]) == ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";
            // 预加载：springdata-redis会自动的预加载
            // DefaultRedisScript：要使用两个参数的构造方法来初始化
            this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);
//            if (StringUtils.equals(uuid, this.redisTemplate.opsForValue().get("lock"))){
//                this.redisTemplate.delete("lock");
//            }
        }
    }

    public void testRead() {
        RReadWriteLock rwLock = this.redissonClient.getReadWriteLock("rwLock");
        rwLock.readLock().lock(10, TimeUnit.SECONDS);

        System.out.println("读的业务操作");
    }

    public void testWrite() {
        // 两个方法的锁名称要一致
        RReadWriteLock rwLock = this.redissonClient.getReadWriteLock("rwLock");
        rwLock.writeLock().lock(10, TimeUnit.SECONDS);

        System.out.println("写的业务操作");
    }

    public void testLatch() {
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");
        latch.trySetCount(6);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // TODO: 后续操作只能等待
    }

    public void testCountDown() {
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");
        // TODO: 业务
        latch.countDown();
    }

}
