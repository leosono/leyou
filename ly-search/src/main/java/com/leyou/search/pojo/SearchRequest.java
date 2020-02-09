package com.leyou.search.pojo;

public class SearchRequest {
    private String key;
    private Integer page;

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
}
