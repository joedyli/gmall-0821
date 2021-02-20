package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址表
 * 
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2021-02-20 14:16:37
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddressEntity> {
	
}
