package com.laoyang.search.server;

import com.laoyang.common.vo.es.SkuVO;

import java.util.List;

public interface ProductService {

    boolean productStatusUp(List<SkuVO> skuVOList);

}
