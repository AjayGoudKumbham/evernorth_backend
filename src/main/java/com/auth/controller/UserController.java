package com.auth.controller;

import com.auth.dto.*;
import com.auth.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PaymentService paymentService;
    private final AddressService addressService;
    private final DependentService dependentService;
    private final HealthRecordService healthRecordService;
    private final AllergyRecordService allergyRecordService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>> getCurrentUserPayments() {
        return ResponseEntity.ok(paymentService.getCurrentUserPayments());
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> addPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.addPayment(request));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>> getCurrentUserAddresses() {
        return ResponseEntity.ok(addressService.getCurrentUserAddresses());
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> addAddress(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.addAddress(request));
    }

    @GetMapping("/dependents")
    public ResponseEntity<List<DependentResponse>> getCurrentUserDependents() {
        return ResponseEntity.ok(dependentService.getCurrentUserDependents());
    }

    @PostMapping("/dependents")
    public ResponseEntity<DependentResponse> addDependent(@Valid @RequestBody DependentRequest request) {
        return ResponseEntity.ok(dependentService.addDependent(request));
    }

    @GetMapping("/health-records")
    public ResponseEntity<List<HealthRecordResponse>> getCurrentUserHealthRecords() {
        return ResponseEntity.ok(healthRecordService.getCurrentUserHealthRecords());
    }

    @PostMapping("/health-records")
    public ResponseEntity<HealthRecordResponse> addHealthRecord(@Valid @RequestBody HealthRecordRequest request) {
        return ResponseEntity.ok(healthRecordService.addHealthRecord(request));
    }

    @GetMapping("/allergy-records")
    public ResponseEntity<List<AllergyRecordResponse>> getCurrentUserAllergyRecords() {
        return ResponseEntity.ok(allergyRecordService.getCurrentUserAllergyRecords());
    }

    @PostMapping("/allergy-records")
    public ResponseEntity<AllergyRecordResponse> addAllergyRecord(@Valid @RequestBody AllergyRecordRequest request) {
        return ResponseEntity.ok(allergyRecordService.addAllergyRecord(request));
    }
}