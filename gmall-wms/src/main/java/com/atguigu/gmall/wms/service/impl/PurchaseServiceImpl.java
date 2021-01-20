package com.atguigu.gmall.wms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.PurchaseMapper;
import com.atguigu.gmall.wms.entity.PurchaseEntity;
import com.atguigu.gmall.wms.service.PurchaseService;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseMapper, PurchaseEntity> implements PurchaseService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<PurchaseEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageResultVo(page);
    }

}