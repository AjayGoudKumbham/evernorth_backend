package com.auth.service;

import com.auth.dto.AddressRequest;
import com.auth.dto.AddressResponse;
import com.auth.exception.ResourceNotFoundException;
import com.auth.model.Address;
import com.auth.repository.AddressRepository;
import com.auth.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<AddressResponse> getCurrentUserAddresses() {
        String memberId = securityUtils.getCurrentUserId();
        List<Address> addresses = addressRepository.findByMemberId(memberId);
        
        if (addresses.isEmpty()) {
            throw new ResourceNotFoundException("No addresses found for the user");
        }
        
        return addresses.stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse addAddress(AddressRequest request) {
        String memberId = securityUtils.getCurrentUserId();
        
        Address address = Address.builder()
                .memberId(memberId)
                .addressLabel(request.getAddressLabel())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .landmark(request.getLandmark())
                .build();
        
        Address savedAddress = addressRepository.save(address);
        return mapToAddressResponse(savedAddress);
    }
    
    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .addressLabel(address.getAddressLabel())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .landmark(address.getLandmark())
                .build();
    }
}