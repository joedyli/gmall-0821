package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    private static final String KEY_PREFIX = "index:cates:";

    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> categoryResponseVo = this.pmsClient.queryCategoriesByPid(0l);
        return categoryResponseVo.getData();
    }

    public List<CategoryEntity> queryLvl2CategoriesWithSubsByPid(Long pid) {
        // 先查询缓存，缓存中有，直接返回
        String json = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json)){
            return JSON.parseArray(json, CategoryEntity.class);
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
            if (StringUtils.equals(uuid, this.redisTemplate.opsForValue().get("lock"))){
                this.redisTemplate.delete("lock");
            }
        }
    }
}
