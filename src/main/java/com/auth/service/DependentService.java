package com.auth.service;

import com.auth.dto.DependentRequest;
import com.auth.dto.DependentResponse;
import com.auth.exception.ResourceNotFoundException;
import com.auth.model.Dependent;
import com.auth.repository.DependentRepository;
import com.auth.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DependentService {

    private final DependentRepository dependentRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<DependentResponse> getCurrentUserDependents() {
        String memberId = securityUtils.getCurrentUserId();
        List<Dependent> dependents = dependentRepository.findByMemberId(memberId);
        
        if (dependents.isEmpty()) {
            throw new ResourceNotFoundException("No dependents found for the user");
        }
        
        return dependents.stream()
                .map(this::mapToDependentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DependentResponse addDependent(DependentRequest request) {
        String memberId = securityUtils.getCurrentUserId();
        
        Dependent dependent = Dependent.builder()
                .memberId(memberId)
                .fullName(request.getFullName())
                .relation(request.getRelation())
                .dob(request.getDob())
                .mobileNumber(request.getMobileNumber())
                .emailAddress(request.getEmailAddress())
                .emergencySosContact(request.getEmergencySosContact())
                .build();
        
        Dependent savedDependent = dependentRepository.save(dependent);
        return mapToDependentResponse(savedDependent);
    }
    
    private DependentResponse mapToDependentResponse(Dependent dependent) {
        return DependentResponse.builder()
                .fullName(dependent.getFullName())
                .relation(dependent.getRelation())
                .dob(dependent.getDob())
                .mobileNumber(dependent.getMobileNumber())
                .emailAddress(dependent.getEmailAddress())
                .emergencySosContact(dependent.getEmergencySosContact())
                .build();
    }
}