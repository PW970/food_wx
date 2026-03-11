package com.foodrecommendation.config;

import com.foodrecommendation.entity.Category;
import com.foodrecommendation.entity.Collection;
import com.foodrecommendation.entity.Review;
import com.foodrecommendation.entity.Shop;
import com.foodrecommendation.entity.ShopTag;
import com.foodrecommendation.entity.User;
import com.foodrecommendation.repository.CategoryRepository;
import com.foodrecommendation.repository.CollectionRepository;
import com.foodrecommendation.repository.ReviewRepository;
import com.foodrecommendation.repository.ShopRepository;
import com.foodrecommendation.repository.ShopTagRepository;
import com.foodrecommendation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 数据初始化器
 * Spring Boot 启动时自动插入测试数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShopTagRepository shopTagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 检查是否已有数据，避免重复插入
        if (categoryRepository.count() > 0) {
            System.out.println("=== 数据已存在，跳过初始化 ===");
            return;
        }

        System.out.println("=== 开始初始化测试数据 ===");

        // 1. 初始化用户数据
        initUsers();

        // 2. 初始化分类数据
        initCategories();

        // 3. 初始化店铺数据
        initShops();

        // 4. 初始化店铺标签
        initShopTags();

        // 5. 初始化评论数据
        initReviews();

        // 6. 初始化收藏数据
        initCollections();

        System.out.println("=== 测试数据初始化完成 ===");
    }

    /**
     * 初始化用户数据
     */
    private void initUsers() {
        List<User> users = Arrays.asList(
            createUser("oWx123456", "小明", "https://picsum.photos/100"),
            createUser("oWx234567", "小红", "https://picsum.photos/101"),
            createUser("oWx345678", "测试用户", "https://picsum.photos/102")
        );
        userRepository.saveAll(users);
    }

    private User createUser(String openid, String nickname, String avatar) {
        User user = new User();
        user.setOpenid(openid);
        user.setNickname(nickname);
        user.setAvatar(avatar);
        return user;
    }

    /**
     * 初始化分类数据 - 6个分类
     */
    private void initCategories() {
        List<Category> categories = Arrays.asList(
            createCategory("火锅", "https://picsum.photos/200"),
            createCategory("烧烤", "https://picsum.photos/201"),
            createCategory("面馆", "https://picsum.photos/202"),
            createCategory("甜品", "https://picsum.photos/203"),
            createCategory("本帮菜", "https://picsum.photos/204"),
            createCategory("小吃", "https://picsum.photos/205")
        );
        categoryRepository.saveAll(categories);
    }

    private Category createCategory(String name, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setIcon(icon);
        return category;
    }

    /**
     * 初始化店铺数据 - 15家店铺
     */
    private void initShops() {
        // 获取分类ID
        List<Category> categories = categoryRepository.findAll();
        Long hotpotId = categories.get(0).getId();  // 火锅
        Long bbqId = categories.get(1).getId();     // 烧烤
        Long noodlesId = categories.get(2).getId();  // 面馆
        Long dessertId = categories.get(3).getId();  // 甜品
        Long localId = categories.get(4).getId();     // 本帮菜
        Long snackId = categories.get(5).getId();    // 小吃

        List<Shop> shops = Arrays.asList(
            // 火锅类 (categoryId = hotpotId)
            createShop("海底捞火锅", "朝阳区望京SOHO T1", 39.9955, 116.4711, "火锅", 4.8f, 256, hotpotId,
                "010-12345678", "10:00-22:00", new BigDecimal("120.00"),
                "https://picsum.photos/300", "服务周到，食材新鲜"),
            createShop("重庆老火锅", "海淀区五道口华联", 39.9890, 116.3263, "火锅", 4.5f, 128, hotpotId,
                "010-23456789", "11:00-23:00", new BigDecimal("80.00"),
                "https://picsum.photos/301", "正宗重庆味道"),
            createShop("潮汕牛肉火锅", "朝阳区三里屯", 39.9358, 116.4475, "火锅", 4.7f, 95, hotpotId,
                "010-34567890", "11:00-21:30", new BigDecimal("100.00"),
                "https://picsum.photos/302", "牛肉鲜嫩，汤底鲜美"),

            // 烧烤类 (categoryId = bbqId)
            createShop("很久以前羊肉串", "朝阳区工体", 39.9328, 116.4425, "烧烤", 4.6f, 180, bbqId,
                "010-45678901", "17:00-02:00", new BigDecimal("90.00"),
                "https://picsum.photos/303", "氛围好，羊肉串香"),
            createShop("韩式烤肉", "朝阳区国贸", 39.9042, 116.4074, "烧烤", 4.4f, 156, bbqId,
                "010-56789012", "11:30-22:00", new BigDecimal("110.00"),
                "https://picsum.photos/304", "烤肉自助，食材丰富"),

            // 面馆类 (categoryId = noodlesId)
            createShop("兰州拉面", "海淀区中关村", 39.9890, 116.3063, "面馆", 4.5f, 320, noodlesId,
                "010-67890123", "06:00-22:00", new BigDecimal("25.00"),
                "https://picsum.photos/305", "24小时营业"),
            createShop("重庆小面", "西城区西单", 39.9137, 116.3732, "面馆", 4.3f, 85, noodlesId,
                "010-78901234", "07:00-21:00", new BigDecimal("20.00"),
                "https://picsum.photos/306", "地道重庆小面"),
            createShop("日式拉面", "朝阳区三里屯", 39.9358, 116.4475, "面馆", 4.7f, 120, noodlesId,
                "010-89012345", "11:00-22:00", new BigDecimal("55.00"),
                "https://picsum.photos/307", "豚骨汤底浓郁"),

            // 甜品类 (categoryId = dessertId)
            createShop("哈根达斯", "朝阳区国贸", 39.9042, 116.4074, "甜品", 4.2f, 450, dessertId,
                "010-90123456", "10:00-22:00", new BigDecimal("45.00"),
                "https://picsum.photos/308", "冰淇淋知名品牌"),
            createShop("满记甜品", "朝阳区大望路", 39.9085, 116.4915, "甜品", 4.4f, 280, dessertId,
                "010-01234567", "10:00-23:00", new BigDecimal("35.00"),
                "https://picsum.photos/309", "港式甜品"),
            createShop("星光甜品屋", "海淀区五道口", 39.9890, 116.3263, "甜品", 4.6f, 95, dessertId,
                "", "12:00-20:00", new BigDecimal("30.00"),
                "https://picsum.photos/310", "手工甜品"),

            // 本帮菜类 (categoryId = localId)
            createShop("全聚德烤鸭", "东城区前门", 39.9010, 116.4107, "本帮菜", 4.5f, 520, localId,
                "010-11111111", "10:30-21:00", new BigDecimal("150.00"),
                "https://picsum.photos/311", "北京烤鸭代表"),
            createShop("外婆家", "朝阳区望京", 39.9955, 116.4711, "本帮菜", 4.3f, 380, localId,
                "010-22222222", "10:00-22:00", new BigDecimal("60.00"),
                "https://picsum.photos/312", "江浙菜口味"),
            createShop("绿茶餐厅", "朝阳区三里屯", 39.9360, 116.4480, "本帮菜", 4.4f, 256, localId,
                "010-33333333", "11:00-22:00", new BigDecimal("55.00"),
                "https://picsum.photos/313", "杭帮菜代表"),

            // 小吃类 (categoryId = snackId)
            createShop("沙县小吃", "海淀区知春路", 39.9856, 116.3195, "小吃", 4.0f, 620, snackId,
                "", "06:00-23:00", new BigDecimal("15.00"),
                "https://picsum.photos/314", "经济实惠"),
            createShop("兰州沙县小吃", "朝阳区劲松", 39.8958, 116.4358, "小吃", 4.1f, 180, snackId,
                "010-44444444", "07:00-22:00", new BigDecimal("18.00"),
                "https://picsum.photos/315", "口味地道")
        );

        shopRepository.saveAll(shops);
    }

    private Shop createShop(String name, String address, Double lat, Double lng,
                           String category, Float score, Integer reviewCount, Long categoryId,
                           String phone, String hours, BigDecimal perCapita,
                           String coverImage, String description) {
        Shop shop = new Shop();
        shop.setName(name);
        shop.setAddress(address);
        shop.setLatitude(lat);
        shop.setLongitude(lng);
        shop.setCategory(category);
        shop.setScore(score != null ? score.doubleValue() : 0.0);
        shop.setReviewCount(reviewCount);
        shop.setCategoryId(categoryId);
        shop.setPhone(phone);
        shop.setBusinessHours(hours);
        shop.setPerCapita(perCapita);
        shop.setCoverImage(coverImage);
        shop.setDescription(description);
        shop.setStatus("OPEN");
        return shop;
    }

    /**
     * 初始化店铺标签数据
     */
    private void initShopTags() {
        List<Shop> shops = shopRepository.findAll();

        // 为每家店添加2-4个标签
        for (Shop shop : shops) {
            List<String> tags = getTagsForShop(shop.getName());
            for (String tagName : tags) {
                ShopTag tag = new ShopTag();
                tag.setShopId(shop.getId());
                tag.setTagName(tagName);
                shopTagRepository.save(tag);
            }
        }
    }

    private List<String> getTagsForShop(String shopName) {
        // 根据店铺名称返回不同的标签
        if (shopName.contains("海底捞") || shopName.contains("服务")) {
            return Arrays.asList("服务好", "食材新鲜", "适合聚会");
        } else if (shopName.contains("火锅")) {
            return Arrays.asList("味道好", "性价比高", "人气旺");
        } else if (shopName.contains("烧烤")) {
            return Arrays.asList("氛围好", "肉串香", "适合夜宵");
        } else if (shopName.contains("拉面") || shopName.contains("小面")) {
            return Arrays.asList("实惠", "口味地道", "分量足");
        } else if (shopName.contains("甜品") || shopName.contains("冰淇淋")) {
            return Arrays.asList("甜蜜", "环境好", "适合拍照");
        } else if (shopName.contains("烤鸭")) {
            return Arrays.asList("招牌菜", "经典", "必吃");
        } else if (shopName.contains("小吃")) {
            return Arrays.asList("实惠", "方便", "经济");
        } else {
            return Arrays.asList("口味好", "推荐");
        }
    }

    /**
     * 初始化评论数据
     */
    private void initReviews() {
        List<User> users = userRepository.findAll();
        List<Shop> shops = shopRepository.findAll();

        // 为部分店铺添加评论
        List<Review> reviews = Arrays.asList(
            createReview(users.get(0).getId(), shops.get(0).getId(), 5, "服务真的太好了！海底捞的服务没话说，食材也很新鲜。"),
            createReview(users.get(1).getId(), shops.get(0).getId(), 4, "味道不错，就是人太多了，需要排队。"),
            createReview(users.get(0).getId(), shops.get(5).getId(), 5, "兰州拉面真的很正宗，价格实惠，推荐！"),
            createReview(users.get(2).getId(), shops.get(5).getId(), 4, "味道不错，24小时营业很方便。"),
            createReview(users.get(1).getId(), shops.get(11).getId(), 5, "北京烤鸭果然名不虚传，皮脆肉嫩！"),
            createReview(users.get(0).getId(), shops.get(13).getId(), 5, "沙县小吃YYDS！便宜又好吃！"),
            createReview(users.get(2).getId(), shops.get(8).getId(), 4, "日式拉面汤底很浓郁，推荐豚骨拉面。")
        );

        reviewRepository.saveAll(reviews);
    }

    private Review createReview(Long userId, Long shopId, Integer rating, String content) {
        Review review = new Review();
        review.setUserId(userId);
        review.setShopId(shopId);
        review.setRating(rating);
        review.setContent(content);
        return review;
    }

    /**
     * 初始化收藏数据
     */
    private void initCollections() {
        List<User> users = userRepository.findAll();
        List<Shop> shops = shopRepository.findAll();

        // 创建一些收藏记录
        List<Collection> collections = Arrays.asList(
            createCollection(users.get(0).getId(), shops.get(0).getId()),  // 小明收藏海底捞
            createCollection(users.get(0).getId(), shops.get(5).getId()),  // 小明收藏兰州拉面
            createCollection(users.get(1).getId(), shops.get(0).getId()),  // 小红收藏海底捞
            createCollection(users.get(1).getId(), shops.get(11).getId()), // 小红收藏全聚德
            createCollection(users.get(2).getId(), shops.get(13).getId())  // 测试用户收藏沙县
        );

        collectionRepository.saveAll(collections);
    }

    private Collection createCollection(Long userId, Long shopId) {
        Collection collection = new Collection();
        collection.setUserId(userId);
        collection.setShopId(shopId);
        return collection;
    }
}
