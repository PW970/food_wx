package com.foodrecommendation;

import com.foodrecommendation.entity.Shop;
import com.foodrecommendation.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class FoodRecommendationApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodRecommendationApplication.class, args);
    }

    @Autowired
    private ShopRepository shopRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (shopRepository.count() == 0) {
                List<Shop> shops = Arrays.asList(
                    createShop("海底捞火锅", "北京市朝阳区建国路88号", 39.9042, 116.4074, "火锅店", 4.8, 256),
                    createShop("呷哺呷哺", "北京市海淀区中关村大街1号", 39.9830, 116.3120, "火锅店", 4.5, 189),
                    createShop("木屋烧烤", "北京市西城区西单北大街120号", 39.9139, 116.3734, "烧烤店", 4.6, 145),
                    createShop("很久以前羊肉串", "北京市东城区王府井大街138号", 39.9163, 116.4102, "烧烤店", 4.7, 203),
                    createShop("兰州拉面", "北京市丰台区方庄路18号", 39.8698, 116.4350, "面馆", 4.3, 98),
                    createShop("山西刀削面", "北京市石景山区石景山路68号", 39.9070, 116.2220, "面馆", 4.4, 76),
                    createShop("许留山甜品", "北京市朝阳区三里屯路11号", 39.9350, 116.4470, "甜品店", 4.9, 167),
                    createShop("满记甜品", "北京市海淀区五道口购物中心", 40.0034, 116.3200, "甜品店", 4.7, 134),
                    createShop("绿茶餐厅", "北京市朝阳区望京SOHO", 39.9960, 116.4710, "中餐", 4.5, 211),
                    createShop("外婆家", "北京市西城区金融街购物中心", 39.9153, 116.3609, "中餐", 4.6, 245),
                    createShop("粤菜馆", "北京市东城区崇文门新世界", 39.9015, 116.4165, "粤菜", 4.8, 178),
                    createShop("川菜王", "北京市海淀区五棵松华熙LIVE", 39.9056, 116.2734, "川菜", 4.4, 156)
                );
                shopRepository.saveAll(shops);
                System.out.println("=== 初始化测试数据完成，共插入 " + shops.size() + " 条店铺数据 ===");
            } else {
                System.out.println("=== 店铺数据已存在，跳过初始化 ===");
            }
        };
    }

    private Shop createShop(String name, String address, Double latitude, Double longitude,
                           String category, Double score, Integer reviewCount) {
        Shop shop = new Shop();
        shop.setName(name);
        shop.setAddress(address);
        shop.setLatitude(latitude);
        shop.setLongitude(longitude);
        shop.setCategory(category);
        shop.setScore(score);
        shop.setReviewCount(reviewCount);
        return shop;
    }
}
