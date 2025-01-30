package com.auth.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.dto.AuthenticationRequest;
import com.auth.dto.AuthenticationResponse;
import com.auth.dto.LoginRequest;
import com.auth.dto.RegisterRequest;
import com.auth.exception.InvalidCredentialsException;
import com.auth.exception.OtpValidationException;
import com.auth.exception.ResourceNotFoundException;
import com.auth.model.EmailVerification;
import com.auth.model.Member;
import com.auth.repository.EmailVerificationRepository;
import com.auth.repository.MemberRepository;
import com.auth.security.JwtService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final MemberIdGenerator memberIdGenerator;

    @Transactional
    public void register(RegisterRequest request) throws MessagingException {
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidCredentialsException("Email already registered");
        }

        String otp = generateOtp();

        var verification = EmailVerification.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .contact(request.getContact())
                .dob(request.getDob())
                .otp(passwordEncoder.encode(otp))
                .otpExpiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        
        emailVerificationRepository.save(verification);
        emailService.sendVerificationEmail(verification.getEmail(), otp);
    }

    @Transactional
    public void verifyEmail(AuthenticationRequest request) {
        var verification = emailVerificationRepository.findById(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Registration request not found"));

        if (verification.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            emailVerificationRepository.delete(verification);
            throw new OtpValidationException("OTP has expired. Please register again.");
        }

        if (!passwordEncoder.matches(request.getOtp(), verification.getOtp())) {
            throw new OtpValidationException("Invalid OTP");
        }

        // Generate member ID and create new member
        String memberId = memberIdGenerator.generateMemberId(verification.getFullName(), verification.getDob());
        
        var member = Member.builder()
                .memberId(memberId)
                .fullName(verification.getFullName())
                .email(verification.getEmail())
                .contact(verification.getContact())
                .dob(verification.getDob())
                .build();
        
        memberRepository.save(member);
        emailVerificationRepository.delete(verification);
        
        try {
            emailService.sendWelcomeEmail(member.getEmail(), member.getFullName());
        } catch (MessagingException e) {
            // Log the error but don't fail the verification
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    @Transactional
    public void sendOtp(LoginRequest request) throws MessagingException {
    var member = memberRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

    // Generate OTP and store it as plain text
    String otp = generateOtp();
    member.setOtp(otp);  // Store OTP as plain text (no encoding)
    member.setOtpExpiryTime(LocalDateTime.now().plusMinutes(1));
    memberRepository.save(member);

    emailService.sendOtpEmail(member.getEmail(), otp);
    }


    @Transactional
    public AuthenticationResponse verifyOtp(AuthenticationRequest request) {
    // Retrieve member by email instead of memberId
    var member = memberRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

    // Check if OTP has expired
    if (member.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
        throw new OtpValidationException("OTP has expired");
    }

    // Validate the OTP entered by the user (no hashing, just direct comparison)
    if (!request.getOtp().equals(member.getOtp())) {
        throw new OtpValidationException("Invalid OTP");
    }

    // Clear OTP and expiry time after successful verification
    member.setOtp(null);
    member.setOtpExpiryTime(null);
    memberRepository.save(member);

    // Generate and return the JWT token
    String token = jwtService.generateToken(member.getMemberId());

    return AuthenticationResponse.builder()
            .token(token)
            .build();
    }

    @Transactional
    public void logout(String token) {
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);
        tokenBlacklistService.blacklistToken(token, expiryDate);
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}