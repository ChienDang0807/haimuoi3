package vn.chiendt.haimuoi3.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.user.dto.request.CreateAddressRequest;
import vn.chiendt.haimuoi3.user.dto.request.UpdateAddressRequest;
import vn.chiendt.haimuoi3.user.dto.response.AddressResponse;
import vn.chiendt.haimuoi3.user.mapper.AddressMapper;
import vn.chiendt.haimuoi3.user.model.postgres.AddressEntity;
import vn.chiendt.haimuoi3.user.repository.AddressRepository;
import vn.chiendt.haimuoi3.user.service.AddressService;
import vn.chiendt.haimuoi3.user.validator.AddressValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressValidator addressValidator;
    private final AddressMapper addressMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses(Long userId) {
        List<AddressEntity> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse createAddress(Long userId, CreateAddressRequest request) {
        addressValidator.validateCreateAddress(request);

        AddressEntity address = AddressEntity.builder()
                .userId(userId)
                .addressName(request.getAddressName())
                .recipientName(request.getRecipientName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .streetAddress(request.getStreetAddress())
                .isDefault(false)
                .build();

        AddressEntity savedAddress = addressRepository.save(address);
        return addressMapper.toResponse(savedAddress);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        addressValidator.validateUpdateAddress(request);

        AddressEntity address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        address.setAddressName(request.getAddressName());
        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setStreetAddress(request.getStreetAddress());

        AddressEntity updatedAddress = addressRepository.save(address);
        return addressMapper.toResponse(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        AddressEntity address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        AddressEntity address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        List<AddressEntity> userAddresses = addressRepository.findByUserId(userId);
        for (AddressEntity addr : userAddresses) {
            if (addr.getIsDefault()) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }

        address.setIsDefault(true);
        addressRepository.save(address);
    }
}
