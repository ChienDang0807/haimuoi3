package vn.chiendt.haimuoi3.user.service;

import vn.chiendt.haimuoi3.user.dto.request.CreateAddressRequest;
import vn.chiendt.haimuoi3.user.dto.request.UpdateAddressRequest;
import vn.chiendt.haimuoi3.user.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {

    List<AddressResponse> listAddresses(Long userId);

    AddressResponse createAddress(Long userId, CreateAddressRequest request);

    AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request);

    void deleteAddress(Long userId, Long addressId);

    void setDefaultAddress(Long userId, Long addressId);
}
