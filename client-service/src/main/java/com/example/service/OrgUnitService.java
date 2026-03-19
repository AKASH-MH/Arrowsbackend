package com.example.service;

import com.example.dto.OrgUnitRequest;
import com.example.dto.OrgUnitResponse;
import com.example.entity.OrgUnit;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.OrgUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class OrgUnitService {

    private final OrgUnitRepository orgUnitRepository;

    public OrgUnitService(OrgUnitRepository orgUnitRepository) {
        this.orgUnitRepository = orgUnitRepository;
    }

    public OrgUnitResponse create(OrgUnitRequest request) {
        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setOrgUnitName(normalize(request.orgUnitName()));
        orgUnit.setStatus(normalizeStatus(request.status()));

        return toResponse(orgUnitRepository.saveAndFlush(orgUnit));
    }

    @Transactional(readOnly = true)
    public List<OrgUnitResponse> getAll() {
        return orgUnitRepository.findAllByOrderByOrgUnitNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrgUnitResponse getById(UUID orgUnitId) {
        return toResponse(getRequiredOrgUnit(orgUnitId));
    }

    public OrgUnitResponse update(UUID orgUnitId, OrgUnitRequest request) {
        OrgUnit orgUnit = getRequiredOrgUnit(orgUnitId);
        orgUnit.setOrgUnitName(normalize(request.orgUnitName()));
        orgUnit.setStatus(normalizeStatus(request.status()));
        return toResponse(orgUnitRepository.saveAndFlush(orgUnit));
    }

    public void delete(UUID orgUnitId) {
        OrgUnit orgUnit = getRequiredOrgUnit(orgUnitId);
        orgUnitRepository.delete(orgUnit);
        orgUnitRepository.flush();
    }

    private OrgUnit getRequiredOrgUnit(UUID orgUnitId) {
        return orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new ResourceNotFoundException("Org unit not found: " + orgUnitId));
    }

    private OrgUnitResponse toResponse(OrgUnit orgUnit) {
        return new OrgUnitResponse(
                orgUnit.getOrgUnitId(),
                orgUnit.getOrgUnitName(),
                orgUnit.getStatus(),
                orgUnit.getCreatedAt()
        );
    }

    private String normalize(String name) {
        return name.strip();
    }

    private String normalizeStatus(String value) {
        return value.strip().toUpperCase(Locale.ROOT);
    }
}
