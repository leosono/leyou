package com.leyou.order.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import com.leyou.order.enums.PayState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        //1.添加订单表
        Order order = new Order();
        //1.1添加基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());
        //1.2添加买家信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        //1.3添加物流信息
        /*System.out.println(orderDTO.getAddressId());
        System.out.println(AddressClient.findById(orderDTO.getAddressId()));*/
        AddressDTO addr = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverState(addr.getState());
        order.setReceiverZip(addr.getZipCode());
        //1.4金额
        Long totalPay = 0L;
        List<CartDTO> cartList = orderDTO.getCarts();
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartDTO cartDTO : cartList) {
            Long skuId = cartDTO.getSkuId();
            Sku sku = goodsClient.querySkuById(skuId);
            Long price = sku.getPrice();
            Integer num = cartDTO.getNum();
            totalPay += num*price;

            //订单详情
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            orderDetail.setNum(num);
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setSkuId(skuId);
            orderDetailList.add(orderDetail);
        }
        order.setTotalPay(totalPay);
        order.setActualPay(totalPay+order.getPostFee()-0);

        int count = orderMapper.insertSelective(order);
        if(count!=1){
            throw new LyException(ExceptionEnum.ORDER_INSERT_ERROR);
        }
        //添加订单详情表
        count = orderDetailMapper.insertList(orderDetailList);
        if(count!=orderDetailList.size()){
            throw new LyException(ExceptionEnum.ORDERDETAIL_INSERT_ERROR);
        }
        //添加订单状态表
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(new Date());
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        count = orderStatusMapper.insertSelective(orderStatus);
        if(count!=1){
            throw new LyException(ExceptionEnum.ORDERSTATUS_INSERT_ERROR);
        }

        //减库存
        for (CartDTO cartDTO : cartList) {
            goodsClient.decreaseStock(cartDTO.getSkuId(), cartDTO.getNum());
        }

        return orderId;
    }

    public Order queryOrder(Long orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order == null){
            throw new LyException(ExceptionEnum.ORDER_QUERY_ERROR);
        }
        //为了方便我的乐优模块顺带查出orderDetail和OrderStatus
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        if(CollectionUtils.isEmpty(orderDetails)){
            throw new LyException(ExceptionEnum.ORDERDETAIL_QUERY_ERROR);
        }
        order.setOrderDetails(orderDetails);

        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        if(orderStatus == null){
            throw new LyException(ExceptionEnum.ORDERSTATUS_QUERY_ERROR);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    public String createPayUrl(Long orderId) {
        Order order = queryOrder(orderId);
        Long totalPay = order.getTotalPay();
        //模拟
        totalPay = 1L;
        String desc = order.getOrderDetails().get(0).getTitle();
        String payUrl = payHelper.createPayUrl(orderId, totalPay, desc);
        if(StringUtils.isEmpty(payUrl)){
            throw new LyException(ExceptionEnum.PAY_ERROR);
        }
        return payUrl;
    }

    //微信的回调service
    public void notify(Map<String, String> result) {
        //校验返回状态
        payHelper.verifyCallback(result);
        //校验签名
        payHelper.verifySign(result);
        //校验订单金额
        payHelper.verifyOrderTotalPay(result);

        //修改订单状态表的订单状态
        OrderStatus orderStatus = new OrderStatus();
        Long orderId = Long.valueOf(result.get("out_trade_no"));
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.PAYED.value());
        orderStatus.setPaymentTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        log.info("[订单回调] 订单支付成功{}",orderId);
    }

    public PayState queryPayState(Long orderId) {
        //查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();

        if(status!=OrderStatusEnum.UN_PAY.value()){
            return PayState.SUCCESS;
        }
        //如果等于未付款
        PayState payState = payHelper.queryOrderState(orderId);

        return payState;
    }
}
