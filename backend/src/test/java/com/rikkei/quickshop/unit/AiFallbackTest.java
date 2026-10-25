package com.rikkei.quickshop.unit;

import com.rikkei.quickshop.ai.AiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AiFallbackTest {

    @Test
    @DisplayName("TC-UNIT-02: Chuẩn hóa tiếng Việt có dấu về không dấu")
    void normalize() {
        assertThat(AiService.normalize("Tai Nghe Điện Tử")).isEqualTo("tai nghe dien tu");
    }

    @Test
    @DisplayName("TC-UNIT-03: Nhận diện khoảng giá trong câu hỏi")
    void extractPrice() {
        assertThat(AiService.extractMaxPrice("san pham duoi 500k")).isEqualByComparingTo(BigDecimal.valueOf(500_000));
        assertThat(AiService.extractMaxPrice("tai nghe duoi 1 trieu")).isEqualByComparingTo(BigDecimal.valueOf(1_000_000));
        assertThat(AiService.extractMaxPrice("ao thun gia re")).isNull();
    }

    @Test
    @DisplayName("TC-UNIT-04: Mô tả fallback chứa tên sản phẩm và lời kêu gọi mua hàng")
    void fallbackDescription() {
        String text = AiService.fallbackDescription("Bàn phím cơ K2", "Điện tử");
        assertThat(text).contains("Bàn phím cơ K2").contains("COD");
    }
}
