package org.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.request.PaginationRequest;
import org.handler.dto.response.CaseResponseDto;
import org.handler.dto.response.PaginationResponse;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.CaseType;
import org.handler.service.CaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {
    private final CaseService caseService;

    @PostMapping
    public ResponseEntity<CaseResponseDto> createCase(@Valid @RequestBody CaseRequestDto caseRequestDto) {
        CaseResponseDto caseResponseDto = caseService.createCase(caseRequestDto);
        return ResponseEntity.ok(caseResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseResponseDto> getCaseById(@PathVariable Long id) {
        CaseResponseDto caseResponseDto = caseService.getCaseById(id);
        return ResponseEntity.ok(caseResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<CaseResponseDto>> getAllCases() {
        List<CaseResponseDto> caseResponseDtos = caseService.getAllCases();
        return ResponseEntity.ok(caseResponseDtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCase(@PathVariable Long id) {
        caseService.deleteCase(id);
        return ResponseEntity.ok("Case deleted successfully");
    }

    @GetMapping("/count/{userId}")
    public ResponseEntity<Long> getCaseCountByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(caseService.getCaseCountByUserId(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PaginationResponse<CaseResponseDto>> getCasesByUserId(
            @PathVariable Long userId,
            @ModelAttribute PaginationRequest paginationRequest,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) CaseType type) {

        PaginationResponse<CaseResponseDto> response = caseService.getCasesByUserId(userId, paginationRequest, status, type);
        return ResponseEntity.ok(response);
    }
}
