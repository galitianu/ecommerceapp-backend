package com.ciconiasystems.ecommerceappbackend.services;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
public class KeycloakService {
    @Value("${ecommerce.web.baseURL}")
    private String baseURL;

    @Value("${ecommerce.web.clientSecret}")
    private String webClientSecret;

    @Value("${ecommerce.mobile.clientSecret}")
    private String mobileClientSecret;

    public boolean createNewWebUser(String username, String email, String firstName, String lastName, String password) {
        return createNewUser(username, email, firstName, lastName, password, "ecommerceapp", "ecommerceapp-backend", webClientSecret);
    }

    public boolean createNewMobileUser(String username, String email, String firstName, String lastName, String password) {
        return createNewUser(username, email, firstName, lastName, password, "ecommerceapp-mobile", "ecommerceapp-backend-mobile", mobileClientSecret);
    }

    private boolean createNewUser(String username, String email, String firstName, String lastName, String password, String realmName, String clientId, String clientSecret) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(baseURL)
                .realm(realmName)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);

        // Create user on server
        Response createUserResponse = keycloak.realm(realmName).users().create(user);
        if (createUserResponse.getStatus() == 201) {
            String userId = CreatedResponseUtil.getCreatedId(createUserResponse);
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(password);

            // Reset password using userId
            UserResource userResource = keycloak.realm(realmName).users().get(userId);
            userResource.resetPassword(passwordCred);
        } else {
            log.error("Error creating user: " + createUserResponse.getStatus());
            return false;
        }
        keycloak.close();
        return true;
    }

    public boolean patchUserMobile(String username, String newFirstName, String newLastName) {
        return patchUser(username,newFirstName, newLastName,"ecommerceapp-mobile", "ecommerceapp-backend-mobile", mobileClientSecret);
    }

    private boolean patchUser(String username, String newFirstName, String newLastName, String realmName, String clientId, String clientSecret) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(baseURL)
                .realm(realmName)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

        List<UserRepresentation> searchResults = keycloak.realm(realmName).users().search(username);
        if (searchResults.isEmpty()) {
            log.error("User not found: " + username);
            keycloak.close();
            return false;
        }
        String userId = searchResults.get(0).getId();

        UserResource userResource = keycloak.realm(realmName).users().get(userId);
        UserRepresentation userRepresentation = userResource.toRepresentation();
        userRepresentation.setFirstName(newFirstName);
        userRepresentation.setLastName(newLastName);

        try {
            userResource.update(userRepresentation);
        } catch (Exception e) {
            log.error("Error updating user: " + username, e);
            keycloak.close();
            return false;
        }

        keycloak.close();
        return true;
    }

    public boolean isWebUserAdmin(String email) {
        return isUserAdmin(email, "ecommerceapp", "ecommerceapp-backend", webClientSecret);
    }

    public boolean isUserAdmin(String email, String realmName, String clientId, String clientSecret) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(baseURL)
                .realm(realmName)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

        List<UserRepresentation> users = keycloak.realm(realmName).users().search(null, null, null, email, 0, 1);
        if (users.isEmpty()) {
            keycloak.close();
            return false;
        }

        UserRepresentation user = users.get(0);
        UserResource userResource = keycloak.realm(realmName).users().get(user.getId());
        Set<RoleRepresentation> roles = new HashSet<>(userResource.roles().realmLevel().listEffective());

        boolean isAdmin = roles.stream().anyMatch(role -> role.getName().equalsIgnoreCase("admin"));

        keycloak.close();
        return isAdmin;
    }
}
