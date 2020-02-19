package com.leyou.sms.mq;

import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsConfig;
import com.leyou.sms.utils.SmsUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Component
@EnableConfigurationProperties(SmsConfig.class)
public class SmsListener {

    @Autowired
    private SmsUtil smsUtil;

    @Autowired
    private SmsConfig smsConfig;

    //接收发松短信验证码的消息
    //发送消息只能携带一个参数,而我们的smsUtil有很多参数，所以封装成Map
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.code.queue",durable = "true"),
            exchange = @Exchange(name = "ly.sms.exchange",type = ExchangeTypes.TOPIC),
            key = "sms.verifycode"
    ))
    public void listenVerifyCode(Map<String,String> map){
        if(CollectionUtils.isEmpty(map)){
            return;
        }
        //取出了phone,就剩code了。
        String phone = map.remove("phone");
        if(StringUtils.isBlank(phone)){
            return;
        }
        smsUtil.sendSms(phone, JsonUtils.serialize(map),smsConfig.getSignName(),smsConfig.getVerifyCodeTemplate());
    }
}
