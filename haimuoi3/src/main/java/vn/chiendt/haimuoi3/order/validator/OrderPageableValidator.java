package vn.chiendt.haimuoi3.order.validator;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.common.constants.Constants;

@Component
public class OrderPageableValidator {

    public void validate(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return;
        }
        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (pageable.getPageSize() < 1) {
            throw new IllegalArgumentException("size must be >= 1");
        }
        if (pageable.getPageSize() > Constants.Order.MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must not exceed " + Constants.Order.MAX_PAGE_SIZE);
        }
    }
}
