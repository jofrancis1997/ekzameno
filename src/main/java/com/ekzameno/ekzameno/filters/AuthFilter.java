package com.ekzameno.ekzameno.filters;

import java.io.IOException;
import java.security.Key;
import java.sql.SQLException;
import java.util.UUID;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.ekzameno.ekzameno.mappers.UserMapper;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Filter to determine whether requests are authenticated.
 */
@Provider
@Protected
public class AuthFilter implements ContainerRequestFilter {
    private Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(System.getenv(
        "JWT_SECRET"
    )));

    @Override
    public void filter(
        ContainerRequestContext requestContext
    ) throws IOException {
        Cookie cookie = requestContext.getCookies().get("jwt");
        if (cookie != null) {
            String jwt = cookie.getValue();
            try {
                String subject = Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getSubject();

                new UserMapper().findById(UUID.fromString(subject));
            } catch (JwtException | SQLException e) {
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).build()
                );
            }
        } else {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED).build()
            );
        }
    }
}
