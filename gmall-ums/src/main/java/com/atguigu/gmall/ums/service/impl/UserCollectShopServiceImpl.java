package com.atguigu.gmall.ums.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserCollectShopMapper;
import com.atguigu.gmall.ums.entity.UserCollectShopEntity;
import com.atguigu.gmall.ums.service.UserCollectShopService;


@Service("userCollectShopService")
public class UserCollectShopServiceImpl extends ServiceImpl<UserCollectShopMapper, UserCollectShopEntity> implements UserCollectShopService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserCollectShopEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserCollectShopEntity>()
        );

        return new PageResultVo(page);
    }

}