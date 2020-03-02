package com.leyou.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.InputStream;

@Data
@ConfigurationProperties(prefix = "ly.pay")
public class WXPayConfig implements com.github.wxpay.sdk.WXPayConfig {

    private String appID;

    private String mchID;

    private String key;

    private int httpConnectTimeoutMs;

    private int httpReadTimeoutMs;

    private String notifyUrl;

    @Override
    public InputStream getCertStream() {
        return null;
    }
}
