package com.ciconiasystems.ecommerceappbackend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JwtConfig {

    @Value("${ecommerce.web.issuer}")
    private String WebIssuer;

    @Value("${ecommerce.mobile.issuer}")
    private String MobileIssuer;

    @Bean
    public JwtDecoder jwtDecoder() {
        Map<String, JwtDecoder> decoders = new HashMap<>();

        // Configure a decoder for issuer A
        NimbusJwtDecoder jwtDecoderA = NimbusJwtDecoder.withJwkSetUri(WebIssuer + "/protocol/openid-connect/certs").build();
        // Add custom validators or converters if necessary for Issuer A
        decoders.put(WebIssuer, jwtDecoderA);

        // Configure a decoder for issuer B
        NimbusJwtDecoder jwtDecoderB = NimbusJwtDecoder.withJwkSetUri(MobileIssuer + "/protocol/openid-connect/certs").build();
        // Add custom validators or converters if necessary for Issuer B
        decoders.put(MobileIssuer, jwtDecoderB);

        return new DelegatingJwtDecoder(decoders);
    }
}
