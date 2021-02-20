package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * sku图片
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2021-01-18 14:43:54
 */
@Api(tags = "sku图片 管理")
@RestController
@RequestMapping("pms/skuimages")
public class SkuImagesController {

    @Autowired
    private SkuImagesService skuImagesService;

    @GetMapping("sku/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId")Long skuId){
        List<SkuImagesEntity> skuImagesEntities = this.skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
        return ResponseVo.ok(skuImagesEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuImagesByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = skuImagesService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuImagesEntity> querySkuImagesById(@PathVariable("id") Long id){
		SkuImagesEntity skuImages = skuImagesService.getById(id);

        return ResponseVo.ok(skuImages);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuImagesEntity skuImages){
		skuImagesService.save(skuImages);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuImagesEntity skuImages){
		skuImagesService.updateById(skuImages);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		skuImagesService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
