package com.atguigu.gmall.oms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.oms.mapper.RefundInfoMapper;
import com.atguigu.gmall.oms.entity.RefundInfoEntity;
import com.atguigu.gmall.oms.service.RefundInfoService;


@Service("refundInfoService")
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfoEntity> implements RefundInfoService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<RefundInfoEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<RefundInfoEntity>()
        );

        return new PageResultVo(page);
    }

}