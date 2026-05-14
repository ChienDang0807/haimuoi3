package vn.chiendt.haimuoi3.shop.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop_categories", indexes = {
        @Index(name = "idx_shop_global", columnList = "shop_id, global_category_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "shop_category_id")
    private String shopCategoryId;

    @Column(name = "shop_id", nullable = false)
    private String shopId;

    @Column(name = "global_category_id")
    private String globalCategoryId;

    private String name;

    @Column(unique = true)
    private String slug;

    private String imageUrl;

    private Integer displayOrder;

    private boolean isActive = true;
}
