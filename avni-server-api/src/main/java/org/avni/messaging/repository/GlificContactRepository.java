package org.avni.messaging.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.avni.messaging.contract.glific.GlificOptinContactResponse;
import org.avni.messaging.contract.glific.GlificGetContactsResponse;
import org.avni.messaging.contract.glific.GlificResponse;
import org.avni.messaging.external.GlificRestClient;
import org.avni.server.util.ObjectMapperSingleton;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class GlificContactRepository {

    private final String OPTIN_CONTACT_JSON;
    private final GlificRestClient glificRestClient;
    private final String GET_CONTACT_JSON;

    public GlificContactRepository(GlificRestClient glificRestClient) {
        this.glificRestClient = glificRestClient;
        try {
            GET_CONTACT_JSON = ObjectMapperSingleton.getObjectMapper().readTree(this.getClass().getResource("/external/glific/getContact.json")).toString();
            OPTIN_CONTACT_JSON = ObjectMapperSingleton.getObjectMapper().readTree(this.getClass().getResource("/external/glific/optinContact.json")).toString();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public String getOrCreateGlificContactId(String phoneNumber, String fullName) {
        GlificGetContactsResponse glificContacts = getGlificContact(phoneNumber);
        return glificContacts.getContacts().isEmpty() ?
                createGlificContact(phoneNumber, fullName) :
                glificContacts.getContacts().get(0).getId();
    }

    private String createGlificContact(String phoneNumber, String fullName) {
        String message = OPTIN_CONTACT_JSON.replace("${phoneNumber}", phoneNumber)
                .replace("${fullName}", fullName);
        GlificOptinContactResponse glificOptinContactResponse = glificRestClient.callAPI(message, new ParameterizedTypeReference<GlificResponse<GlificOptinContactResponse>>() {
        });
        return glificOptinContactResponse.getOptinContact().getContact().getId();
    }

    private GlificGetContactsResponse getGlificContact(String phoneNumber) {
        String message = GET_CONTACT_JSON.replace("${phoneNumber}", phoneNumber);
        return glificRestClient.callAPI(message, new ParameterizedTypeReference<GlificResponse<GlificGetContactsResponse>>() {
        });
    }

}