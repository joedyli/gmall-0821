package com.atguigu.gmall.sms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.MemberPriceMapper;
import com.atguigu.gmall.sms.entity.MemberPriceEntity;
import com.atguigu.gmall.sms.service.MemberPriceService;


@Service("memberPriceService")
public class MemberPriceServiceImpl extends ServiceImpl<MemberPriceMapper, MemberPriceEntity> implements MemberPriceService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<MemberPriceEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<MemberPriceEntity>()
        );

        return new PageResultVo(page);
    }

}