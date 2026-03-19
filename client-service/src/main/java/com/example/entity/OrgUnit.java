package com.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "org_unit",
        uniqueConstraints = @UniqueConstraint(name = "uk_org_unit_name", columnNames = "org_unit_name")
)
public class OrgUnit {

    @Id
    @UuidGenerator
    @Column(name = "org_unit_id", updatable = false, nullable = false)
    private UUID orgUnitId;

    @Column(name = "org_unit_name", nullable = false, length = 255)
    private String orgUnitName;

    @Column(name = "status", nullable = false, length = 64)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "orgUnit")
    private List<Client> clients;

    public UUID getOrgUnitId() {
        return orgUnitId;
    }

    public String getOrgUnitName() {
        return orgUnitName;
    }

    public void setOrgUnitName(String orgUnitName) {
        this.orgUnitName = orgUnitName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
