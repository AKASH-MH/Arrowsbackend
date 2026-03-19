package com.example.controller;

import com.example.dto.ClientRequest;
import com.example.dto.ClientResponse;
import com.example.service.ClientCrudService;
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
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientCrudService clientCrudService;

    public ClientController(ClientCrudService clientCrudService) {
        this.clientCrudService = clientCrudService;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientCrudService.create(request));
    }

    @GetMapping
    public List<ClientResponse> getClients() {
        return clientCrudService.getAll();
    }

    @GetMapping("/{id}")
    public ClientResponse getClient(@PathVariable UUID id) {
        return clientCrudService.getById(id);
    }

    @PutMapping("/{id}")
    public ClientResponse updateClient(@PathVariable UUID id,
                                       @Valid @RequestBody ClientRequest request) {
        return clientCrudService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        clientCrudService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
