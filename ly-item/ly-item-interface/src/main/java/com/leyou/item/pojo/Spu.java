package com.leyou.item.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * @author leoso
 * @create 2020-01-06 12:20
 */
@Data
@Table(name="tb_spu")
public class Spu {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long brandId;
    private Long cid1;
    private Long cid2;
    private Long cid3;
    /**商品名称 Iphone8*/
    private String title;
    private String subTitle;
    private Boolean saleable;
    @JsonIgnore
    private Boolean valid; //是否有效，逻辑删除用
    private Date createTime;
    @JsonIgnore
    private Date lastUpdateTime;
    @Transient
    private String cname;//页面的商品分类
    @Transient
    private String bname;//页面的品牌
}
