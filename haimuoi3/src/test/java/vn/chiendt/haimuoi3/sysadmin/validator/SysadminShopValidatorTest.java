package vn.chiendt.haimuoi3.sysadmin.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateShopRequest;

import static org.junit.jupiter.api.Assertions.*;

class SysadminShopValidatorTest {

    private SysadminShopValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SysadminShopValidator();
    }

    // CreateShopRequest Tests

    @Test
    void validateCreateShopRequest_ValidRequest_ShouldPass() {
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName("Test Shop")
                .slug("test-shop")
                .description("Test description")
                .email("test@example.com")
                .phone("0123456789")
                .build();

        assertDoesNotThrow(() -> validator.validateCreateShopRequest(request));
    }

    @Test
    void validateCreateShopRequest_NullRequest_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateShopRequest(null)
        );
        assertEquals("Create shop request cannot be null", exception.getMessage());
    }

    @Test
    void validateCreateShopRequest_NullShopName_ShouldThrowException() {
        CreateShopRequest request = CreateShopRequest.builder()
                .slug("test-shop")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateShopRequest(request)
        );
        assertEquals("Shop name is required", exception.getMessage());
    }

    @Test
    void validateCreateShopRequest_BlankShopName_ShouldThrowException() {
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName("   ")
                .slug("test-shop")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateShopRequest(request)
        );
        assertEquals("Shop name is required", exception.getMessage());
    }

    @Test
    void validateCreateShopRequest_ShopNameTooLong_ShouldThrowException() {
        String longName = "a".repeat(151);
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName(longName)
                .slug("test-shop")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateShopRequest(request)
        );
        assertTrue(exception.getMessage().contains("must not exceed"));
    }

    @Test
    void validateCreateShopRequest_NullSlug_ShouldThrowException() {
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName("Test Shop")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateShopRequest(request)
        );
        assertEquals("Slug is required", exception.getMessage());
    }

    @Test
    void validateCreateShopRequest_InvalidSlugWithUppercase_ShouldThrowException() {
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName("Test Shop")
                .slug("Test-Shop")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateShopRequest(request)
        );
        assertTrue(exception.getMessage().contains("lowercase"));
    }

    @Test
    void validateCreateShopRequest_InvalidSlugWithSpecialChars_ShouldThrowException() {
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName("Test Shop")
                .slug("test_shop!")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateShopRequest(request)
        );
        assertTrue(exception.getMessage().contains("alphanumeric with hyphens"));
    }

    @Test
    void validateCreateShopRequest_SlugTooShort_ShouldThrowException() {
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName("Test Shop")
                .slug("ab")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateShopRequest(request)
        );
        assertTrue(exception.getMessage().contains("3-50 characters"));
    }

    @Test
    void validateCreateShopRequest_InvalidEmail_ShouldThrowException() {
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName("Test Shop")
                .slug("test-shop")
                .email("invalid-email")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateShopRequest(request)
        );
        assertEquals("Email format is invalid", exception.getMessage());
    }

    @Test
    void validateCreateShopRequest_InvalidPhone_ShouldThrowException() {
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName("Test Shop")
                .slug("test-shop")
                .phone("123")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateShopRequest(request)
        );
        assertEquals("Phone number must be 10-15 digits", exception.getMessage());
    }

    @Test
    void validateCreateShopRequest_DescriptionTooLong_ShouldThrowException() {
        String longDescription = "a".repeat(1001);
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName("Test Shop")
                .slug("test-shop")
                .description(longDescription)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateShopRequest(request)
        );
        assertTrue(exception.getMessage().contains("Description must not exceed 1000 characters"));
    }

    // UpdateShopRequest Tests

    @Test
    void validateUpdateShopRequest_ValidRequest_ShouldPass() {
        UpdateShopRequest request = UpdateShopRequest.builder()
                .shopName("Updated Shop")
                .description("Updated description")
                .email("updated@example.com")
                .phone("0987654321")
                .build();

        assertDoesNotThrow(() -> validator.validateUpdateShopRequest(request));
    }

    @Test
    void validateUpdateShopRequest_NullRequest_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateUpdateShopRequest(null)
        );
        assertEquals("Update shop request cannot be null", exception.getMessage());
    }

    @Test
    void validateUpdateShopRequest_EmptyRequest_ShouldPass() {
        UpdateShopRequest request = UpdateShopRequest.builder().build();
        assertDoesNotThrow(() -> validator.validateUpdateShopRequest(request));
    }

    @Test
    void validateUpdateShopRequest_ShopNameTooLong_ShouldThrowException() {
        String longName = "a".repeat(151);
        UpdateShopRequest request = UpdateShopRequest.builder()
                .shopName(longName)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateUpdateShopRequest(request)
        );
        assertTrue(exception.getMessage().contains("must not exceed"));
    }

    @Test
    void validateUpdateShopRequest_InvalidEmail_ShouldThrowException() {
        UpdateShopRequest request = UpdateShopRequest.builder()
                .email("invalid-email")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateUpdateShopRequest(request)
        );
        assertEquals("Email format is invalid", exception.getMessage());
    }

    @Test
    void validateUpdateShopRequest_InvalidPhone_ShouldThrowException() {
        UpdateShopRequest request = UpdateShopRequest.builder()
                .phone("123")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateUpdateShopRequest(request)
        );
        assertEquals("Phone number must be 10-15 digits", exception.getMessage());
    }
}
