package com.laoyang.ware.controller;

import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.R;
import com.laoyang.common.vo.order.LockWareResultVo;
import com.laoyang.common.vo.ware.WareSkuLockVo;
import com.laoyang.ware.config.excep.NoStockException;
import com.laoyang.ware.entity.WareSkuEntity;
import com.laoyang.ware.service.WareSkuService;
import com.laoyang.common.vo.ware.SkuStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品库存
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 锁定库存
     *
     * @param vo
     * @return
     */
    @PostMapping(value = "/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo) {
        try {
            wareSkuService.orderLockStock(vo);
           return R.ok();
        } catch (NoStockException e) {
            return R.ok();
//            return R.error(500, e.getMessage());
        }
    }


    /**
     * 商品上架时、远程调用仓储服务、查询库存数据
     * 给我一串skuIds、
     * 返回他们各自的库存数据
     *
     * @param skuIds
     * @return
     */
    @PostMapping("/hasstock")
    public R<List<SkuStockVo>> selectSkuStock(@RequestBody List<Long> skuIds) {
        List<SkuStockVo> skuStockVos = wareSkuService.selectSkuStock(skuIds);
        return R.ok().put("data", skuStockVos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
