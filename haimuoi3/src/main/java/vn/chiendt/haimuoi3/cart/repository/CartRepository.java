package vn.chiendt.haimuoi3.cart.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.chiendt.haimuoi3.cart.model.mongo.CartEntity;
import vn.chiendt.haimuoi3.cart.model.mongo.CartState;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends MongoRepository<CartEntity, String> {

    Optional<CartEntity> findByCartTokenAndState(String cartToken, CartState state);

    Optional<CartEntity> findByUserIdAndState(String userId, CartState state);

    List<CartEntity> findByStateAndExpiresAtBefore(CartState state, Instant instant);
}
