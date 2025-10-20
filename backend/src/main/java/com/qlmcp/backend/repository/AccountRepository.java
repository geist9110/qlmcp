package com.qlmcp.backend.repository;

import com.qlmcp.backend.entity.Account;
import com.qlmcp.backend.entity.Account.AuthProvider;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
