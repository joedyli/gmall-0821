package com.atguigu.gmall.ums.controller;

import java.util.List;

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

import com.atguigu.gmall.ums.entity.IntegrationHistoryEntity;
import com.atguigu.gmall.ums.service.IntegrationHistoryService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 购物积分记录表
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2021-02-20 14:16:37
 */
@Api(tags = "购物积分记录表 管理")
@RestController
@RequestMapping("ums/integrationhistory")
public class IntegrationHistoryController {

    @Autowired
    private IntegrationHistoryService integrationHistoryService;

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryIntegrationHistoryByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = integrationHistoryService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<IntegrationHistoryEntity> queryIntegrationHistoryById(@PathVariable("id") Long id){
		IntegrationHistoryEntity integrationHistory = integrationHistoryService.getById(id);

        return ResponseVo.ok(integrationHistory);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody IntegrationHistoryEntity integrationHistory){
		integrationHistoryService.save(integrationHistory);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody IntegrationHistoryEntity integrationHistory){
		integrationHistoryService.updateById(integrationHistory);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		integrationHistoryService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
