package mh.backend.userservice.repository;

import mh.backend.userservice.entity.OrgUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrgUnitRepository extends JpaRepository<OrgUnit, UUID> {
}
