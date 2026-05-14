package vn.chiendt.haimuoi3.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.chiendt.haimuoi3.product.model.postgres.GlobalCategoryEntity;


public interface GlobalCategoryRepository extends JpaRepository<GlobalCategoryEntity, String> {

    boolean existsBySlug(String slug);

    Page<GlobalCategoryEntity> findByIsActiveTrue(Pageable pageable);

    //Khi bạn dùng @Modifying, có một vấn đề là Hibernate không hề biết dữ liệu trong DB đã thay đổi.
    // trong cùng một Transaction đó, trước đó bạn đã lỡ gọi findById(id), thì cái Object đó vẫn mang imageUrl cũ.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE GlobalCategoryEntity c SET c.imageUrl = :imageUrl WHERE c.globalCategoryId = :id")
    int updateImageUrl(@Param("id") String id, @Param("imageUrl") String imageUrl);
}
