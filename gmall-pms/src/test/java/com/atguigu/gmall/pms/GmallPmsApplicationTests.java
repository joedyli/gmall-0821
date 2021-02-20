package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
class GmallPmsApplicationTests {

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuAttrValueService attrValueService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Test
    void contextLoads() {
        System.out.println(this.skuAttrValueService.querySaleAttrsBySpuId(20l));
    }

}
