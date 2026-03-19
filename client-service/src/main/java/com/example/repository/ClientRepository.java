package com.example.repository;

import com.example.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Query("select c from Client c join fetch c.orgUnit order by c.clientName asc")
    List<Client> findAllWithOrgUnit();

    @Query("select c from Client c join fetch c.orgUnit where c.clientId = :clientId")
    Optional<Client> findDetailedById(@Param("clientId") UUID clientId);
}
