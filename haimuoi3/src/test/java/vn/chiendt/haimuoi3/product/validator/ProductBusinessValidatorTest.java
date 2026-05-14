package vn.chiendt.haimuoi3.product.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ProductBusinessValidatorTest {

    @Mock
    private ProductParentSkuValidator productParentSkuValidator;

    @InjectMocks
    private ProductBusinessValidator validator;

    @Test
    void validateGlobalFilter_acceptsValidPriceRange() {
        assertThatCode(() -> validator.validateGlobalFilter(
                "watch",
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                4.0
        )).doesNotThrowAnyException();
    }

    @Test
    void validateGlobalFilter_rejectsInvalidPriceRange() {
        assertThatThrownBy(() -> validator.validateGlobalFilter(
                null,
                BigDecimal.valueOf(120),
                BigDecimal.valueOf(100),
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minPrice");
    }

    @Test
    void validateGlobalFilter_rejectsOutOfBoundRating() {
        assertThatThrownBy(() -> validator.validateGlobalFilter(
                null,
                null,
                null,
                5.5
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minRating");
    }
}
