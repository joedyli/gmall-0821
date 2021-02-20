package com.atguigu.gmall.ums.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.GrowthHistoryMapper;
import com.atguigu.gmall.ums.entity.GrowthHistoryEntity;
import com.atguigu.gmall.ums.service.GrowthHistoryService;


@Service("growthHistoryService")
public class GrowthHistoryServiceImpl extends ServiceImpl<GrowthHistoryMapper, GrowthHistoryEntity> implements GrowthHistoryService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<GrowthHistoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<GrowthHistoryEntity>()
        );

        return new PageResultVo(page);
    }

}