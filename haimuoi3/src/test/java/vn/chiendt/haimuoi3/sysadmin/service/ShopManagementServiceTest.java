package vn.chiendt.haimuoi3.sysadmin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopEntity;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopStatus;
import vn.chiendt.haimuoi3.shop.repository.ShopRepository;
import vn.chiendt.haimuoi3.sysadmin.dto.request.AssignOwnerRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopListResponse;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.sysadmin.mapper.SysadminShopMapper;
import vn.chiendt.haimuoi3.sysadmin.service.impl.ShopManagementServiceImpl;
import vn.chiendt.haimuoi3.sysadmin.validator.SysadminShopValidator;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserRole;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopManagementServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SysadminShopMapper shopMapper;

    @Mock
    private SysadminShopValidator shopValidator;

    @InjectMocks
    private ShopManagementServiceImpl shopManagementService;

    private ShopEntity testShop;
    private UserEntity testOwner;
    private CreateShopRequest createRequest;
    private UpdateShopRequest updateRequest;
    private ShopResponse shopResponse;

    @BeforeEach
    void setUp() {
        testShop = ShopEntity.builder()
                .id(1L)
                .shopName("Test Shop")
                .slug("test-shop")
                .status(ShopStatus.ACTIVE)
                .build();

        testOwner = UserEntity.builder()
                .id(1L)
                .email("owner@example.com")
                .fullName("Test Owner")
                .role(UserRole.SHOP_OWNER)
                .build();

        createRequest = CreateShopRequest.builder()
                .shopName("New Shop")
                .slug("new-shop")
                .build();

        updateRequest = UpdateShopRequest.builder()
                .shopName("Updated Shop")
                .build();

        shopResponse = ShopResponse.builder()
                .id(1L)
                .shopName("Test Shop")
                .slug("test-shop")
                .build();
    }

    @Test
    void createShop_ValidRequest_ShouldCreateShop() {
        // Given
        when(shopRepository.findBySlug(createRequest.getSlug())).thenReturn(Optional.empty());
        when(shopMapper.toShopEntity(createRequest)).thenReturn(testShop);
        when(shopRepository.save(any(ShopEntity.class))).thenReturn(testShop);
        when(shopMapper.toShopResponse(testShop, null)).thenReturn(shopResponse);

        // When
        ShopResponse result = shopManagementService.createShop(createRequest);

        // Then
        assertNotNull(result);
        verify(shopValidator).validateCreateShopRequest(createRequest);
        verify(shopRepository).findBySlug(createRequest.getSlug());
        verify(shopRepository).save(any(ShopEntity.class));
    }

    @Test
    void createShop_DuplicateSlug_ShouldThrowException() {
        // Given
        when(shopRepository.findBySlug(createRequest.getSlug())).thenReturn(Optional.of(testShop));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shopManagementService.createShop(createRequest)
        );
        assertTrue(exception.getMessage().contains("already exists"));
        verify(shopRepository, never()).save(any());
    }

    @Test
    void findAllShops_ShouldReturnPaginatedShops() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ShopEntity> shopPage = new PageImpl<>(List.of(testShop));
        when(shopRepository.findAll(pageable)).thenReturn(shopPage);
        when(shopMapper.toShopResponse(testShop, null)).thenReturn(shopResponse);

        // When
        ShopListResponse result = shopManagementService.findAllShops(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getShops().size());
        assertEquals(0, result.getCurrentPage());
        assertEquals(1, result.getTotalPages());
        verify(shopRepository).findAll(pageable);
    }

    @Test
    void getShopById_ExistingShop_ShouldReturnShop() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(shopMapper.toShopResponse(testShop, null)).thenReturn(shopResponse);

        // When
        ShopResponse result = shopManagementService.getShopById(1L);

        // Then
        assertNotNull(result);
        verify(shopRepository).findById(1L);
    }

    @Test
    void getShopById_NonExistingShop_ShouldThrowException() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> shopManagementService.getShopById(1L));
    }

    @Test
    void updateShop_ValidRequest_ShouldUpdateShop() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(shopRepository.save(testShop)).thenReturn(testShop);
        when(shopMapper.toShopResponse(testShop, null)).thenReturn(shopResponse);

        // When
        ShopResponse result = shopManagementService.updateShop(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(shopValidator).validateUpdateShopRequest(updateRequest);
        verify(shopMapper).updateShopEntity(updateRequest, testShop);
        verify(shopRepository).save(testShop);
    }

    @Test
    void deleteShop_ShopWithoutProducts_ShouldDeleteShop() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(productRepository.findByShopId(anyString(), any(Pageable.class)))
                .thenReturn(Page.empty());

        // When
        shopManagementService.deleteShop(1L);

        // Then
        verify(shopRepository).delete(testShop);
    }

    @Test
    void deleteShop_ShopWithProducts_ShouldThrowException() {
        // Given
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(productRepository.findByShopId(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(null)));

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> shopManagementService.deleteShop(1L)
        );
        assertTrue(exception.getMessage().contains("Cannot delete shop with existing products"));
        verify(shopRepository, never()).delete(any());
    }

    @Test
    void assignOwner_ValidRequest_ShouldAssignOwner() {
        // Given
        AssignOwnerRequest request = AssignOwnerRequest.builder().userId(1L).build();
        testShop.setOwnerId(null);
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(shopRepository.findByOwnerId(1L)).thenReturn(Optional.empty());
        when(shopRepository.save(testShop)).thenReturn(testShop);
        when(shopMapper.toShopResponse(testShop, testOwner)).thenReturn(shopResponse);

        // When
        ShopResponse result = shopManagementService.assignOwner(1L, request);

        // Then
        assertNotNull(result);
        verify(shopRepository).save(testShop);
        assertEquals(1L, testShop.getOwnerId());
    }

    @Test
    void assignOwner_ShopAlreadyHasOwner_ShouldThrowException() {
        // Given
        AssignOwnerRequest request = AssignOwnerRequest.builder().userId(1L).build();
        testShop.setOwnerId(2L);
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> shopManagementService.assignOwner(1L, request)
        );
        assertTrue(exception.getMessage().contains("already has an owner"));
    }

    @Test
    void assignOwner_UserNotShopOwner_ShouldThrowException() {
        // Given
        AssignOwnerRequest request = AssignOwnerRequest.builder().userId(1L).build();
        testShop.setOwnerId(null);
        UserEntity customer = UserEntity.builder()
                .id(1L)
                .role(UserRole.CUSTOMER)
                .build();
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shopManagementService.assignOwner(1L, request)
        );
        assertTrue(exception.getMessage().contains("SHOP_OWNER role"));
    }

    @Test
    void changeOwner_ValidRequest_ShouldChangeOwner() {
        // Given
        AssignOwnerRequest request = AssignOwnerRequest.builder().userId(2L).build();
        testShop.setOwnerId(1L);
        UserEntity newOwner = UserEntity.builder()
                .id(2L)
                .role(UserRole.SHOP_OWNER)
                .build();
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newOwner));
        when(shopRepository.findByOwnerId(2L)).thenReturn(Optional.empty());
        when(shopRepository.save(testShop)).thenReturn(testShop);
        when(shopMapper.toShopResponse(testShop, newOwner)).thenReturn(shopResponse);

        // When
        ShopResponse result = shopManagementService.changeOwner(1L, request);

        // Then
        assertNotNull(result);
        verify(shopRepository).save(testShop);
        assertEquals(2L, testShop.getOwnerId());
    }

    @Test
    void removeOwner_ValidRequest_ShouldRemoveOwner() {
        // Given
        testShop.setOwnerId(1L);
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(shopRepository.save(testShop)).thenReturn(testShop);
        when(shopMapper.toShopResponse(testShop, null)).thenReturn(shopResponse);

        // When
        ShopResponse result = shopManagementService.removeOwner(1L);

        // Then
        assertNotNull(result);
        assertNull(testShop.getOwnerId());
        verify(shopRepository).save(testShop);
    }

    @Test
    void removeOwner_ShopWithoutOwner_ShouldThrowException() {
        // Given
        testShop.setOwnerId(null);
        when(shopRepository.findById(1L)).thenReturn(Optional.of(testShop));

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> shopManagementService.removeOwner(1L)
        );
        assertTrue(exception.getMessage().contains("does not have an owner"));
    }
}
