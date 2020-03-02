package com.leyou.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotifyService {

    @Autowired
    private OrderService orderService;

    public Map<String,String> handleNotify(Map<String, String> result) {
        orderService.notify(result);
        Map<String,String> msg = new HashMap<>();
        msg.put("return_code", "SUCCESS");
        msg.put("return_msg", "OK");
        log.info("[微信回调] 微信回调成功{}",result);
        return msg;
    }
}
