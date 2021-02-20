package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import net.bytebuddy.matcher.InheritedAnnotationMatcher;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private TemplateEngine templateEngine;

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();

        // 获取sku相关信息
        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return null;
            }
            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            return skuEntity;
        }, threadPoolExecutor);

        // 设置分类信息
        CompletableFuture<Void> catesFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<CategoryEntity>> catesResponseVo = this.pmsClient.query123CategoriesByCid3(skuEntity.getCategoryId());
            List<CategoryEntity> categoryEntities = catesResponseVo.getData();
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);

        // 品牌信息
        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);

        // spu信息
        CompletableFuture<Void> spuFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);

        // sku的图片列表
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> imagesResponseVo = this.pmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = imagesResponseVo.getData();
            itemVo.setImages(skuImagesEntities);
        }, threadPoolExecutor);

        // sku营销信息
        CompletableFuture<Void> salesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> saleResponseVo = this.smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> sales = saleResponseVo.getData();
            itemVo.setSales(sales);
        }, threadPoolExecutor);

        // 库存信息
        CompletableFuture<Void> storeFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, threadPoolExecutor);

        // 所有销售属性
        CompletableFuture<Void> saleAttrsFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<SaleAttrValueVo>> saleAttrsResponseVo = this.pmsClient.querySaleAttrsBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrsResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrValueVos);
        }, threadPoolExecutor);

        // 当前sku的销售属性：{4: '暗夜黑', 5: '8G', 6: '128G'}
        CompletableFuture<Void> saleAttrFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = this.pmsClient.querySaleAttrValuesBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                itemVo.setSaleAttr(skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue)));
            }
        }, threadPoolExecutor);

        // skuId和销售属性组合的映射关系
        CompletableFuture<Void> mappingFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<String> stringResponseVo = this.pmsClient.querySaleAttrsMappingSkuIdBySpuId(skuEntity.getSpuId());
            String json = stringResponseVo.getData();
            itemVo.setSkuJsons(json);
        }, threadPoolExecutor);

        // 海报信息
        CompletableFuture<Void> descFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = this.pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if (spuDescEntity != null && StringUtils.isNotBlank(spuDescEntity.getDecript())) {
                itemVo.setSpuImages(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ",")));
            }
        }, threadPoolExecutor);

        // 分组及规格参数信息
        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<ItemGroupVo>> groupResponseVo = this.pmsClient.queryGroupWithAttrValuesBy(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId);
            List<ItemGroupVo> groupVos = groupResponseVo.getData();
            itemVo.setGroups(groupVos);
        }, threadPoolExecutor);

        // 等待所有子任务执行完成，才能返回
        CompletableFuture.allOf(catesFuture, brandFuture, spuFuture, imagesFuture, salesFuture, storeFuture,
                saleAttrsFuture, saleAttrFuture, mappingFuture, descFuture, groupFuture).join();

        return itemVo;
    }

    public void generateHtml(ItemVo itemVo){

        // 初始化上下对象，通过该对象给模板传递渲染所需要的数据
        Context context = new Context();
        context.setVariable("itemVo", itemVo);
        // 初始化文件流：jdk1.8的新语法
        try (PrintWriter printWriter = new PrintWriter("D:\\project-0821\\html\\" + itemVo.getSkuId() + ".html")) {
            // 通过模板引擎生成静态页面，1-模板的名称， 2-上下文对象  3-文件流
            this.templateEngine.process("item", context, printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class CompletableFutureDemo{
    public static void main(String[] args) throws IOException {

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("hello CompletableFuture");
            //int i = 1/0;
            return "hello supplyAsync";
        });
        CompletableFuture<String> future1 = future.thenApplyAsync(t -> {
            System.out.println("===============thenApplyAsync 1==============");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务的返回结果：" + t);
            return "hello thenApplyAsync 1";
        });
        CompletableFuture<String> future2 = future.thenApplyAsync(t -> {
            System.out.println("===============thenApplyAsync 2==============");
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务的返回结果：" + t);
            return "hello thenApplyAsync 2";
        });
        CompletableFuture<String> future3 = future.thenApplyAsync(t -> {
            System.out.println("===============thenApplyAsync 3==============");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务的返回结果：" + t);
            return "hello thenApplyAsync 3";
        });

        CompletableFuture.allOf(future1, future2, future3).join();
//                .whenCompleteAsync((t, u) -> {
//            System.out.println("上一个任务的返回结果集t: " + t);
//            System.out.println("上一个任务的异常信息u: " + u);
//            System.out.println("执行另一个任务");
//        }).exceptionally(t -> {
//            System.out.println("上一个任务任务的异常信息：" + t);
//            System.out.println("异常后的处理任务");
//            return "hello exceptionally";
//        });
        try {
            //System.out.println(future.get());
            System.out.println("这是主方法的打印"); // return
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.in.read();
//        FutureTask<String> futureTask = new FutureTask<>(new MyCallable());
//        new Thread(futureTask).start();
//        try {
//            System.out.println(futureTask.get());
//            System.out.println("这是主线程的打印。。。");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}

class MyCallable implements Callable<String>{
    @Override
    public String call() throws Exception {
        System.out.println("这是使用Callable初始化了多线程程序");
        return "hello callable.....";
    }
}
