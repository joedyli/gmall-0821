package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    @PostMapping("pms/spu/json")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    @GetMapping("pms/spudesc/{spuId}")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/sku/{id}")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    @GetMapping("pms/skuimages/sku/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId")Long skuId);

    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("pms/category/all/{cid}")
    public ResponseVo<List<CategoryEntity>> query123CategoriesByCid3(@PathVariable("cid")Long cid);

    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategoriesByPid(@PathVariable("parentId")Long pid);

    @GetMapping("pms/category/parent/withsubs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLvl2CatesWithSubsByPid(@PathVariable("pid")Long pid);

    @GetMapping("pms/skuattrvalue/search/{cid}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValuesByCidAndSkuId(
            @PathVariable("cid")Long cid,
            @RequestParam("skuId")Long skuId
    );

    @GetMapping("pms/skuattrvalue/spu/mapping/{spuId}")
    public ResponseVo<String> querySaleAttrsMappingSkuIdBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrsBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySaleAttrValuesBySkuId(@PathVariable("skuId")Long skuId);

    @GetMapping("pms/spuattrvalue/search/{cid}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValuesByCidAndSpuId(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId
    );

    @GetMapping("pms/attrgroup/category/spuId/skuId/{cid}")
    public ResponseVo<List<ItemGroupVo>> queryGroupWithAttrValuesBy(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId,
            @RequestParam("skuId")Long skuId
    );
}
