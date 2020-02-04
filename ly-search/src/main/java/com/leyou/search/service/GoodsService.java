package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private SpecificationClient specificationClient;

    public Goods buildGoods(Spu spu){ 
        //分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
        List<String> nameList = categories.stream().map(Category::getName).collect(Collectors.toList());
        //品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        String all = spu.getTitle()+ StringUtils.join(nameList, ",")+brand.getName();

        List<Sku> skuList = goodsClient.querySkusBySpuId(spu.getId());
        //只取Sku中的id,title,image,price,取到skus中
        List<Map<String,Object>> skus = new ArrayList<>();
        Set<Long> priceSet = new HashSet<>();
        for (Sku sku : skuList) {
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("id", sku.getId());
            skuMap.put("title", sku.getTitle());
            skuMap.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            skus.add(skuMap);
            priceSet.add(sku.getPrice());
        }
        //Set<Long> priceSet = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());

        //查询规格参数的key（spec_param中的name）
        List<SpecParam> specParams = specificationClient.queryParamList(null, spu.getCid3(), true);
        //查询规格参数中的值(在spu_detail中的通用/特有规格参数中根据spec_param的id查找)
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spu.getId());
        //获取通用规格参数
        String genericSpecJson = spuDetail.getGenericSpec();
        //为了下面的根据spec_param的id找value，所以把json转换为Map
        Map<Long,String> genericSpec = JsonUtils.parseMap
                (genericSpecJson, Long.class,String.class);
        //获取特有规格参数
        String specialSpecJson = spuDetail.getSpecialSpec();
        Map<Long,List<String>> specialSpec = JsonUtils.nativeRead
                (specialSpecJson, new TypeReference<Map<Long, List<String>>>() {});
        //规格参数
        Map<String,Object> spec = new HashMap<>();
        for (SpecParam specParam : specParams) {
            String key = specParam.getName();
            Object value = "";
            if(specParam.getGeneric()){
                if(specParam.getNumeric()){
                    value = chooseSegment(genericSpec.get(specParam.getId()), specParam);
                }
            }else{
                value = specialSpec.get(specParam.getId());
            }
            //存入map
            spec.put(key, value);
        }

        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid1(spu.getCid2());
        goods.setCid1(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setAll(all);// 搜索字段 包括标题 分类 品牌 规格等
        goods.setPrice(priceSet);// sku的price的集合
        goods.setSkus(JsonUtils.serialize(skus));// 所有sku集合的json格式
        goods.setSpecs(spec);// 所有可搜索的规格参数
        return goods;
    }

    //规格参数数值类型转换为段的处理
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
}
