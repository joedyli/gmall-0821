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

import com.atguigu.gmall.ums.entity.UserStatisticsEntity;
import com.atguigu.gmall.ums.service.UserStatisticsService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 统计信息表
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2021-02-20 14:16:37
 */
@Api(tags = "统计信息表 管理")
@RestController
@RequestMapping("ums/userstatistics")
public class UserStatisticsController {

    @Autowired
    private UserStatisticsService userStatisticsService;

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryUserStatisticsByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = userStatisticsService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserStatisticsEntity> queryUserStatisticsById(@PathVariable("id") Long id){
		UserStatisticsEntity userStatistics = userStatisticsService.getById(id);

        return ResponseVo.ok(userStatistics);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody UserStatisticsEntity userStatistics){
		userStatisticsService.save(userStatistics);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody UserStatisticsEntity userStatistics){
		userStatisticsService.updateById(userStatistics);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		userStatisticsService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
