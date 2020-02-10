package com.leyou.search.pojo;

import java.util.Map;

public class SearchRequest {
    private String key;
    private Integer page;
    private Map<String,String> filter; //过滤条件

    private static final int DEFAULT_SIZE = 20;
    private static final int DEFAULT_PAGE = 1;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getSize() {
        return DEFAULT_SIZE;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPage(){
        //默认为1
        if(page==null){
            return DEFAULT_PAGE;
        }
        //防止用户输入负数
        return Math.max(page, DEFAULT_PAGE);
    }

    public Map<String, String> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, String> filter) {
        this.filter = filter;
    }

    public SearchRequest(String key, Integer page, Map<String, String> filter) {
        this.key = key;
        this.page = page;
        this.filter = filter;
    }
}
