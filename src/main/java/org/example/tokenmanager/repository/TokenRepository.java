package org.example.tokenmanager.repository;

import org.example.tokenmanager.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenValue(String tokenValue);

    List<Token> findByUserId(String userId);

    List<Token> findByUserIdAndValidTokenTrue(String userId);
}
