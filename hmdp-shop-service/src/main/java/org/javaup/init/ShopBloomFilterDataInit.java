package org.javaup.init;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.entity.Shop;
import org.javaup.handler.BloomFilterHandlerFactory;
import org.javaup.service.IShopService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.javaup.constant.Constant.BLOOM_FILTER_HANDLER_SHOP;

/**
 * 商铺布隆过滤器初始化。
 * 商铺查询前会先做布隆判断，未初始化会被判定为"不存在"，因此启动时必须加载。
 */
@Slf4j
@Order(1)
@Component
public class ShopBloomFilterDataInit {

    @Resource
    private IShopService shopService;

    @Resource
    private BloomFilterHandlerFactory bloomFilterHandlerFactory;

    @PostConstruct
    public void init() {
        log.info("==========初始化商铺的布隆过滤器==========");
        List<Shop> shopList = shopService.list();
        for (Shop shop : shopList) {
            bloomFilterHandlerFactory.get(BLOOM_FILTER_HANDLER_SHOP).add(String.valueOf(shop.getId()));
        }
    }
}
