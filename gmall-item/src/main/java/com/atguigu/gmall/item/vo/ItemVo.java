package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemVo {

    //  面包屑：分类 V
    private List<CategoryEntity> categories;

    // 面包屑：品牌 V
    private Long brandId;
    private String brandName;

    // 面包屑：spu V
    private Long spuId;
    private String spuName;

    // 中间的sku信息 V
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private String defaultImage;
    private Integer weight;

    // 图片列表 V
    private List<SkuImagesEntity> images;

    // 营销信息 V
    private List<ItemSaleVo> sales;

    // 库存信息 V
    private Boolean store = false;

    // [{attrId:4, attrName: '颜色', attrValues: ['暗夜黑', '白天白']},
    // {attrId:5, attrName: '内存', attrValues: ['8G', '12G']},
    // {attrId:6, attrName: '存储', attrValues: ['128G', '256G']}]
    // 跟当前sku相同的spu下的所有sku的销售属性列表 V
    private List<SaleAttrValueVo> saleAttrs;

    // {4: '暗夜黑', 5: '8G', 6: '128G'}
    // 当前sku的销售参数 V
    private Map<Long, String> saleAttr;

    // 销售属性组合和skuId的映射关系
    // {'暗夜黑,8G,128G': 10, '白天白,12G,128G': 11} V
    private String skuJsons;

    // 商品海报信息 V
    private List<String> spuImages;

    // 规格参数分组列表 V
    private List<ItemGroupVo> groups;
}
