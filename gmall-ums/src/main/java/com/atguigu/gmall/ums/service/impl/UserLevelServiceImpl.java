package com.atguigu.gmall.ums.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserLevelMapper;
import com.atguigu.gmall.ums.entity.UserLevelEntity;
import com.atguigu.gmall.ums.service.UserLevelService;


@Service("userLevelService")
public class UserLevelServiceImpl extends ServiceImpl<UserLevelMapper, UserLevelEntity> implements UserLevelService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserLevelEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserLevelEntity>()
        );

        return new PageResultVo(page);
    }

}