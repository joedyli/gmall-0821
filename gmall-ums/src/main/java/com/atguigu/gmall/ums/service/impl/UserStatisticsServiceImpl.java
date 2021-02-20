package com.atguigu.gmall.ums.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserStatisticsMapper;
import com.atguigu.gmall.ums.entity.UserStatisticsEntity;
import com.atguigu.gmall.ums.service.UserStatisticsService;


@Service("userStatisticsService")
public class UserStatisticsServiceImpl extends ServiceImpl<UserStatisticsMapper, UserStatisticsEntity> implements UserStatisticsService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserStatisticsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserStatisticsEntity>()
        );

        return new PageResultVo(page);
    }

}