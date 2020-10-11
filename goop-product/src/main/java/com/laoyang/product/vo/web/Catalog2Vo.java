package com.laoyang.product.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catalog2Vo implements Serializable {
    private String catalog1Id;
    private List<Object> catalog3List;
    private String id;
    private String name;



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catalog3Vo{
        private String catalog2Id;
        private String id;
        private String name;

    }
}
