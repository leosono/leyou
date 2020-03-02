package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author leoso
 * @create 2019-12-12 11:23
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum ExceptionEnum {
    //public static final ExceptionEnum PRICE_CAN_NOT_BE_NULL = new ExceptionEnum(...);
    PRICE_CAN_NOT_BE_NULL(400,"价格不能为空"),
    CATEGORY_NOT_FOUND(404,"查询不到该分类"),
    BRAND_NOT_FOUND(404,"品牌找不到"),
    GOODS_NOT_FOUND(404,"商品找不到"),
    SPEC_GROUP_NOT_FOUND(404,"规格组查询不到"),
    SPEC_PARAM_NOT_FOUND(404,"规格参数查询不到"),
    BRAND_SAVE_ERROR(500,"品牌添加失败"),
    SPEC_GROUP_SAVE_ERROR(500,"规格组添加失败"),
    BRAND_UPDATE_ERROR(500,"品牌修改失败"),
    SPEC_GROUP_UPDATE_ERROR(500,"规格组修改失败"),
    BRAND_DELETE_ERROR(500,"品牌删除失败"),
    SPEC_GROUP_DELETE_ERROR(500,"规格组删除失败"),
    INVALID_IMAGE_TYPE(400,"非法的文件类型"),
    IMAGE_UPLOAD_ERROR(500,"图片上传失败"),
    GOODS_SAVE_ERROR(500,"商品添加失败"),
    GOODS_SPUDETAIL_NOT_FOUND(404,"商品详情找不到"),
    GOODS_SKU_NOT_FOUND(404,"商品SKU找不到"),
    GOODS_STOCK_NOT_FOUND(404,"商品库存找不到"),
    GOODS_SPU_NOT_FOUND(404,"商品SPU找不到"),
    INVALID_USER_DATA_TYPE(400,"非法的用户数据类型"),
    INVALID_VERIFY_CODE(400,"验证码不正确"),
    INVALID_USERNAME_PASSWORD(400,"用户名或密码错误"),
    TOKEN_CREATED_ERROR(500,"用户凭证创建失败"),
    UNAUTHORIZED(403,"用户未认证"),
    CART_NOT_FOUND(404,"购物车查询不到"),
    ORDER_INSERT_ERROR(500,"订单添加失败"),
    ORDERDETAIL_INSERT_ERROR(500,"订单详情添加失败"),
    ORDERSTATUS_INSERT_ERROR(500,"订单状态添加失败"),
    STOCK_UPDATE_ERROR(500,"库存更新失败"),
    ORDER_QUERY_ERROR(404,"订单查询不到"),
    ORDERDETAIL_QUERY_ERROR(404,"订单详情查询不到"),
    ORDERSTATUS_QUERY_ERROR(404,"订单状态查询不到"),
    PAY_CONNECT_ERROR(500,"微信下单通讯失败"),
    PAY_SERVICE_ERROR(500,"微信下单业务失败"),
    PAY_ERROR(500,"微信下单失败"),
    PAY_SIGN_ERROR(500,"签名校验失败"),
    PAY_MONEY_ERROR(500,"订单金额错误")
        ;
    private int code;
    private String message;
}
