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
    CATEGORY_NOT_FOUND(400,"查询不到该分类"),
    BRAND_NOT_FOUND(400,"品牌找不到"),
    GOODS_NOT_FOUND(400,"商品找不到"),
    SPEC_GROUP_NOT_FOUND(400,"规格组查询不到"),
    SPEC_PARAM_NOT_FOUND(400,"规格参数查询不到"),
    BRAND_SAVE_ERROR(500,"品牌添加失败"),
    SPEC_GROUP_SAVE_ERROR(500,"规格组添加失败"),
    BRAND_UPDATE_ERROR(500,"品牌修改失败"),
    SPEC_GROUP_UPDATE_ERROR(500,"规格组修改失败"),
    BRAND_DELETE_ERROR(500,"品牌删除失败"),
    SPEC_GROUP_DELETE_ERROR(500,"规格组删除失败"),
    INVALID_IMAGE_TYPE(500,"非法的文件类型"),
    IMAGE_UPLOAD_ERROR(500,"图片上传失败"),
    GOODS_SAVE_ERROR(500,"商品添加失败"),
    GOODS_SPUDETAIL_NOT_FOUND(500,"商品详情找不到"),
    GOODS_SKU_NOT_FOUND(500,"商品SKU找不到"),
    GOODS_STOCK_NOT_FOUND(500,"商品库存找不到"),
        ;
    private int code;
    private String message;
}
