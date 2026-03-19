package com.example.repository;

import com.example.entity.OrgUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrgUnitRepository extends JpaRepository<OrgUnit, UUID> {

    List<OrgUnit> findAllByOrderByOrgUnitNameAsc();
}
