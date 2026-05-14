package vn.chiendt.haimuoi3.shop.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.shop.dto.request.UpdateShopRequest;

import java.util.regex.Pattern;

@Component
public class UpdateShopValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$");

    public void validate(UpdateShopRequest request) {
        if (request.getShopName() != null && request.getShopName().length() > Constants.Shop.MAX_SHOP_NAME_LENGTH) {
            throw new IllegalArgumentException("Shop name must not exceed " + Constants.Shop.MAX_SHOP_NAME_LENGTH + " characters");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank() 
            && !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank() 
            && !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new IllegalArgumentException("Phone must be 10-11 digits");
        }
    }
}
