package com.sanket.AI.Code.Review.Platform.auth.service;

import com.sanket.AI.Code.Review.Platform.auth.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(Long userId);

    RefreshToken verifyExpiration(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void revokeToken(String token);

    void deleteByUserId(Long userId);
}
