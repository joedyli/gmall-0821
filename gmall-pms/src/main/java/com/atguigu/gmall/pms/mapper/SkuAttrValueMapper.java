package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2021-01-18 14:43:54
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValueEntity> {

    List<Map<String, Object>> querySaleAttrsMappingSkuId(@Param("skuIds") List<Long> skuIds);

}
