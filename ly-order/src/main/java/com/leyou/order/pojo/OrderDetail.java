package com.leyou.order.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_order_detail")
@Data
public class OrderDetail {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    private Long orderId;

    private Long skuId;

    private Integer num;

    private String image;

    private String title;

    private Long price;

    private String ownSpec;

}
