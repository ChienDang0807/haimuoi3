package vn.chiendt.haimuoi3.sysadmin.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateUserRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateUserRequest;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class SysadminUserValidatorTest {

    @InjectMocks
    private SysadminUserValidator validator;

    @Test
    void validateCreateUserRequest_shouldAcceptValidRequest() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .phone("1234567890")
                .password("password123")
                .build();

        // When/Then
        assertThatCode(() -> validator.validateCreateUserRequest(request))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreateUserRequest_shouldRejectNullRequest() {
        // When/Then
        assertThatThrownBy(() -> validator.validateCreateUserRequest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void validateCreateUserRequest_shouldRejectBlankEmail() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("")
                .fullName("Test User")
                .password("password123")
                .build();

        // When/Then
        assertThatThrownBy(() -> validator.validateCreateUserRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is required");
    }

    @Test
    void validateCreateUserRequest_shouldRejectInvalidEmailFormat() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("invalid-email")
                .fullName("Test User")
                .password("password123")
                .build();

        // When/Then
        assertThatThrownBy(() -> validator.validateCreateUserRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email format is invalid");
    }

    @Test
    void validateCreateUserRequest_shouldRejectBlankFullName() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .fullName("")
                .password("password123")
                .build();

        // When/Then
        assertThatThrownBy(() -> validator.validateCreateUserRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Full name is required");
    }

    @Test
    void validateCreateUserRequest_shouldRejectTooLongFullName() {
        // Given
        String longName = "a".repeat(101);
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .fullName(longName)
                .password("password123")
                .build();

        // When/Then
        assertThatThrownBy(() -> validator.validateCreateUserRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Full name must not exceed 100 characters");
    }

    @Test
    void validateCreateUserRequest_shouldRejectInvalidPhoneFormat() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .phone("123")
                .password("password123")
                .build();

        // When/Then
        assertThatThrownBy(() -> validator.validateCreateUserRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number must be 10-15 digits");
    }

    @Test
    void validateCreateUserRequest_shouldAcceptValidPhoneFormat() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .phone("1234567890")
                .password("password123")
                .build();

        // When/Then
        assertThatCode(() -> validator.validateCreateUserRequest(request))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreateUserRequest_shouldRejectBlankPassword() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("")
                .build();

        // When/Then
        assertThatThrownBy(() -> validator.validateCreateUserRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password is required");
    }

    @Test
    void validateCreateUserRequest_shouldRejectShortPassword() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("pass1")
                .build();

        // When/Then
        assertThatThrownBy(() -> validator.validateCreateUserRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must be at least");
    }

    @Test
    void validateCreateUserRequest_shouldRejectPasswordWithoutLetterAndNumber() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password")
                .build();

        // When/Then
        assertThatThrownBy(() -> validator.validateCreateUserRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must contain at least one letter and one number");
    }

    @Test
    void validateUpdateUserRequest_shouldAcceptValidRequest() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("Updated Name")
                .phone("1234567890")
                .build();

        // When/Then
        assertThatCode(() -> validator.validateUpdateUserRequest(request))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdateUserRequest_shouldRejectNullRequest() {
        // When/Then
        assertThatThrownBy(() -> validator.validateUpdateUserRequest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void validateUpdateUserRequest_shouldRejectTooLongFullName() {
        // Given
        String longName = "a".repeat(101);
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName(longName)
                .build();

        // When/Then
        assertThatThrownBy(() -> validator.validateUpdateUserRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Full name must not exceed 100 characters");
    }

    @Test
    void validateUpdateUserRequest_shouldRejectInvalidPhoneFormat() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .phone("123")
                .build();

        // When/Then
        assertThatThrownBy(() -> validator.validateUpdateUserRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number must be 10-15 digits");
    }

    @Test
    void validateUpdateUserRequest_shouldAcceptEmptyRequest() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder().build();

        // When/Then
        assertThatCode(() -> validator.validateUpdateUserRequest(request))
                .doesNotThrowAnyException();
    }
}
