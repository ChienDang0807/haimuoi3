package vn.chiendt.haimuoi3.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.user.dto.request.CreateAddressRequest;
import vn.chiendt.haimuoi3.user.dto.request.UpdateAddressRequest;
import vn.chiendt.haimuoi3.user.dto.response.AddressResponse;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.service.AddressService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ApiResponse<List<AddressResponse>> listAddresses(
            @AuthenticationPrincipal UserEntity currentUser) {
        List<AddressResponse> addresses = addressService.listAddresses(currentUser.getId());
        return ApiResponse.success(addresses, "Addresses retrieved successfully");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressResponse> createAddress(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestBody CreateAddressRequest request) {
        AddressResponse address = addressService.createAddress(currentUser.getId(), request);
        return ApiResponse.success(address, "Address created successfully");
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long id,
            @RequestBody UpdateAddressRequest request) {
        AddressResponse address = addressService.updateAddress(currentUser.getId(), id, request);
        return ApiResponse.success(address, "Address updated successfully");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteAddress(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long id) {
        addressService.deleteAddress(currentUser.getId(), id);
        return ApiResponse.success(null, "Address deleted successfully");
    }

    @PutMapping("/{id}/set-default")
    public ApiResponse<Void> setDefaultAddress(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long id) {
        addressService.setDefaultAddress(currentUser.getId(), id);
        return ApiResponse.success(null, "Default address set successfully");
    }
}
