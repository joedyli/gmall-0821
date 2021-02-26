package com.atguigu.gmall.order.pojo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVo {

    // 收件人地址列表
    private List<UserAddressEntity> addresses;

    // 送货清单
    private List<OrderItemVo> orderItems;

    // 购买积分
    private Integer bounds;

    private String orderToken; // 为了防止重复提交
}
