package com.laoyang.product.server.impl.category;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.Query;
import com.laoyang.product.dao.CategoryDao;
import com.laoyang.product.entity.CategoryEntity;
import com.laoyang.product.server.inter.CategoryBrandRelationService;
import com.laoyang.product.server.inter.CategoryService;
import com.laoyang.product.vo.web.Catalog2Vo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    CategoryDao categoryDao;

    @Resource
    RedissonClient redissonClient;

    @Autowired(required = false)
    StringRedisTemplate stringRedisTemplate;

    @Resource
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public List<CategoryEntity> listOneCategory(Long parentId) {
        if (parentId == null) {
            parentId = 0L;
        }
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", parentId));
    }


    /**
     * 组装分类树
     * 先查出所有一级分类、以一级id为key
     * 在查出所有二级分类、以一级id为父key、作为value
     * 在查出所有三级分类、以二级Id为父key、封装到category3字段
     *
     * @return
     */
//    @Cacheable(value = {"category", "product"}, key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> listCategoryTree() {
        /**
         * 初次查询、将结果主动缓存到Redis、和注解缓存到Redis
         * 二次查询、不会进入方法、直接从注解缓存获取结果
         */
        /**
         * 预防高并发请求下、缓存击穿、
         */
        Map<String, List<Catalog2Vo>> res = null;
        //从缓存中获取JSON数据
        String key = "categoryTree";
        ValueOperations<String, String> forValue = stringRedisTemplate.opsForValue();

        while (true) {
            //获取结果缓存
            String tree = forValue.get(key);
            //如果缓存不为空、直接解析返回
            if (StringUtils.isNotEmpty(tree)) {
                return JSON.parseObject(tree, new TypeReference<Map<String, List<Catalog2Vo>>>() {
                });
            }
            //缓存为空、加锁查数据更新缓存
            RLock lock = redissonClient.getLock("categoryTree-lock");
            //拿到锁、就去查询数据库、更新缓存
            if (lock.tryLock()) {
                try {
                    //拿到锁就执行业务、否则循环
                    res = listCategoryTreeFromDB();
                    forValue.set(key, JSON.toJSONString(res));
                } finally {
                    lock.unlock();
                }
                return res;
            }
            //睡上100毫秒
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //从数据库查询
    private Map<String, List<Catalog2Vo>> listCategoryTreeFromDB() {

        //获取所有的分类
        List<CategoryEntity> allCategory = list(null);

        //从 allCategory 获取一级分类
        List<CategoryEntity> category1 = categoryFilterByCatId(allCategory, 0L);

        Map<String, List<Catalog2Vo>> map = category1.stream().collect(Collectors.toMap(
                //把1级分类的 id 作为key
                key -> key.getCatId().toString(),
                val -> {
                    //获取当前分类的所有子分类
                    List<CategoryEntity> category2 = categoryFilterByCatId(allCategory, val.getCatId());

                    //封装父视图集合作为val  封装二级分类
                    List<Catalog2Vo> catalog2Vos = null;

                    if (category2 != null) {
                        catalog2Vos = category2.stream().map(l2 -> {

                            //封装 map 的值的一个元素
                            Catalog2Vo vo = new Catalog2Vo();

                            vo.setId(l2.getCatId().toString());
                            vo.setName(l2.getName());
                            vo.setCatalog1Id(val.getCatId().toString());

                            //获取当前分类的子分类
                            List<CategoryEntity> category3 = categoryFilterByCatId(allCategory, l2.getCatId());
                            //封装三级子分类
                            List<Catalog2Vo.Catalog3Vo> val3 = null;
                            if (category3 != null) {
                                val3 = category3.stream().map(l3 -> {
                                    Catalog2Vo.Catalog3Vo childVo = new Catalog2Vo.Catalog3Vo();

                                    childVo.setId(l3.getCatId().toString());
                                    childVo.setName(l3.getName());
                                    childVo.setCatalog2Id(l2.getCatId().toString());

                                    return childVo;
                                }).collect(Collectors.toList());
                            }
                            vo.setCatalog3List(Collections.unmodifiableList(val3));
                            return vo;
                        }).collect(Collectors.toList());
                    }
                    return catalog2Vos;
                }
        ));
        return map;
    }

    /**
     * 过滤出 指定parentId 的分类数据
     *
     * @param categorys 所有的分类
     * @param parentId  指定父id
     * @return
     */
    private List<CategoryEntity> categoryFilterByCatId(List<CategoryEntity> categorys, Long parentId) {
        return categorys.stream()
                .filter(item -> item.getParentCid().equals(parentId))
                .collect(Collectors.toList());
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );
        return new PageUtils(page);
    }


    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }


    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }


    /**
     * 组装分类树
     *
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类数据
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1、找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream()
                //过滤、拿到所有一级分类
                .filter(categoryEntity ->
                        categoryEntity.getParentCid() == 0)
                //映射、得到所有子分类然后存到children集合内
                .map((menu) -> {
                    menu.setChildren(getChildrens(menu, entities));
                    return menu;
                })
                //排序、进行非空校验并升序排列
                .sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) -
                            (menu2.getSort() == null ? 0 : menu2.getSort());
                })
                //收集组装集合
                .collect(Collectors.toList());
        return level1Menus;
    }

    /**
     * 为所有的父分类找到他的子分类并映射到child集合里、
     *
     * @param root 父分类
     * @param all  原始集合数据
     * @return
     */
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream()
                //root为父分类、得到所有分类的父Id == root分类Id的数据
                .filter(categoryEntity -> {
                    return categoryEntity.getParentCid() == root.getCatId();
                })
                //映射、然后递归调用、
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildrens(categoryEntity, all));
                    return categoryEntity;
                })
                //排序
                .sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                })
                //封装
                .collect(Collectors.toList());
        return children;
    }


}