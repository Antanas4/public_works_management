package org.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.request.PaginationRequest;
import org.handler.dto.response.CaseResponseDto;
import org.handler.dto.response.PaginationResponse;
import org.handler.dto.response.SupplierDto;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.CaseType;
import org.handler.service.CaseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {
    private final CaseService caseService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CaseResponseDto> createCase(@RequestPart("case") @Valid CaseRequestDto caseRequestDto,
                                                      @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        CaseResponseDto caseResponseDto = caseService.createCase(caseRequestDto, photos);
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

    @GetMapping("/count")
    public ResponseEntity<Long> getCaseCountForCurrentUser() {
        return ResponseEntity.ok(caseService.getCaseCountForCurrentUser());
    }

    @GetMapping("/user")
    public ResponseEntity<PaginationResponse<CaseResponseDto>> getUserCases(
            @ModelAttribute PaginationRequest paginationRequest,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) CaseType type) {

        PaginationResponse<CaseResponseDto> response = caseService.getUserCases(paginationRequest, status, type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/suggested-companies")
    public ResponseEntity<List<SupplierDto>> suggestCompaniesForCase(@PathVariable Long id) {
        return ResponseEntity.ok(caseService.suggestCompaniesForCase(id));
    }
}
