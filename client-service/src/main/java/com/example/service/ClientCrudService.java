package com.example.service;

import com.example.dto.ClientRequest;
import com.example.dto.ClientResponse;
import com.example.entity.Client;
import com.example.entity.OrgUnit;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.ClientRepository;
import com.example.repository.OrgUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class ClientCrudService {

    private final ClientRepository clientRepository;
    private final OrgUnitRepository orgUnitRepository;

    public ClientCrudService(ClientRepository clientRepository, OrgUnitRepository orgUnitRepository) {
        this.clientRepository = clientRepository;
        this.orgUnitRepository = orgUnitRepository;
    }

    public ClientResponse create(ClientRequest request) {
        OrgUnit orgUnit = getRequiredOrgUnit(request.orgUnitId());
        Client client = new Client();
        client.setOrgUnit(orgUnit);
        client.setClientName(normalize(request.clientName()));
        client.setIndustry(normalizeNullable(request.industry()));
        client.setStatus(normalizeStatus(request.status()));

        return toResponse(clientRepository.saveAndFlush(client));
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getAll() {
        return clientRepository.findAllWithOrgUnit()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse getById(UUID clientId) {
        return toResponse(getRequiredClient(clientId));
    }

    public ClientResponse update(UUID clientId, ClientRequest request) {
        Client client = getRequiredClient(clientId);
        client.setOrgUnit(getRequiredOrgUnit(request.orgUnitId()));
        client.setClientName(normalize(request.clientName()));
        client.setIndustry(normalizeNullable(request.industry()));
        client.setStatus(normalizeStatus(request.status()));

        return toResponse(clientRepository.saveAndFlush(client));
    }

    public void delete(UUID clientId) {
        Client client = getRequiredClient(clientId);
        clientRepository.delete(client);
        clientRepository.flush();
    }

    private Client getRequiredClient(UUID clientId) {
        return clientRepository.findDetailedById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
    }

    private OrgUnit getRequiredOrgUnit(UUID orgUnitId) {
        return orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new ResourceNotFoundException("Org unit not found: " + orgUnitId));
    }

    private ClientResponse toResponse(Client client) {
        OrgUnit orgUnit = client.getOrgUnit();
        return new ClientResponse(
                client.getClientId(),
                orgUnit == null ? null : orgUnit.getOrgUnitId(),
                orgUnit == null ? null : orgUnit.getOrgUnitName(),
                client.getClientName(),
                client.getIndustry(),
                client.getStatus(),
                client.getCreatedAt()
        );
    }

    private String normalize(String value) {
        return value.strip();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }

    private String normalizeStatus(String value) {
        return value.strip().toUpperCase(Locale.ROOT);
    }
}
