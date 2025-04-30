package com.ahmad.hogwartsartifactsonline.security;

import com.ahmad.hogwartsartifactsonline.client.rediscache.RedisCacheClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final RedisCacheClient redisCacheClient;

    public JwtInterceptor(RedisCacheClient redisCacheClient) {
        this.redisCacheClient = redisCacheClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Get the token from thr request header
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // If the token is not null, and it starts with "Bearer ", then verify if the token is present in Redis.
        // Else this request is just a public request that does not need to be authenticated, so we can return true.

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwt = (Jwt) authentication.getPrincipal();

            // Retrieve the userId from the JWT token and check if the token is in the white list in Redis
            String userId = jwt.getClaim("userId").toString();
            if (!redisCacheClient.isUserTokenInWhiteList(userId, jwt.getTokenValue())) {
                throw new BadCredentialsException("Invalid token");
            }
        }
        return true;
    }
}
