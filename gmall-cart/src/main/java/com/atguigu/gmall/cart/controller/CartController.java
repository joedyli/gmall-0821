package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public String saveCart(Cart cart){
        this.cartService.saveCart(cart);

        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();
    }

    @GetMapping("addCart.html")
    public String toCart(@RequestParam("skuId")Long skuId, Model model){
        Cart cart = this.cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart", cart);
        return "addCart";
    }

    @GetMapping("cart.html")
    public String queryCarts(Model model){
        List<Cart> carts = this.cartService.queryCarts();
        model.addAttribute("carts", carts);
        return "cart";
    }

    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateNum(@RequestBody Cart cart){
        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam("skuId")Long skuId){
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

    @RequestMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request){
        long now = System.currentTimeMillis();
        this.cartService.executor1();
        this.cartService.executor2();
//        future1.addCallback(result -> {
//            System.out.println(result);
//        }, ex -> {
//            System.out.println(ex.getMessage());
//        });
//        future2.addCallback(result -> {
//            System.out.println(result);
//        }, ex -> {
//            System.out.println(ex.getMessage());
//        });
//        try {
//            //System.out.println(future1.get());
//            System.out.println(future2.get());
//        } catch (Exception e) {
//            System.out.println(e.getMessage() + "==========================");
//        }
        System.out.println("controller方法的执行时间：" + (System.currentTimeMillis() - now));
        return "hello Test";
    }
}
