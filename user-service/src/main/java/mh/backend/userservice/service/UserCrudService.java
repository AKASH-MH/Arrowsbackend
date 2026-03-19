package mh.backend.userservice.service;

import mh.backend.userservice.dto.UserRequest;
import mh.backend.userservice.dto.UserResponse;
import mh.backend.userservice.entity.OrgUnit;
import mh.backend.userservice.entity.UserAccount;
import mh.backend.userservice.exception.ResourceNotFoundException;
import mh.backend.userservice.repository.OrgUnitRepository;
import mh.backend.userservice.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class UserCrudService {

    private final UserAccountRepository userAccountRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final TenantUserDirectoryService tenantUserDirectoryService;

    public UserCrudService(UserAccountRepository userAccountRepository,
                           OrgUnitRepository orgUnitRepository,
                           TenantUserDirectoryService tenantUserDirectoryService) {
        this.userAccountRepository = userAccountRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.tenantUserDirectoryService = tenantUserDirectoryService;
    }

    public UserResponse create(UserRequest request) {
        OrgUnit orgUnit = getRequiredOrgUnit(request.orgUnitId());
        UserAccount userAccount = new UserAccount();
        userAccount.setOrgUnit(orgUnit);
        userAccount.setFullName(normalize(request.fullName()));
        userAccount.setEmail(normalize(request.email()));
        userAccount.setStatus(normalizeStatus(request.status()));

        UserAccount saved = userAccountRepository.saveAndFlush(userAccount);
        tenantUserDirectoryService.sync(null, saved.getEmail(), saved.getStatus());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userAccountRepository.findAllDetailed()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID userId) {
        return toResponse(getRequiredUser(userId));
    }

    public UserResponse update(UUID userId, UserRequest request) {
        UserAccount userAccount = getRequiredUser(userId);
        String previousEmail = userAccount.getEmail();

        userAccount.setOrgUnit(getRequiredOrgUnit(request.orgUnitId()));
        userAccount.setFullName(normalize(request.fullName()));
        userAccount.setEmail(normalize(request.email()));
        userAccount.setStatus(normalizeStatus(request.status()));

        UserAccount saved = userAccountRepository.saveAndFlush(userAccount);
        tenantUserDirectoryService.sync(previousEmail, saved.getEmail(), saved.getStatus());
        return toResponse(saved);
    }

    public void delete(UUID userId) {
        UserAccount userAccount = getRequiredUser(userId);
        tenantUserDirectoryService.delete(userAccount.getEmail());
        userAccountRepository.delete(userAccount);
        userAccountRepository.flush();
    }

    private UserAccount getRequiredUser(UUID userId) {
        return userAccountRepository.findDetailedById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private OrgUnit getRequiredOrgUnit(UUID orgUnitId) {
        return orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new ResourceNotFoundException("Org unit not found: " + orgUnitId));
    }

    private UserResponse toResponse(UserAccount userAccount) {
        OrgUnit orgUnit = userAccount.getOrgUnit();
        return new UserResponse(
                userAccount.getUserId(),
                orgUnit == null ? null : orgUnit.getOrgUnitId(),
                orgUnit == null ? null : orgUnit.getOrgUnitName(),
                userAccount.getFullName(),
                userAccount.getEmail(),
                userAccount.getStatus(),
                userAccount.getCreatedAt()
        );
    }

    private String normalize(String value) {
        return value.strip();
    }

    private String normalizeStatus(String value) {
        return value.strip().toUpperCase(Locale.ROOT);
    }
}
