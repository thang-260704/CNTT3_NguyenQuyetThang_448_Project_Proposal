package com.rikkei.quickshop.ai;

import com.rikkei.quickshop.dto.ProductDtos.ProductResponse;
import com.rikkei.quickshop.entity.Category;
import com.rikkei.quickshop.entity.Product;
import com.rikkei.quickshop.exception.ApiException;
import com.rikkei.quickshop.repository.CategoryRepository;
import com.rikkei.quickshop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Sinh dữ liệu sản phẩm mẫu (mock data) cho môi trường dev/demo, mô tả được tạo bằng AiService. */
@Service
public class MockDataService {

    private static final Map<String, List<String>> NAMES = Map.of(
            "Điện tử", List.of("Tai nghe Bluetooth", "Sạc dự phòng 10000mAh", "Chuột không dây", "Bàn phím cơ", "Loa mini"),
            "Thời trang", List.of("Áo thun cotton", "Áo hoodie", "Quần jean slim", "Mũ lưỡi trai", "Túi tote canvas"),
            "Nhà cửa", List.of("Bình giữ nhiệt", "Đèn ngủ LED", "Gối tựa lưng", "Hộp đựng đồ", "Cây xanh mini"),
            "Sách", List.of("Sổ tay bìa da", "Sách kỹ năng", "Truyện tranh", "Sách học lập trình", "Bộ bút màu"));
    private static final List<String> SUFFIXES = List.of("Pro", "Lite", "Plus", "2026", "Basic", "Max");
    private static final List<String> SEEDS = List.of("alpha", "bravo", "charlie", "delta", "echo", "foxtrot");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AiService aiService;

    public MockDataService(ProductRepository productRepository, CategoryRepository categoryRepository,
                           AiService aiService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.aiService = aiService;
    }

    @Transactional
    public List<ProductResponse> generate(int count) {
        if (count < 1 || count > 20) {
            throw ApiException.badRequest("Số lượng sản phẩm mẫu phải từ 1 đến 20");
        }
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw ApiException.badRequest("Chưa có danh mục nào");
        }
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<ProductResponse> created = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Category category = categories.get(rnd.nextInt(categories.size()));
            List<String> pool = NAMES.getOrDefault(category.getName(), NAMES.get("Điện tử"));
            String name = pool.get(rnd.nextInt(pool.size())) + " " + SUFFIXES.get(rnd.nextInt(SUFFIXES.size()));

            Product p = new Product();
            p.setName(name);
            p.setCategory(category);
            p.setPrice(BigDecimal.valueOf(rnd.nextInt(5, 200) * 10_000L));
            p.setStock(rnd.nextInt(5, 100));
            p.setImageUrl("https://picsum.photos/seed/" + SEEDS.get(rnd.nextInt(SEEDS.size())) + rnd.nextInt(1000) + "/600/450");
            p.setDescription(aiService.generateDescription(name, category.getName()).description());
            productRepository.save(p);
            created.add(ProductResponse.from(p));
        }
        return created;
    }
}
