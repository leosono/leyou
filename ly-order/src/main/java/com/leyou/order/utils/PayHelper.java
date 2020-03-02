package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.WXPayConfig;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;

    @Autowired
    private WXPayConfig config;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    //校验签名
    public void verifySign(Map<String, String> result) {
        try{
            //重新生成签名
            String sign1 = WXPayUtil.generateSignature(result, config.getKey(), WXPayConstants.SignType.HMACSHA256);
            String sign2 = WXPayUtil.generateSignature(result, config.getKey(), WXPayConstants.SignType.MD5);
            //将重新生成的签名和获取到的作比较
            String sign = result.get("sign");
            if(!StringUtils.equals(sign,sign1) && !StringUtils.equals(sign, sign2)){
                log.error("[微信支付] 签名校验失败");
                throw new LyException(ExceptionEnum.PAY_SIGN_ERROR);
            }
        }catch(Exception e){
            log.error("[微信支付] 签名校验失败");
            throw new LyException(ExceptionEnum.PAY_SIGN_ERROR);
        }
    }

    //返回状态校验
    public void verifyCallback(Map<String,String> result){
        if(!result.get("return_code").equals(WXPayConstants.SUCCESS)){
            log.error("[订单微服务] 微信下单通讯失败{}",result.get("return_msg"));
            throw new LyException(ExceptionEnum.PAY_CONNECT_ERROR);
        }

        if(!result.get("result_code").equals(WXPayConstants.SUCCESS)){
            log.error("[订单微服务] 微信下单业务失败,错误码{},错误信息{}",result.get("err_code"),result.get("err_code_des"));
            throw new LyException(ExceptionEnum.PAY_SERVICE_ERROR);
        }
    }

    //检验订单金额
    public void verifyOrderTotalPay(Map<String, String> result) {
        String total_fee = result.get("total_fee");
        //商户订单号
        String out_trade_no = result.get("out_trade_no");
        if(StringUtils.isEmpty(total_fee) || StringUtils.isEmpty(out_trade_no)){
            throw new LyException(ExceptionEnum.PAY_MONEY_ERROR);
        }
        Long totalFee = Long.valueOf(total_fee);
        Long orderId = Long.valueOf(out_trade_no);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        Long actualPay = order.getActualPay();
        //模拟
        actualPay = 1L;
        if(totalFee!=actualPay){
            throw new LyException(ExceptionEnum.PAY_MONEY_ERROR);
        }
    }

    public String createPayUrl(Long orderId,Long totalPay,String desc) {

        try {
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body",desc);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            //货币
            data.put("fee_type", "CNY");
            //金额，单位是分
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端IP（estore商城的IP）
            data.put("spbill_create_ip", "192.168.183.1");
            //回调地址
            data.put("notify_url", config.getNotifyUrl());
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");
            //商品id,使用假数据
            data.put("product_id", "1234567");

            Map<String, String> result = wxPay.unifiedOrder(data);

            //返回状态校验
            verifyCallback(result);
            //校验签名和金额
            verifySign(result);
            verifyOrderTotalPay(result);

            return result.get("code_url");

        } catch (Exception e) {
            log.error("创建预交易订单异常,{}", e);
            return null;
        }
    }

    //商户主动查看订单状态
    public PayState queryOrderState(Long orderId) {
        try{
            Map<String, String> data = new HashMap<>();
            data.put("out_trade_no", orderId.toString());
            Map<String, String> result = wxPay.orderQuery(data);
            verifyCallback(result);
            verifySign(result);
            verifyOrderTotalPay(result);
            String tradeState = result.get("trade_state");

            if("SUCCESS".equals(tradeState)){
                //修改订单状态表
                OrderStatus orderStatus = new OrderStatus();
                orderStatus.setOrderId(orderId);
                orderStatus.setStatus(OrderStatusEnum.PAYED.value());
                orderStatus.setPaymentTime(new Date());
                orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
                log.info("[订单回调] 订单支付成功{}",orderId);
                return PayState.SUCCESS;
            }

            if("NOTPAY".equals(tradeState) || "USERPAYING".equals(tradeState)){
                return PayState.NOT_PAY;
            }
            return PayState.FAIL;

        }catch(Exception e){
            return PayState.NOT_PAY;
        }
    }
}
