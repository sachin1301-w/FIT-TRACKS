
package com.fitness.gateway;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.text.ParseException;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {

    private final UserService userService;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain) {

        String userId = exchange.getRequest()
                .getHeaders().getFirst("X-User-Id");

        String token = exchange.getRequest()
                .getHeaders().getFirst("Authorization");

        // No token: continue the filter chain.
        if (token == null || token.isBlank()) {
            return chain.filter(exchange);
        }

        RegisterRequest registerRequest;

        try {
            registerRequest = getUserDetails(token);
        } catch (RuntimeException e) {
            return Mono.error(e);
        }

        if (userId == null) {
            userId = registerRequest.getKeycloakId();
        }

        if (userId == null || userId.isBlank()) {
            return chain.filter(exchange);
        }

        String finalUserId = userId;

        return userService.validateUser(finalUserId)
                .flatMap(exists -> {

                    if (!exists) {
                        return userService.registerUser(registerRequest)
                                .doOnSuccess(user ->
                                        log.info("User registered successfully"))
                                .then();
                    }

                    log.info("User already exists");

                    return Mono.empty();
                })
                .then(Mono.defer(() -> {

                    ServerHttpRequest mutatedRequest =
                            exchange.getRequest().mutate()
                                    .header("X-User-Id", finalUserId)
                                    .build();

                    return chain.filter(
                            exchange.mutate()
                                    .request(mutatedRequest)
                                    .build()
                    );
                }));
    }

    private RegisterRequest getUserDetails(String token) {

        try {
            String tokenWithoutBearer =
                    token.replaceFirst("(?i)^Bearer\\s+", "").trim();

            SignedJWT signedJWT =
                    SignedJWT.parse(tokenWithoutBearer);

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            RegisterRequest request = new RegisterRequest();

            request.setEmail(claims.getStringClaim("email"));
            request.setKeycloakId(claims.getStringClaim("sub"));
            request.setFirstName(claims.getStringClaim("given_name"));
            request.setLastName(claims.getStringClaim("family_name"));
            request.setPassword("dummy@123123");

            return request;

        } catch (ParseException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
}
