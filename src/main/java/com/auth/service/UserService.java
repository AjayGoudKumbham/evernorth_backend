package com.auth.service;

import com.auth.dto.UserProfileResponse;
import com.auth.exception.ResourceNotFoundException;
import com.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final MemberRepository memberRepository;

    public UserProfileResponse getCurrentUserProfile() {
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        return memberRepository.findById(memberId)
                .map(member -> UserProfileResponse.builder()
                        .memberId(member.getMemberId())
                        .fullName(member.getFullName())
                        .email(member.getEmail())
                        .contact(member.getContact())
                        .dob(member.getDob())
                        .createdAt(member.getCreatedAt())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}