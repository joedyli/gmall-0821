package com.atguigu.gmall.sms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.HomeAdvMapper;
import com.atguigu.gmall.sms.entity.HomeAdvEntity;
import com.atguigu.gmall.sms.service.HomeAdvService;


@Service("homeAdvService")
public class HomeAdvServiceImpl extends ServiceImpl<HomeAdvMapper, HomeAdvEntity> implements HomeAdvService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<HomeAdvEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<HomeAdvEntity>()
        );

        return new PageResultVo(page);
    }

}