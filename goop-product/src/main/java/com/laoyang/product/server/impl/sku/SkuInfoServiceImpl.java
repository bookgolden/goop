package com.laoyang.product.server.impl.sku;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.Query;
import com.laoyang.common.util.R;
import com.laoyang.common.vo.seckill.SecKillSkuRedisVo;
import com.laoyang.product.dao.SkuInfoDao;
import com.laoyang.product.entity.SkuImagesEntity;
import com.laoyang.product.entity.SkuInfoEntity;
import com.laoyang.product.entity.SpuInfoDescEntity;
import com.laoyang.product.feign.SecKillFeignService;
import com.laoyang.product.server.inter.*;
import com.laoyang.product.vo.web.SkuItemSaleAttrVo;
import com.laoyang.product.vo.web.SkuItemVo;
import com.laoyang.product.vo.web.SpuItemAttrGroupVo;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    SpuInfoDescService spuInfoDescService;

    @Resource
    SkuImagesService skuImagesService;

    @Resource
    AttrGroupService attrGroupService;

    @Resource
    SecKillFeignService secKillFeignService;

    @Resource
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skus = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skus;
    }

    @Autowired
    ThreadPoolExecutor executor;

    @SneakyThrows
    @Override
    public SkuItemVo getSkuItemById(Long skuId) {
        SkuItemVo itemVo = new SkuItemVo();

        //异步编排 、必须先执行
        CompletableFuture<SkuInfoEntity> skuInfoEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = baseMapper.selectById(skuId);
            itemVo.setSkuInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);

        //获取spuId后执行、获取spu info
        CompletableFuture<Void> spuFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(skuInfoEntity.getSpuId());
            itemVo.setSpuInfoDesc(spuInfoDescEntity);
        }, executor);

        //获取spuId和分类Id后执行、获取 attr group
        CompletableFuture<Void> groupFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            List<SpuItemAttrGroupVo> spuItemAttrGroupVos = attrGroupService
                    .getAttrGroupWithAttrsBySkuId(skuInfoEntity.getSpuId(), skuInfoEntity.getCatalogId());
            itemVo.setApuItemAttrGroups(spuItemAttrGroupVos);
        }, executor);

        //获取spuId后执行、获取销售属性
        CompletableFuture<Void> saleFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueService
                    .getSaleAttrsBySpuId(skuInfoEntity.getSpuId());
            itemVo.setSaleAttrs(saleAttrsBySpuId);
        }, executor);


        //执行运行
        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImages = skuImagesService.getImagesBySkuId(skuId);
            itemVo.setImages(skuImages);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            //3、远程调用查询当前sku是否参与秒杀优惠活动
            R<SecKillSkuRedisVo> skuSeckilInfo = secKillFeignService.getSkuSeckilInfo(skuId);
            if (skuSeckilInfo.getCode() == 200) {
                //查询成功
                SecKillSkuRedisVo seckilInfoData = skuSeckilInfo.getDate( new TypeReference<SecKillSkuRedisVo>() {});
                itemVo.setSecKillSkuRedisVo(seckilInfoData);

                if (seckilInfoData != null) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime > seckilInfoData.getEndTime()) {
                        itemVo.setSecKillSkuRedisVo(null);
                    }
                }
            }
        }, executor);


        //阻塞等待所有任务完成
        CompletableFuture.anyOf(groupFuture, spuFuture, saleFuture, imgFuture,seckillFuture);

        return itemVo;

//        SkuItemVo itemVo = new SkuItemVo();
//
//        /**
//         * 获取sku基本信息、pms_sku_info
//         */
//        SkuInfoEntity skuInfoEntity = baseMapper.selectById(skuId);
//        itemVo.setSkuInfo(skuInfoEntity);
//
//        Long spuId = skuInfoEntity.getSpuId();
//        Long catalogId = skuInfoEntity.getCatalogId();
//
//        /**
//         * 获取 spu详情信息 pms_spu_desc
//         */
//        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(spuId);
//        itemVo.setSpuInfoDesc(spuInfoDescEntity);
//
//        /**
//         * 获取 sku图片信息、pms_sku_images
//         */
//        List<SkuImagesEntity> skuImages = skuImagesService.getImagesBySkuId(skuId);
//        itemVo.setImages(skuImages);
//
//        /**
//         * 获取  spu属性组集合
//         *
//         */
//        List<SpuItemAttrGroupVo> spuItemAttrGroupVos = attrGroupService.getAttrGroupWithAttrsBySkuId(spuId, catalogId);
//        itemVo.setApuItemAttrGroups(spuItemAttrGroupVos);
//
//        /**
//         * 获取 spu销售属性组合、
//         *
//         */
//        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
//        itemVo.setSaleAttrs(saleAttrsBySpuId);
//        return itemVo;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {

            queryWrapper.eq("catalog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");

        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);

                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {

            }

        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }


}