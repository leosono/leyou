package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(com.leyou.order.config.WXPayConfig.class)
public class WXPayConfiguration {

    @Autowired
    private WXPayConfig wxPayConfig;

    @Bean
    public WXPay wxPay(){
        return new WXPay(wxPayConfig);
    }
}
