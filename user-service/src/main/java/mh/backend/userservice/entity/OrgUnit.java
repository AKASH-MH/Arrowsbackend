package mh.backend.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "org_unit")
public class OrgUnit {

    @Id
    @Column(name = "org_unit_id", nullable = false, updatable = false)
    private UUID orgUnitId;

    @Column(name = "org_unit_name", nullable = false)
    private String orgUnitName;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UUID getOrgUnitId() {
        return orgUnitId;
    }

    public String getOrgUnitName() {
        return orgUnitName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
