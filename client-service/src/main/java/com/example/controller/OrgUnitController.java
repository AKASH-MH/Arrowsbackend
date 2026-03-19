package com.example.controller;

import com.example.dto.OrgUnitRequest;
import com.example.dto.OrgUnitResponse;
import com.example.service.OrgUnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/org-units")
public class OrgUnitController {

    private final OrgUnitService orgUnitService;

    public OrgUnitController(OrgUnitService orgUnitService) {
        this.orgUnitService = orgUnitService;
    }

    @PostMapping
    public ResponseEntity<OrgUnitResponse> createOrgUnit(@Valid @RequestBody OrgUnitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orgUnitService.create(request));
    }

    @GetMapping
    public List<OrgUnitResponse> getOrgUnits() {
        return orgUnitService.getAll();
    }

    @GetMapping("/{id}")
    public OrgUnitResponse getOrgUnit(@PathVariable UUID id) {
        return orgUnitService.getById(id);
    }

    @PutMapping("/{id}")
    public OrgUnitResponse updateOrgUnit(@PathVariable UUID id,
                                         @Valid @RequestBody OrgUnitRequest request) {
        return orgUnitService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrgUnit(@PathVariable UUID id) {
        orgUnitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
