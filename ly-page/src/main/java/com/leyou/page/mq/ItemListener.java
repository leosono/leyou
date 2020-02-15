package com.leyou.page.mq;

import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {
    @Autowired
    private PageService pageService;
    //创建新的静态页
    @RabbitListener(bindings = @QueueBinding(
           value = @Queue(name = "page.item.create.queue",durable = "true"),
           exchange = @Exchange(name = "ly.item.exchange", type = ExchangeTypes.TOPIC),
           key = {"item.insert","item.update"}
    ))
    public void listenCreate(Long spuId){
        pageService.createHtml(spuId);
    }
}
