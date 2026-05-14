package vn.chiendt.haimuoi3.cart.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MergeCartResponse {

    boolean merged;

    int mergedItemsCount;

    List<String> notifications;
}
