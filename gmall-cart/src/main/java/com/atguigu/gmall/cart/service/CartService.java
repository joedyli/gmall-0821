package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartAsyncService asyncService;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

//    @Autowired
//    private ThreadPoolExecutor threadPoolExecutor;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    public void saveCart(Cart cart) {
        // 1.获取用户的登录信息
        String userId = getUserId();

        // 获取当前用户的购物车，内存map
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        // 判断该用户的购物车是否包含该商品
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        if (hashOps.hasKey(skuId)) {
            // 包含：更新数量
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));
            // 用更新后的购物车对象覆盖掉redis中的对象
            this.asyncService.updateCart(userId, skuId, cart);
            //this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
        } else {
            // 不包含：新增一条记录
            cart.setUserId(userId);
            cart.setCheck(true);

            // 查询sku相关信息
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null){
                return ;
            }
            cart.setTitle(skuEntity.getTitle());
            cart.setPrice(skuEntity.getPrice());
            cart.setDefaultImage(skuEntity.getDefaultImage());

            // 查询库存
            ResponseVo<List<WareSkuEntity>> listResponseVo = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = listResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)){
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            // 销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = this.pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));
            // 查询营销信息
            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));

            //hashOps.put(skuId, JSON.toJSONString(cart));
            this.asyncService.insertCart(userId, cart);
            // 加入购物车是加入价格缓存
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());
        }
        hashOps.put(skuId, JSON.toJSONString(cart));
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() == null) {
            return userInfo.getUserKey();
        } else {
            return userInfo.getUserId().toString();
        }
    }

    public Cart queryCartBySkuId(Long skuId) {

        String userId = this.getUserId();

        // 根据外层key（userId、userKey）获取内层map
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = hashOps.get(skuId.toString()).toString();

            return JSON.parseObject(cartJson, Cart.class);
        }
        throw new CartException("该用户的购物车不包含该记录。。。");
    }

    @Async
    public void executor1() {
        try {
            System.out.println("executor1方法开始执行。。。。。。");
            TimeUnit.SECONDS.sleep(4);
            int i = 1/0;
            System.out.println("executor1方法结束执行==========");
            //return AsyncResult.forValue("hello executor1");
        } catch (InterruptedException e) {
            e.printStackTrace();
            //return AsyncResult.forExecutionException(e);
        }
    }

    @Async
    public void executor2() {
        try {
            System.out.println("executor2方法开始执行。。。。。。");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("executor2方法结束执行==========");
            //return AsyncResult.forValue("hello executor2");
        } catch (Exception e) {
            e.printStackTrace();
            //return AsyncResult.forExecutionException(e);
        }
    }

    public List<Cart> queryCarts() {

        //1.获取userKey
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        // 组装key
        String unloginKey = KEY_PREFIX + userKey;
        //2.根据userKey查询未登录的购物车
        // 获取未登录购物车的内层的map
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(unloginKey);
        // 获取未登录用户购物车的所有记录List<String>
        List<Object> unloginCartJsons = hashOps.values();
        List<Cart> unloginCarts  = null;
        if (!CollectionUtils.isEmpty(unloginCartJsons)){
            unloginCarts  = unloginCartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        //3.获取userId
        Long userId = userInfo.getUserId();
        //4.如果userId为空，直接返回
        if (userId == null) {
            return unloginCarts;
        }

        //5.把未登录的购物车合并到登录状态的购物车
        String loginKey = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unloginCarts)){
            unloginCarts.forEach(cart -> { // 未登录状态的购物车记录
                String skuId = cart.getSkuId().toString();
                BigDecimal count = cart.getCount();
                if (loginHashOps.hasKey(skuId)){
                    // 用户的购物车包含了该记录，合并数量
                    String cartJson = loginHashOps.get(skuId).toString(); // 获取登录购物车的对应记录
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    // 写入redis 异步写入mysql
                    this.asyncService.updateCart(userId.toString(), skuId, cart);
                    //this.cartMapper.update(cart, new QueryWrapper<Cart>().eq("user_id", userId.toString()).eq("sku_id", skuId));
                } else {
                    // 用户的购物车不包含该记录，新增记录
                    cart.setUserId(userId.toString());
                    this.asyncService.insertCart(userId.toString(), cart);
                }
                loginHashOps.put(skuId, JSON.toJSONString(cart));
            });
        }

        //6.把未登录的购物车删除
        this.redisTemplate.delete(unloginKey);
        this.asyncService.deleteCart(userKey);

        //7.返回登录状态的购物车
        List<Object> cartJsons = loginHashOps.values();
        if (!CollectionUtils.isEmpty(cartJsons)){
             return cartJsons.stream().map(cartJson -> {
                 Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                 cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                 return cart;
             }).collect(Collectors.toList());
        }
        return null;
    }

    public void updateNum(Cart cart) {

        String userId = this.getUserId();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (!hashOps.hasKey(cart.getSkuId().toString())){
            throw new CartException("该用户对应的购物车数据不存在");
        }
        BigDecimal count = cart.getCount();
        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
        cart = JSON.parseObject(cartJson, Cart.class);
        cart.setCount(count);

        hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        this.asyncService.updateCart(userId, cart.getSkuId().toString(), cart);
    }

    public void deleteCart(Long skuId) {

        String userId = this.getUserId();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (!hashOps.hasKey(skuId.toString())){
            throw new CartException("该用户对应的购物车数据不存在");
        }
        hashOps.delete(skuId.toString());
        this.asyncService.deleteCartBySkuId(userId, skuId);
    }
}
