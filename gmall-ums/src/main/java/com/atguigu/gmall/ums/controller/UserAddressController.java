package com.atguigu.gmall.ums.controller;

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

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.service.UserAddressService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 收货地址表
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2021-02-20 14:16:37
 */
@Api(tags = "收货地址表 管理")
@RestController
@RequestMapping("ums/useraddress")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    @GetMapping("user/{userId}")
    public ResponseVo<List<UserAddressEntity>> queryAddressesByUserId(@PathVariable("userId")Long userId){
        List<UserAddressEntity> addressEntities = this.userAddressService.list(new QueryWrapper<UserAddressEntity>().eq("user_id", userId));
        return ResponseVo.ok(addressEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryUserAddressByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = userAddressService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserAddressEntity> queryUserAddressById(@PathVariable("id") Long id){
		UserAddressEntity userAddress = userAddressService.getById(id);

        return ResponseVo.ok(userAddress);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody UserAddressEntity userAddress){
		userAddressService.save(userAddress);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody UserAddressEntity userAddress){
		userAddressService.updateById(userAddress);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		userAddressService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
