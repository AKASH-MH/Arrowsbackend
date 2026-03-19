package mh.backend.userservice.repository;

import mh.backend.userservice.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    @Query("select u from UserAccount u left join fetch u.orgUnit order by u.fullName asc")
    List<UserAccount> findAllDetailed();

    @Query("select u from UserAccount u left join fetch u.orgUnit where u.userId = :userId")
    Optional<UserAccount> findDetailedById(@Param("userId") UUID userId);
}
