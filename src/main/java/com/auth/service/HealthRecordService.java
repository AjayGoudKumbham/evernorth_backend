package com.auth.service;

import com.auth.dto.HealthRecordRequest;
import com.auth.dto.HealthRecordResponse;
import com.auth.exception.ResourceNotFoundException;
import com.auth.model.HealthRecord;
import com.auth.repository.HealthRecordRepository;
import com.auth.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<HealthRecordResponse> getCurrentUserHealthRecords() {
        String memberId = securityUtils.getCurrentUserId();
        List<HealthRecord> healthRecords = healthRecordRepository.findByMemberIdOrderByRecordNoAsc(memberId);
        
        if (healthRecords.isEmpty()) {
            throw new ResourceNotFoundException("No health records found for the user");
        }
        
        return healthRecords.stream()
                .map(this::mapToHealthRecordResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public HealthRecordResponse addHealthRecord(HealthRecordRequest request) {
        String memberId = securityUtils.getCurrentUserId();
        
        Integer nextRecordNo = healthRecordRepository.findByMemberIdOrderByRecordNoAsc(memberId)
                .stream()
                .map(HealthRecord::getRecordNo)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        
        HealthRecord healthRecord = HealthRecord.builder()
                .memberId(memberId)
                .recordNo(nextRecordNo)
                .healthCondition(request.getHealthCondition())
                .description(request.getDescription())
                .build();
        
        HealthRecord savedRecord = healthRecordRepository.save(healthRecord);
        return mapToHealthRecordResponse(savedRecord);
    }
    
    private HealthRecordResponse mapToHealthRecordResponse(HealthRecord healthRecord) {
        return HealthRecordResponse.builder()
                .recordNo(healthRecord.getRecordNo())
                .healthCondition(healthRecord.getHealthCondition())
                .description(healthRecord.getDescription())
                .build();
    }
}