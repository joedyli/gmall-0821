package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponseAttrVo {

    private Long attrId;

    private String attrName;

    private List<String> attrValues; // 规格参数的可选值列表
}
