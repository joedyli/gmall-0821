package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 强制约定：所有异步方法的第一个参数必须是userId、userKey
 */
@Service
public class CartAsyncService {

    @Autowired
    private CartMapper cartMapper;

    @Async
    public void updateCart(String userId, String skuId, Cart cart){
        this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }

    @Async
    public void insertCart(String userId, Cart cart){
        int i = 1/0;
        this.cartMapper.insert(cart);
    }

    @Async
    public void deleteCart(String userId){
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId));
    }

    @Async
    public void deleteCartBySkuId(String userId, Long skuId) {
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }
}
