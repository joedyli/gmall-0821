package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private WareSkuMapper wareSkuMapper;

    private static final String LOCK_PREFIX = "stock:lock:";
    private static final String KEY_PREFIX = "stock:info:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVos, String orderToken) {

        if (CollectionUtils.isEmpty(lockVos)){
            throw new OrderException("您没有要购买的商品！");
        }

        // 遍历所有商品，验库存并锁库存，要具备原子性
        lockVos.forEach(lockVo -> {
            checkLock(lockVo);
        });

        // 只要有一个商品锁定失败，所有锁定成功的商品要解锁库存
        if (lockVos.stream().anyMatch(lockVo -> !lockVo.getLock())) {
            // 获取所有锁定成功的商品，遍历解锁库存
            lockVos.stream().filter(SkuLockVo::getLock).forEach(lockVo -> {
                this.wareSkuMapper.unlock(lockVo.getWareSkuId(), lockVo.getCount());
            });

            // 响应锁定状态
            return lockVos;
        }

        // 如果所有商品都锁定成功的情况下，需要缓存锁定信息到redis。以方便将来解锁库存 或者 减库存
        // 以orderToken作为key，以lockVos锁定信息作为value
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, JSON.toJSONString(lockVos));

        return null;
    }

    private void checkLock(SkuLockVo lockVo){
        RLock fairLock = this.redissonClient.getFairLock(LOCK_PREFIX + lockVo.getSkuId());
        fairLock.lock();

        try {
            // 验库存：查询，返回的是满足要求的库存列表
            List<WareSkuEntity> wareSkuEntities = this.wareSkuMapper.check(lockVo.getSkuId(), lockVo.getCount());
            // 如果没有一个仓库满足要求，这里就验库存失败
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                lockVo.setLock(false);
                return;
            }
            // 大数据分析，获取就近的仓库。这里我们就取第一个仓库
            WareSkuEntity wareSkuEntity = wareSkuEntities.get(0);

            // 锁库存：更新
            Long wareSkuId = wareSkuEntity.getId();
            if (this.wareSkuMapper.lock(wareSkuId, lockVo.getCount()) == 1) {
                lockVo.setLock(true);
                lockVo.setWareSkuId(wareSkuId);// 如果库存锁定成功，要记录锁定成功的仓库的id。以方便将来解锁库存
            }
        } finally {
            fairLock.unlock();
        }
    }
}
