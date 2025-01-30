package com.auth.service;

import com.auth.dto.PaymentRequest;
import com.auth.dto.PaymentResponse;
import com.auth.exception.ResourceNotFoundException;
import com.auth.model.Payment;
import com.auth.repository.PaymentRepository;
import com.auth.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<PaymentResponse> getCurrentUserPayments() {
        String memberId = securityUtils.getCurrentUserId();
        List<Payment> payments = paymentRepository.findByMemberId(memberId);
        
        if (payments.isEmpty()) {
            throw new ResourceNotFoundException("No payment methods found for the user");
        }
        
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse addPayment(PaymentRequest request) {
        String memberId = securityUtils.getCurrentUserId();
        
        Payment payment = Payment.builder()
                .memberId(memberId)
                .paymentType(request.getPaymentType())
                .cardNumber(request.getCardNumber())
                .upiId(request.getUpiId())
                .nameOnCard(request.getNameOnCard())
                .expiryDate(request.getExpiryDate())
                .cardType(request.getCardType())
                .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        return mapToPaymentResponse(savedPayment);
    }
    
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentType(payment.getPaymentType())
                .maskedCardNumber(maskCardNumber(payment.getCardNumber()))
                .upiId(payment.getUpiId())
                .nameOnCard(payment.getNameOnCard())
                .expiryDate(payment.getExpiryDate())
                .cardType(payment.getCardType())
                .build();
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        int length = cardNumber.length();
        if (length <= 4) {
            return cardNumber;
        }
        return "*".repeat(length - 4) + cardNumber.substring(length - 4);
    }
}