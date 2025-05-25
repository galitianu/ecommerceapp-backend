package com.ciconiasystems.ecommerceappbackend.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;

public class DelegatingJwtDecoder implements JwtDecoder {
    private final Map<String, JwtDecoder> decoders;

    public DelegatingJwtDecoder(Map<String, JwtDecoder> decoders) {
        this.decoders = decoders;
    }

    @Value("${keycloak.port}")
    private String keycloakPort;

    @Override
    public Jwt decode(String token) throws JwtException {
        // Extract the issuer from the token
        String issuer = JwtUtils.getIssuer(token, keycloakPort);
        JwtDecoder decoder = decoders.get(issuer);
        if (decoder == null) {
            throw new JwtException("No JwtDecoder registered for issuer: " + issuer);
        }
        return decoder.decode(token);
    }

    private static class JwtUtils {
        private static String getIssuer(String token, String keycloakPort) {
            // Split the JWT token into parts
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new JwtException("JWT token does not have 2 parts");
            }
            // Decode the payload part
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            try {
                // Convert payload JSON string to Map
                Map<String, Object> payloadMap = new ObjectMapper().readValue(payload, Map.class);
                // Return the issuer claim
                String issuerClaim = (String) payloadMap.get("iss");
                int index = issuerClaim.indexOf("localhost");
                if (index != -1) {
                    // Calculate the position right after "localhost" (9 characters long)
                    int positionToInsert = index + "localhost".length();
                    return issuerClaim.substring(0, positionToInsert) + ":" + keycloakPort + issuerClaim.substring(positionToInsert);
                }
                // Return the original URL if "localhost" is not found
                return issuerClaim;
            } catch (Exception e) {
                throw new JwtException("Could not extract issuer from token", e);
            }
        }
    }
}
