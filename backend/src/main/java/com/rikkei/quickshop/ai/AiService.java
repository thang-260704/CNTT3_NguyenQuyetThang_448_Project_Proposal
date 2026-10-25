package com.rikkei.quickshop.ai;

import com.rikkei.quickshop.dto.AiDtos.ChatResponse;
import com.rikkei.quickshop.dto.AiDtos.DescriptionResponse;
import com.rikkei.quickshop.dto.ProductDtos.ProductResponse;
import com.rikkei.quickshop.entity.Product;
import com.rikkei.quickshop.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Tích hợp AI qua Spring AI (OpenAI-compatible: OpenAI, Gemini, Groq, ...).
 * Khi chưa cấu hình AI_API_KEY hoặc gọi model bị lỗi, hệ thống tự chuyển sang chế độ
 * fallback dựa trên dữ liệu catalog để chức năng luôn dùng được khi chấm bài offline.
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final Pattern PRICE_PATTERN =
            Pattern.compile("(duoi|<|toi da|khong qua|re hon)\\s*(\\d+(?:[.,]\\d+)?)\\s*(k|nghin|ngan|tr|trieu)?");
    private static final Set<String> STOP_WORDS = Set.of("toi", "minh", "muon", "can", "tim", "mua", "cho", "co",
            "khong", "nao", "gi", "la", "va", "voi", "cua", "mot", "cai", "san", "pham", "shop", "ban", "hoi",
            "duoi", "gia", "bao", "nhieu", "nghin", "trieu", "the", "nhung", "hay", "nhat", "loai", "goi", "y");

    private final ProductRepository productRepository;
    private final ChatClient chatClient;
    private final boolean enabled;

    public AiService(ProductRepository productRepository,
                     ObjectProvider<ChatClient.Builder> builderProvider,
                     @Value("${spring.ai.openai.api-key:none}") String apiKey) {
        this.productRepository = productRepository;
        ChatClient.Builder builder = builderProvider.getIfAvailable();
        this.enabled = builder != null && apiKey != null && !apiKey.isBlank() && !"none".equalsIgnoreCase(apiKey);
        this.chatClient = enabled ? builder.build() : null;
        log.info("AI integration mode: {}", enabled ? "LLM (Spring AI)" : "fallback (no AI_API_KEY)");
    }

    public boolean isLlmEnabled() {
        return enabled;
    }

    public DescriptionResponse generateDescription(String name, String category) {
        if (enabled) {
            try {
                String content = chatClient.prompt()
                        .system("Bạn là chuyên gia viết nội dung thương mại điện tử chuẩn SEO bằng tiếng Việt.")
                        .user(u -> u.text("""
                                Viết mô tả sản phẩm chuẩn SEO (khoảng 80-120 từ, 2 đoạn ngắn, không dùng markdown,
                                không bịa thông số kỹ thuật cụ thể) cho sản phẩm "{name}" thuộc danh mục "{category}".
                                Đoạn 1 nêu lợi ích chính, đoạn 2 gợi ý đối tượng phù hợp và lời kêu gọi mua hàng.
                                """)
                                .param("name", name)
                                .param("category", category == null || category.isBlank() ? "chung" : category))
                        .call()
                        .content();
                if (content != null && !content.isBlank()) {
                    return new DescriptionResponse(content.trim(), "llm");
                }
            } catch (Exception e) {
                log.warn("AI description failed, using fallback: {}", e.getMessage());
            }
        }
        return new DescriptionResponse(fallbackDescription(name, category), "fallback");
    }

    @Transactional(readOnly = true)
    public ChatResponse chat(String message) {
        List<Product> catalog = productRepository.findByActiveTrue();
        List<Product> matched = match(message, catalog);
        List<ProductResponse> suggestions = matched.stream().limit(4).map(ProductResponse::from).toList();

        if (enabled) {
            try {
                String context = (matched.isEmpty() ? catalog : matched).stream().limit(15)
                        .map(p -> "- #%d %s | giá %s đ | còn %d | danh mục %s".formatted(p.getId(), p.getName(),
                                formatPrice(p.getPrice()), p.getStock(),
                                p.getCategory() == null ? "khác" : p.getCategory().getName()))
                        .collect(Collectors.joining("\n"));
                String reply = chatClient.prompt()
                        .system("""
                                Bạn là trợ lý bán hàng của Rikkei QuickShop. Chỉ tư vấn dựa trên danh sách sản phẩm
                                được cung cấp, trả lời ngắn gọn bằng tiếng Việt, không bịa sản phẩm không có trong danh sách.
                                Thanh toán duy nhất là COD (nhận hàng trả tiền).""")
                        .user("Danh sách sản phẩm:\n" + context + "\n\nCâu hỏi của khách: " + message)
                        .call()
                        .content();
                if (reply != null && !reply.isBlank()) {
                    return new ChatResponse(reply.trim(), suggestions, "llm");
                }
            } catch (Exception e) {
                log.warn("AI chat failed, using fallback: {}", e.getMessage());
            }
        }
        return new ChatResponse(fallbackReply(message, matched), suggestions, "fallback");
    }

    // ---------------------------------------------------------------- fallback logic

    public static String fallbackDescription(String name, String category) {
        String cat = category == null || category.isBlank() ? "sản phẩm" : category.toLowerCase();
        return ("%s là lựa chọn nổi bật trong danh mục %s tại Rikkei QuickShop, được tuyển chọn với chất lượng ổn định, "
                + "thiết kế hiện đại và mức giá hợp lý. Sản phẩm đáp ứng tốt nhu cầu sử dụng hằng ngày, bền bỉ theo thời gian "
                + "và dễ dàng kết hợp với phong cách sống năng động.\n\n"
                + "Phù hợp cho người dùng đang tìm một %s đáng tin cậy để dùng cá nhân hoặc làm quà tặng. "
                + "Đặt hàng ngay hôm nay tại QuickShop để nhận hàng nhanh và thanh toán khi nhận hàng (COD).")
                .formatted(name.trim(), cat, cat);
    }

    static List<Product> match(String message, List<Product> catalog) {
        String q = normalize(message);
        BigDecimal maxPrice = extractMaxPrice(q);
        String withoutPrice = PRICE_PATTERN.matcher(q).replaceAll(" ");
        List<String> tokens = Arrays.stream(withoutPrice.split("[^a-z0-9]+"))
                .filter(t -> t.length() >= 2 && !STOP_WORDS.contains(t) && !t.chars().allMatch(Character::isDigit))
                .toList();

        Map<Product, Integer> scores = new LinkedHashMap<>();
        for (Product p : catalog) {
            String hay = normalize(p.getName() + " " + (p.getCategory() == null ? "" : p.getCategory().getName()));
            int score = 0;
            for (String t : tokens) {
                if (hay.contains(t)) {
                    score += 2;
                }
            }
            if (maxPrice != null) {
                if (p.getPrice().compareTo(maxPrice) > 0) {
                    continue;
                }
                score += tokens.isEmpty() ? 1 : 0;
            }
            if (score > 0 && p.getStock() > 0) {
                scores.put(p, score);
            }
        }
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed()
                        .thenComparing(e -> e.getKey().getPrice()))
                .map(Map.Entry::getKey)
                .toList();
    }

    static String fallbackReply(String message, List<Product> matched) {
        if (matched.isEmpty()) {
            return "Xin lỗi, mình chưa tìm thấy sản phẩm phù hợp với \"" + message.trim() + "\". "
                    + "Bạn thử nhập tên sản phẩm hoặc danh mục (ví dụ: tai nghe, áo thun, sách) "
                    + "hoặc khoảng giá (ví dụ: dưới 500k) nhé.";
        }
        String list = matched.stream().limit(4)
                .map(p -> "• " + p.getName() + " — " + formatPrice(p.getPrice()) + " đ (còn " + p.getStock() + ")")
                .collect(Collectors.joining("\n"));
        return "Mình gợi ý cho bạn " + Math.min(4, matched.size()) + " sản phẩm phù hợp:\n" + list
                + "\nBấm vào sản phẩm để xem chi tiết. Shop hỗ trợ thanh toán khi nhận hàng (COD).";
    }

    public static BigDecimal extractMaxPrice(String normalized) {
        Matcher m = PRICE_PATTERN.matcher(normalized);
        if (!m.find()) {
            return null;
        }
        BigDecimal value = new BigDecimal(m.group(2).replace(",", "."));
        String unit = m.group(3);
        if (unit == null) {
            return value.compareTo(BigDecimal.valueOf(1000)) < 0 ? value.multiply(BigDecimal.valueOf(1000)) : value;
        }
        return switch (unit) {
            case "tr", "trieu" -> value.multiply(BigDecimal.valueOf(1_000_000));
            default -> value.multiply(BigDecimal.valueOf(1000));
        };
    }

    public static String normalize(String s) {
        String n = Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd');
        return n.trim();
    }

    private static String formatPrice(BigDecimal price) {
        return String.format(Locale.US, "%,d", price.longValue()).replace(',', '.');
    }
}
