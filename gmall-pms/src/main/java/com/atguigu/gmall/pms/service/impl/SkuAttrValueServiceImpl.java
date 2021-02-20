package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuAttrValueMapper attrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchAttrValuesByCidAndSkuId(Long cid, Long skuId) {
        // 根据cid查询出检索类型的规格参数
        List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("category_id", cid).eq("search_type", 1));

        if (CollectionUtils.isEmpty(attrEntities)){
            return null;
        }
        // 获取检索类型的规格参数的id集合
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

        // 查询出销售类型的检索规格参数
        return this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).in("attr_id", attrIds));
    }

    @Override
    public List<SaleAttrValueVo> querySaleAttrsBySpuId(Long spuId) {
        // 查询出spu下所有的sku
        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntities)){
            return null;
        }
        // 搜集所有的skuId
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        // 查询sku对应的销售属性
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds).orderByAsc("attr_id"));
        if (CollectionUtils.isEmpty(skuAttrValueEntities)){
            return null;
        }

        // 以attrId分组：attrId-key List<SkuAttrValueEntity>-value
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(t -> t.getAttrId()));

        // 把map中的每个元素专户成SaleAttrValueVo
        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        map.forEach((attrId, attrValueEntities) -> {
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            saleAttrValueVo.setAttrName(attrValueEntities.get(0).getAttrName());
            Set<String> set = attrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet());
            saleAttrValueVo.setAttrValues(set);
            saleAttrValueVos.add(saleAttrValueVo);
        });
        return saleAttrValueVos;
    }

    @Override
    public String querySaleAttrsMappingSkuIdBySpuId(Long spuId) {
        // 查询出spu下所有的sku
        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntities)){
            return null;
        }
        // 搜集所有的skuId
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        // [{sku_id=27, attr_values=金色,12G,256G}, {sku_id=28, attr_values=金色,12G,512G}, {sku_id=29, attr_values=白色,12G,256G}, {sku_id=30, attr_values=白色,12G,512G}]
        // {'暗夜黑,8G,128G': 10, '白天白,12G,128G': 11}
        List<Map<String, Object>> maps = this.attrValueMapper.querySaleAttrsMappingSkuId(skuIds);
        Map<String, Long> mappingMap = maps.stream().collect(Collectors.toMap(map -> map.get("attr_values").toString(), map -> (Long) map.get("sku_id")));

        return JSON.toJSONString(mappingMap);
    }


}
