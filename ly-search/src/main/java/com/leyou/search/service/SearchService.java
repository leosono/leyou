package com.leyou.search.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.SpecParam;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.GoodsRepository;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private SpecificationClient specificationClient;

    public PageResult<Goods> search(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        //判断是否有搜索条件，如果没有，直接返回null,不允许搜索全部商品
        if(StringUtils.isBlank(key)){
            return null;
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //结果过滤(对Goods)
        nativeSearchQueryBuilder.withSourceFilter
                (new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //分页
        nativeSearchQueryBuilder.withPageable
                (PageRequest.of(searchRequest.getPage()-1,searchRequest.getSize()));
        //过滤
        QueryBuilder basicQuery = buildBasicQuery(searchRequest);
        nativeSearchQueryBuilder.withQuery(basicQuery);
        //对分类和品牌聚合
        String categoryAggName = "categoryAgg";
        String brandAggName = "brandAgg";
        nativeSearchQueryBuilder.addAggregation
                (AggregationBuilders.terms(categoryAggName).field("cid3"));
        nativeSearchQueryBuilder.addAggregation
                (AggregationBuilders.terms(brandAggName).field("brandId"));
        //查询(聚合只能用原生template)
        AggregatedPage<Goods> result = elasticsearchTemplate.queryForPage
                (nativeSearchQueryBuilder.build(), Goods.class);
        //解析分页结果
        Long total = result.getTotalElements();
        Long totalPage = ((Integer)result.getTotalPages()).longValue();
        List<Goods> content = result.getContent();
        //解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categoryList = parseCategoryAgg(aggs.get(categoryAggName));
        //聚合规格参数
        List<Map<String,Object>> specs =null;
        if(categoryList!=null && categoryList.size()==1){
            specs = buildSpecificationAgg(categoryList.get(0).getId(),basicQuery);
        }
        List<Brand> brandList = parseBrandAgg(aggs.get(brandAggName));
        return new SearchResult
                (total, totalPage, content, categoryList,brandList,specs);
    }

    //构建基本查询对象(搜索过滤)
    public QueryBuilder buildBasicQuery(SearchRequest searchRequest){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",searchRequest.getKey()).operator(Operator.AND));
        Map<String, String> filter = searchRequest.getFilter();
        for(Map.Entry<String,String> entry:filter.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            //如果key是品牌或分类，前台发送过来的不是key的名称，而是cid3和brandId
            if(!"cid3".equals(key) && !"brandId".equals(key)){
                key = "specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,value));
        }
        return boolQueryBuilder;
    }

    //解析品牌聚合结果
    public List<Category> parseCategoryAgg(LongTerms agg){
        List<LongTerms.Bucket> buckets = agg.getBuckets();
        List<Long> ids = buckets.stream().map(b -> b.getKeyAsNumber().longValue())
                .collect(Collectors.toList());
        System.out.println(ids+"-----------------------------------");
        List<Category> categories = categoryClient.queryCategoryByIds(ids);
        return categories;
    }

    //解析品牌聚合结果
    public List<Brand> parseBrandAgg(LongTerms agg){
        List<LongTerms.Bucket> buckets = agg.getBuckets();
        List<Long> ids=buckets.stream().map(b -> b.getKeyAsNumber().longValue())
                .collect(Collectors.toList());
        List<Brand> brandList = brandClient.queryBrandByIds(ids);
        return brandList;
    }

    //聚合规格参数
    public List<Map<String,Object>> buildSpecificationAgg(Long cid,QueryBuilder basicQuery){
        List<Map<String,Object>> specs = new ArrayList<>();
        List<SpecParam> specParams = specificationClient.queryParamList(null, cid, true);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(basicQuery);
        for (SpecParam specParam : specParams) {
           String key = specParam.getName();
           //添加了一堆聚合
           nativeSearchQueryBuilder.addAggregation
                   (AggregationBuilders.terms(key).field("specs."+key+".keyword"));
        }
        //查询与解析
        AggregatedPage<Goods> goodAgg = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
        Aggregations aggs = goodAgg.getAggregations();
        for (SpecParam specParam : specParams) {
            String key = specParam.getName();
            StringTerms terms = aggs.get(key);
            List<String> values = terms.getBuckets().stream()
                    .map(b -> b.getKeyAsString()).collect(Collectors.toList());
            Map<String,Object> map = new HashMap();
            map.put("k", key);
            map.put("options",values);
            specs.add(map);
        }
        return specs;
    }
}
