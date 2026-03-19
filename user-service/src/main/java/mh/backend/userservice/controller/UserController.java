package mh.backend.userservice.controller;

import jakarta.validation.Valid;
import mh.backend.userservice.dto.UserRequest;
import mh.backend.userservice.dto.UserResponse;
import mh.backend.userservice.service.UserCrudService;
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
@RequestMapping("/api/users")
public class UserController {

    private final UserCrudService userCrudService;

    public UserController(UserCrudService userCrudService) {
        this.userCrudService = userCrudService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userCrudService.create(request));
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userCrudService.getAll();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return userCrudService.getById(id);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable UUID id,
                                   @Valid @RequestBody UserRequest request) {
        return userCrudService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userCrudService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
