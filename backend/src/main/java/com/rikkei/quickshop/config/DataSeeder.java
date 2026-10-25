package com.rikkei.quickshop.config;

import com.rikkei.quickshop.entity.Category;
import com.rikkei.quickshop.entity.Product;
import com.rikkei.quickshop.entity.Role;
import com.rikkei.quickshop.entity.User;
import com.rikkei.quickshop.repository.CategoryRepository;
import com.rikkei.quickshop.repository.ProductRepository;
import com.rikkei.quickshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Khởi tạo dữ liệu mẫu khi database trống: 2 tài khoản demo, 4 danh mục, 16 sản phẩm.
 * Nội dung mô tả sản phẩm được soạn với sự hỗ trợ của AI (AI-assisted mock data).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;

    public DataSeeder(UserRepository userRepository, CategoryRepository categoryRepository,
                      ProductRepository productRepository, PasswordEncoder passwordEncoder,
                      @Value("${app.seed.enabled:true}") boolean enabled) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        seedUser("admin", "admin@quickshop.local", "Quản trị viên", "admin123", Role.ADMIN);
        seedUser("customer", "customer@quickshop.local", "Khách hàng Demo", "customer123", Role.CUSTOMER);

        if (productRepository.count() > 0) {
            return;
        }
        Map<String, Category> cats = new HashMap<>();
        for (String name : new String[]{"Điện tử", "Thời trang", "Nhà cửa", "Sách"}) {
            cats.put(name, categoryRepository.findByName(name).orElseGet(() -> categoryRepository.save(new Category(name))));
        }

        product(cats.get("Điện tử"), "Tai nghe Bluetooth QuickSound Pro", 590_000, 40, "headphones",
                "Tai nghe Bluetooth 5.3 chống ồn chủ động, pin nghe nhạc đến 30 giờ và sạc nhanh 10 phút dùng 2 giờ. Đệm tai êm, gập gọn tiện mang theo khi đi học, đi làm.");
        product(cats.get("Điện tử"), "Sạc dự phòng 10000mAh SlimPower", 350_000, 60, "powerbank",
                "Pin dự phòng 10000mAh mỏng nhẹ, hỗ trợ sạc nhanh PD 20W cho điện thoại, có cổng USB-C hai chiều và đèn báo dung lượng.");
        product(cats.get("Điện tử"), "Chuột không dây Silent Click", 189_000, 80, "mouse",
                "Chuột không dây kết nối 2.4GHz, nút bấm giảm ồn, DPI điều chỉnh 3 mức, thiết kế công thái học cho cả ngày làm việc.");
        product(cats.get("Điện tử"), "Bàn phím cơ RK61 Hot-swap", 990_000, 25, "keyboard",
                "Bàn phím cơ 61 phím layout gọn, switch hot-swap thay nóng, đèn nền RGB và kết nối ba chế độ có dây, Bluetooth, 2.4GHz.");
        product(cats.get("Thời trang"), "Áo thun cotton Basic Tee", 159_000, 120, "tshirt",
                "Áo thun 100% cotton định lượng 220gsm, form regular dễ mặc, thấm hút mồ hôi tốt và giữ màu sau nhiều lần giặt.");
        product(cats.get("Thời trang"), "Áo hoodie nỉ Rikkei Campus", 420_000, 50, "hoodie",
                "Hoodie nỉ bông dày dặn, mũ trùm có dây rút, túi kangaroo phía trước; giữ ấm tốt cho mùa thu đông.");
        product(cats.get("Thời trang"), "Quần jean slim fit Indigo", 480_000, 45, "jeans",
                "Quần jean co giãn nhẹ, dáng slim fit tôn dáng, màu indigo dễ phối với áo thun, sơ mi hay hoodie.");
        product(cats.get("Thời trang"), "Túi tote canvas Minimal", 129_000, 90, "totebag",
                "Túi tote vải canvas dày, quai đeo chắc chắn, có ngăn nhỏ bên trong; vừa laptop 14 inch và sách vở.");
        product(cats.get("Nhà cửa"), "Bình giữ nhiệt inox 500ml", 220_000, 70, "bottle",
                "Bình giữ nhiệt inox 304 hai lớp chân không, giữ nóng 12 giờ và giữ lạnh 24 giờ, nắp chống tràn an toàn.");
        product(cats.get("Nhà cửa"), "Đèn ngủ LED cảm biến", 145_000, 65, "lamp",
                "Đèn LED tự bật khi trời tối và có chuyển động, ánh sáng vàng dịu, sạc USB, dán tường không cần khoan.");
        product(cats.get("Nhà cửa"), "Gối tựa lưng memory foam", 310_000, 35, "pillow",
                "Gối tựa lưng cao su non memory foam ôm theo đường cong cột sống, giảm mỏi lưng khi ngồi làm việc lâu.");
        product(cats.get("Nhà cửa"), "Cây sen đá mini để bàn", 79_000, 100, "succulent",
                "Chậu sen đá mini kèm chậu gốm trắng, dễ chăm sóc, tạo điểm xanh cho bàn học và bàn làm việc.");
        product(cats.get("Sách"), "Clean Code (Bản tiếng Việt)", 245_000, 30, "cleancode",
                "Cuốn sách kinh điển về cách viết mã sạch, dễ đọc, dễ bảo trì; phù hợp cho sinh viên và lập trình viên mới đi làm.");
        product(cats.get("Sách"), "Docker từ cơ bản đến thực chiến", 199_000, 40, "dockerbook",
                "Hướng dẫn container hóa ứng dụng với Docker, Docker Compose và triển khai CI/CD, kèm bài tập thực hành.");
        product(cats.get("Sách"), "Sổ tay bìa da A5", 99_000, 150, "notebook",
                "Sổ tay A5 bìa da PU, 200 trang giấy kẻ dòng 100gsm, có dây đánh dấu trang và chun giữ sổ.");
        product(cats.get("Sách"), "Spring Boot thực chiến", 265_000, 0, "springbook",
                "Xây dựng REST API với Spring Boot, Spring Security JWT và Spring Data JPA qua một dự án thương mại điện tử hoàn chỉnh.");
        log.info("Seeded {} categories and {} products", cats.size(), productRepository.count());
    }

    private void seedUser(String username, String email, String fullName, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setFullName(fullName);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        userRepository.save(u);
        log.info("Seeded demo account '{}' ({})", username, role);
    }

    private void product(Category category, String name, long price, int stock, String imageSeed, String description) {
        Product p = new Product();
        p.setCategory(category);
        p.setName(name);
        p.setPrice(BigDecimal.valueOf(price));
        p.setStock(stock);
        p.setImageUrl("https://picsum.photos/seed/" + imageSeed + "/600/450");
        p.setDescription(description);
        productRepository.save(p);
    }
}
