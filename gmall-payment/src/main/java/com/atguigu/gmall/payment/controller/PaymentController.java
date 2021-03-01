package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.config.AlipayTemplate;
import com.atguigu.gmall.payment.interceptor.LoginInterceptor;
import com.atguigu.gmall.payment.pojo.PayAsyncVo;
import com.atguigu.gmall.payment.pojo.PayVo;
import com.atguigu.gmall.payment.pojo.PaymentInfoEntity;
import com.atguigu.gmall.payment.pojo.UserInfo;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UnknownFormatConversionException;

@Controller
public class PaymentController {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("pay.html")
    public String toPay(@RequestParam("orderToken")String orderToken, Model model){
        OrderEntity orderEntity = this.paymentService.queryOrderByToken(orderToken);
        if (orderEntity == null) {
            throw new OrderException("要支付的订单不存在。");
        }
        // 判断订单是否属于该用户
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != orderEntity.getUserId()){
            throw new OrderException("该订单不属于您，或者您没有支付权限");
        }
        // 判断订单是否属于待付款状态
        if (orderEntity.getStatus() != 0){
            throw new OrderException("该订单无法支付，请注意您的订单状态");
        }
        model.addAttribute("orderEntity", orderEntity);
        return "pay";
    }

    @GetMapping("alipay.html")
    @ResponseBody
    public String alipay(@RequestParam("orderToken") String orderToken) throws AlipayApiException {
        OrderEntity orderEntity = this.paymentService.queryOrderByToken(orderToken);
        if (orderEntity == null) {
            throw new OrderException("要支付的订单不存在。");
        }
        // 判断订单是否属于该用户
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != orderEntity.getUserId()){
            throw new OrderException("该订单不属于您，或者您没有支付权限");
        }
        // 判断订单是否属于待付款状态
        if (orderEntity.getStatus() != 0){
            throw new OrderException("该订单无法支付，请注意您的订单状态");
        }

        // 调用阿里的支付接口，跳转到支付页面
        PayVo payVo = new PayVo();
        payVo.setOut_trade_no(orderToken);
        // 注意：一定不要使用实际价格，建议直接使用0.01
        payVo.setTotal_amount("0.01");
        payVo.setSubject("谷粒商城订单支付平台");
        // 生成对账记录
        String payId = this.paymentService.savePayment(orderEntity);
        payVo.setPassback_params(payId);
        String form = this.alipayTemplate.pay(payVo);

        return form;
    }

    @GetMapping("pay/success")
    public String paysuccess(){

        return "paysuccess";
    }

    @PostMapping("pay/ok")
    @ResponseBody
    public Object payOk(PayAsyncVo payAsyncVo){
        System.out.println(payAsyncVo);
        // 1.验签
        Boolean flag = this.alipayTemplate.checkSignature(payAsyncVo);
        if (!flag){
            return "failure";
        }

        // 2.校验业务参数：app_id、out_trade_no、total_amount
        String app_id = payAsyncVo.getApp_id();
        String out_trade_no = payAsyncVo.getOut_trade_no();
        String total_amount = payAsyncVo.getTotal_amount();
        String payId = payAsyncVo.getPassback_params(); // 对账记录的id
        PaymentInfoEntity paymentInfoEntity = this.paymentService.queryPaymentById(payId);
        if (!StringUtils.equals(app_id, this.alipayTemplate.getApp_id())
                || new BigDecimal(total_amount).compareTo(paymentInfoEntity.getTotalAmount())  != 0
                || !StringUtils.equals(out_trade_no, paymentInfoEntity.getOutTradeNo())
        ) {
            return "failure";
        }

        // 3.校验支付状态
        String trade_status = payAsyncVo.getTrade_status();
        if (!StringUtils.equals("TRADE_SUCCESS", trade_status)){
            return "failure";
        }

        // 4.更新支付对账表中状态
        if (this.paymentService.updatePaymentInfo(payAsyncVo, payId) == 0) {
            return "failure";
        }

        // 5.发送消息给订单（oms 发送消息给wms 减库存）
        this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.success", out_trade_no);

        // 6.响应信息给支付宝
        return "success";
    }

    @GetMapping("seckill/{skuId}")
    public ResponseVo<Object> seckill(@PathVariable("skuId")Long skuId){

        RLock fairLock = this.redissonClient.getFairLock("seckill:lock:" + skuId);
        fairLock.lock();

        // 判断库存是否充足
        String stockString = this.redisTemplate.opsForValue().get("seckill:stock:" + skuId);
        if (StringUtils.isBlank(stockString) || Integer.parseInt(stockString) == 0){
            throw new OrderException("秒杀不存在或者秒杀已结束！");
        }

        // 减库存
        this.redisTemplate.opsForValue().decrement("seckill:stock:" + skuId);

        // 发送消息异步创建订单，并减库存
        Map<String, Object> msg = new HashMap<>();
        msg.put("skuId", skuId);
        msg.put("count", 1);
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        msg.put("userId",  userInfo.getUserId());
        String orderToken = IdWorker.getTimeId();
        msg.put("orderToken", orderToken);
        this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "seckill.success", msg);

        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("seckill:countdown:" + orderToken);
        countDownLatch.trySetCount(1);

        fairLock.unlock();
        return ResponseVo.ok("秒杀成功！" + orderToken);
    }

    @GetMapping("order/{orderToken}")
    public ResponseVo<OrderEntity> queryOrderByToken(@PathVariable("orderToken")String orderToken) throws InterruptedException {

        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("seckill:countdown:" + orderToken);
        countDownLatch.await();

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        OrderEntity orderEntity = this.paymentService.queryOrderByToken(orderToken);
        if (orderEntity.getUserId() == userInfo.getUserId()){
            return ResponseVo.ok(orderEntity);
        }
        return ResponseVo.ok();
    }
}
