package vn.chiendt.haimuoi3.user.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.user.dto.request.CreateAddressRequest;
import vn.chiendt.haimuoi3.user.dto.request.UpdateAddressRequest;

import java.util.regex.Pattern;

@Component
public class AddressValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$");

    public void validateCreateAddress(CreateAddressRequest request) {
        if (request.getRecipientName() == null || request.getRecipientName().isBlank()) {
            throw new IllegalArgumentException("Recipient name is required");
        }

        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone is required");
        }

        if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new IllegalArgumentException("Phone must be 10-11 digits");
        }

        if (request.getProvince() == null || request.getProvince().isBlank()) {
            throw new IllegalArgumentException("Province is required");
        }

        if (request.getDistrict() == null || request.getDistrict().isBlank()) {
            throw new IllegalArgumentException("District is required");
        }

        if (request.getWard() == null || request.getWard().isBlank()) {
            throw new IllegalArgumentException("Ward is required");
        }

        if (request.getStreetAddress() == null || request.getStreetAddress().isBlank()) {
            throw new IllegalArgumentException("Street address is required");
        }

        if (request.getAddressName() != null && request.getAddressName().length() > Constants.Address.MAX_ADDRESS_NAME_LENGTH) {
            throw new IllegalArgumentException("Address name must not exceed " + Constants.Address.MAX_ADDRESS_NAME_LENGTH + " characters");
        }
    }

    public void validateUpdateAddress(UpdateAddressRequest request) {
        if (request.getRecipientName() == null || request.getRecipientName().isBlank()) {
            throw new IllegalArgumentException("Recipient name is required");
        }

        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone is required");
        }

        if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new IllegalArgumentException("Phone must be 10-11 digits");
        }

        if (request.getProvince() == null || request.getProvince().isBlank()) {
            throw new IllegalArgumentException("Province is required");
        }

        if (request.getDistrict() == null || request.getDistrict().isBlank()) {
            throw new IllegalArgumentException("District is required");
        }

        if (request.getWard() == null || request.getWard().isBlank()) {
            throw new IllegalArgumentException("Ward is required");
        }

        if (request.getStreetAddress() == null || request.getStreetAddress().isBlank()) {
            throw new IllegalArgumentException("Street address is required");
        }

        if (request.getAddressName() != null && request.getAddressName().length() > Constants.Address.MAX_ADDRESS_NAME_LENGTH) {
            throw new IllegalArgumentException("Address name must not exceed " + Constants.Address.MAX_ADDRESS_NAME_LENGTH + " characters");
        }
    }
}
