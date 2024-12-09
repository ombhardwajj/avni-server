package org.avni.messaging.repository;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.transaction.Transactional;
import org.avni.messaging.contract.glific.GlificAuth;
import org.avni.messaging.contract.glific.GlificMessageTemplateResponse;
import org.avni.messaging.contract.glific.GlificResponse;
import org.avni.messaging.domain.exception.GlificConnectException;
import org.avni.messaging.domain.exception.GlificNotConfiguredException;
import org.avni.messaging.external.GlificRestClient;
import org.avni.server.common.AbstractControllerIntegrationTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@Sql(value = {"/tear-down.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/test-data.sql"})
@Sql(value = {"/tear-down.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
public class GlificRestClientTest extends AbstractControllerIntegrationTest {
    private String SAMPLE_AUTH_RESPONSE = "{\"data\":{\"access_token\":\"SFMyNTY.YjQ2M2MzMmMtNGZlOC00OTEyLWIzYTEtZmRhZTRkOGQ1ZTIx.3TjKqpElrD5N2ffGHEAFX91cyp7zwoTztYR8p1jwwgA\",\"renewal_token\":\"SFMyNTY.MjYxODllMTgtNDM1OC00YjJjLTlmN2MtOTA5MzMwYzM3ZjA2.dDigSwftcGFGHu4o9MwkASp2KqH6eitp1aRmeYSgi5M\",\"token_expiry_time\":\"2022-10-13T21:42:33.342529Z\"}}";

    @Autowired
    private GlificRestClient glificRestClient;

    private static WireMockServer wireMockServer = new WireMockServer(9191);

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        wireMockServer.start();
        Thread.sleep(2000);
    }

    @Before
    public void setup() {
        wireMockServer.resetAll();
        stubRequest("/api/v1/session", SAMPLE_AUTH_RESPONSE);
    }

    @AfterClass
    public static void afterClass() {
        wireMockServer.stop();
    }

    @Test
    public void shouldAuthenticateWithGlific() throws GlificNotConfiguredException {
        GlificAuth authResponse = glificRestClient.authenticate();

        assertThat(authResponse).isNotNull();
        assertThat(authResponse.getAccessToken()).isEqualTo("SFMyNTY.YjQ2M2MzMmMtNGZlOC00OTEyLWIzYTEtZmRhZTRkOGQ1ZTIx.3TjKqpElrD5N2ffGHEAFX91cyp7zwoTztYR8p1jwwgA");
    }

    @Test
    public void shouldMakeCallsToGlific() throws IOException, GlificNotConfiguredException {
        String sessionTemplates = "{\"data\":{\"sessionTemplates\":[{\"body\":\"Hello!\\nWe are coming up with a Delhivery career opertunities for all students. These sessions would include training + employment process. Plese register with us to know more about this program.\\nTo register, click on below link\\nhttps://api.whatsapp.com/send?phone=918956411022&text=jobdlv\\nRegards,\\nLend a Hand India.\",\"id\":\"3639\",\"insertedAt\":\"2022-05-19T09:43:54Z\",\"isActive\":true,\"isHsm\":true,\"isSource\":false,\"label\":\"Delhivary_update\",\"language\":{\"label\":\"English\"},\"messageMedia\":null,\"shortcode\":\"webinar_invite\",\"translations\":\"{}\",\"type\":\"TEXT\",\"updatedAt\":\"2022-05-20T00:00:29Z\"},{\"body\":\"Hello!\\nWe are coming up with a Delhivery career opertunities for all students. These sessions would include training + employment process. Plese register with us to know more about this program.\\nTo register, you can type \\\"jobdlv\\\" and reply to this message.\\nRegards,\\nLend a Hand India.\",\"id\":\"3640\",\"insertedAt\":\"2022-05-19T09:46:07Z\",\"isActive\":true,\"isHsm\":true,\"isSource\":false,\"label\":\"Delv_web\",\"language\":{\"label\":\"English\"},\"messageMedia\":null,\"shortcode\":\"webinar_invite_1\",\"translations\":\"{}\",\"type\":\"TEXT\",\"updatedAt\":\"2022-05-20T00:00:29Z\"},{\"body\":\"[0xe0][0xa4][0xaa][0xe0][0xa5][0x8d][0xe0][0xa4][0xb0][0xe0][0xa4][0xbf][0xe0][0xa4][0xaf] [0xe0][0xa4][0xb5][0xe0][0xa4][0xbf][0xe0][0xa4][0xa6][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa4][0xbe][0xe0][0xa4][0xb0][0xe0][0xa5][0x8d][0xe0][0xa4][0xa5][0xe0][0xa5][0x80],\\n\\n[0xe0][0xa4][0xb9][0xe0][0xa4][0xbe] [0xe0][0xa4][0xae][0xe0][0xa5][0x87][0xe0][0xa4][0xb8][0xe0][0xa5][0x87][0xe0][0xa4][0x9c] [0xe0][0xa4][0xa4][0xe0][0xa5][0x81][0xe0][0xa4][0xae][0xe0][0xa4][0x9a][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa4][0xbe] [0xe0][0xa4][0x95][0xe0][0xa4][0xb0][0xe0][0xa4][0xbf][0xe0][0xa4][0xa4][0xe0][0xa4][0xbe] [0xe0][0xa4][0x95][0xe0][0xa4][0xbf][0xe0][0xa4][0x82][0xe0][0xa4][0xb5][0xe0][0xa4][0xbe] [0xe0][0xa4][0xa4][0xe0][0xa5][0x81][0xe0][0xa4][0xae][0xe0][0xa4][0x9a][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa4][0xbe] [0xe0][0xa4][0xae][0xe0][0xa5][0x81][0xe0][0xa4][0xb2][0xe0][0xa5][0x80]/[0xe0][0xa4][0xac][0xe0][0xa4][0xb9][0xe0][0xa4][0xbf][0xe0][0xa4][0xa3][0xe0][0xa5][0x80]/[0xe0][0xa4][0xae][0xe0][0xa5][0x88][0xe0][0xa4][0xa4][0xe0][0xa5][0x8d][0xe0][0xa4][0xb0][0xe0][0xa4][0xbf][0xe0][0xa4][0xa3][0xe0][0xa5][0x80] [0xe0][0xa4][0x95][0xe0][0xa4][0xb0][0xe0][0xa4][0xbf][0xe0][0xa4][0xa4][0xe0][0xa4][0xbe] [0xe0][0xa4][0x85][0xe0][0xa4][0xa4][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa4][0x82][0xe0][0xa4][0xa4] [0xe0][0xa4][0x89][0xe0][0xa4][0xaa][0xe0][0xa4][0xaf][0xe0][0xa5][0x81][0xe0][0xa4][0x95][0xe0][0xa5][0x8d][0xe0][0xa4][0xa4] [0xe0][0xa4][0xa0][0xe0][0xa4][0xb0][0xe0][0xa5][0x82] [0xe0][0xa4][0xb6][0xe0][0xa4][0x95][0xe0][0xa4][0xa4][0xe0][0xa5][0x8b]\\n\\n[0xf0][0x9f][0x93][0xa3][0xe0][0xa4][0xa6][0xe0][0xa4][0xb9][0xe0][0xa4][0xbe][0xe0][0xa4][0xb5][0xe0][0xa5][0x80]/[0xe0][0xa4][0xac][0xe0][0xa4][0xbe][0xe0][0xa4][0xb0][0xe0][0xa4][0xbe][0xe0][0xa4][0xb5][0xe0][0xa5][0x80] [0xe0][0xa4][0xaa][0xe0][0xa5][0x82][0xe0][0xa4][0xb0][0xe0][0xa5][0x8d][0xe0][0xa4][0xa3] [0xe2][0x9c][0x94][0xef][0xb8][0x8f] [0xe0][0xa4][0x95][0xe0][0xa5][0x87][0xe0][0xa4][0xb2][0xe0][0xa5][0x87][0xe0][0xa4][0xb2][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa4][0xbe] [0xe0][0xa4][0x86][0xe0][0xa4][0xa3][0xe0][0xa4][0xbf] [0xe0][0xa4][0xb8][0xe0][0xa5][0x8d][0xe0][0xa4][0xb5][0xe0][0xa4][0xac][0xe0][0xa4][0xb3][0xe0][0xa4][0xbe][0xe0][0xa4][0xb5][0xe0][0xa4][0xb0] [0xe0][0xa4][0x86][0xe0][0xa4][0xaa][0xe0][0xa4][0xb2][0xe0][0xa5][0x87] [0xe0][0xa4][0x95][0xe0][0xa4][0xb0][0xe0][0xa4][0xbf][0xe0][0xa4][0x85][0xe0][0xa4][0xb0] [0xe0][0xa4][0xac][0xe0][0xa4][0xa8][0xe0][0xa4][0xb5][0xe0][0xa4][0xa3][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa4][0xbe][0xe0][0xa4][0xb8] [0xe0][0xa4][0x87][0xe0][0xa4][0x9a][0xe0][0xa5][0x8d][0xe0][0xa4][0x9b][0xe0][0xa5][0x81][0xe0][0xa4][0x95] [0xe0][0xa4][0xae][0xe0][0xa5][0x81][0xe0][0xa4][0xb2][0xe0][0xa5][0x80][0xe0][0xa4][0x82][0xe0][0xa4][0x95][0xe0][0xa4][0xb0][0xe0][0xa4][0xbf][0xe0][0xa4][0xa4][0xe0][0xa4][0xbe] [0xf0][0x9f][0x91][0xa9][0xe2][0x80][0x8d][0xf0][0x9f][0x92][0xbc] [0xe0][0xa4][0xb8][0xe0][0xa5][0x81][0xe0][0xa4][0xb5][0xe0][0xa4][0xb0][0xe0][0xa5][0x8d][0xe0][0xa4][0xa3][0xe0][0xa4][0xb8][0xe0][0xa4][0x82][0xe0][0xa4][0xa7][0xe0][0xa5][0x80][0xf0][0x9f][0x93][0xa3]\\n\\n[0xe0][0xa4][0xb2][0xe0][0xa5][0x87][0xe0][0xa4][0xa8][0xe0][0xa5][0x8d][0xe0][0xa4][0xa1] [0xe0][0xa4][0x85] [0xe0][0xa4][0xb9][0xe0][0xa5][0x85][0xe0][0xa4][0xa8][0xe0][0xa5][0x8d][0xe0][0xa4][0xa1] [0xe0][0xa4][0x87][0xe0][0xa4][0x82][0xe0][0xa4][0xa1][0xe0][0xa4][0xbf][0xe0][0xa4][0xaf][0xe0][0xa4][0xbe] [0xe0][0xa4][0xb9][0xe0][0xa4][0xbf] [0xe0][0xa4][0xb6][0xe0][0xa4][0xbf][0xe0][0xa4][0x95][0xe0][0xa5][0x8d][0xe0][0xa4][0xb7][0xe0][0xa4][0xa3] [0xe0][0xa4][0x95][0xe0][0xa5][0x8d][0xe0][0xa4][0xb7][0xe0][0xa5][0x87][0xe0][0xa4][0xa4][0xe0][0xa5][0x8d][0xe0][0xa4][0xb0][0xe0][0xa4][0xbe][0xe0][0xa4][0xa4] [0xe0][0xa4][0x95][0xe0][0xa4][0xbe][0xe0][0xa4][0xae] [0xe0][0xa4][0x95][0xe0][0xa4][0xb0][0xe0][0xa4][0xa3][0xe0][0xa4][0xbe][0xe0][0xa4][0xb0][0xe0][0xa5][0x80] [0xe0][0xa4][0xb8][0xe0][0xa4][0xbe][0xe0][0xa4][0xae][0xe0][0xa4][0xbe][0xe0][0xa4][0x9c][0xe0][0xa4][0xbf][0xe0][0xa4][0x95] [0xe0][0xa4][0xb8][0xe0][0xa4][0x82][0xe0][0xa4][0xb8][0xe0][0xa5][0x8d][0xe0][0xa4][0xa5][0xe0][0xa4][0xbe] [0xe0][0xa4][0x86][0xe0][0xa4][0xb9][0xe0][0xa5][0x87] [0xe0][0xa4][0x86][0xe0][0xa4][0xa3][0xe0][0xa4][0xbf] [0xe0][0xa4][0x86][0xe0][0xa4][0xae][0xe0][0xa5][0x8d][0xe0][0xa4][0xb9][0xe0][0xa5][0x80] [0xe0][0xa4][0xae][0xe0][0xa5][0x81][0xe0][0xa4][0xb2][0xe0][0xa5][0x80][0xe0][0xa4][0x82] [0xe0][0xa4][0x95][0xe0][0xa4][0xb0][0xe0][0xa4][0xbf][0xe0][0xa4][0xa4][0xe0][0xa4][0xbe] [0xe0][0xa5][0xa7][0xe0][0xa5][0xa6] [0xe0][0xa4][0xb5][0xe0][0xa5][0x80] [0xe0][0xa4][0x86][0xe0][0xa4][0xa3][0xe0][0xa4][0xbf] [0xe0][0xa5][0xa7][0xe0][0xa5][0xa8][0xe0][0xa4][0xb5][0xe0][0xa5][0x80] [0xe0][0xa4][0xa8][0xe0][0xa4][0x82][0xe0][0xa4][0xa4][0xe0][0xa4][0xb0] [0xf0][0x9f][0x96][0xa5][0xef][0xb8][0x8f] [0xe0][0xa4][0xb8][0xe0][0xa5][0x89][0xe0][0xa4][0xab][0xe0][0xa5][0x8d][0xe0][0xa4][0x9f][0xe0][0xa4][0xb5][0xe0][0xa5][0x87][0xe0][0xa4][0x85][0xe0][0xa4][0xb0] [0xe0][0xa4][0x95][0xe0][0xa5][0x8d][0xe0][0xa4][0xb7][0xe0][0xa5][0x87][0xe0][0xa4][0xa4][0xe0][0xa5][0x8d][0xe0][0xa4][0xb0][0xe0][0xa4][0xbe][0xe0][0xa4][0xa4] [0xe0][0xa4][0x95][0xe0][0xa4][0xb0][0xe0][0xa4][0xbf][0xe0][0xa4][0x85][0xe0][0xa4][0xb0] [0xe0][0xa4][0x9a][0xe0][0xa5][0x80] [0xe0][0xa4][0x89][0xe0][0xa4][0xa4][0xe0][0xa5][0x8d][0xe0][0xa4][0xa4][0xe0][0xa4][0xae] [0xe0][0xa4][0xb8][0xe0][0xa4][0x82][0xe0][0xa4][0xa7][0xe0][0xa5][0x80] [0xe0][0xa4][0x98][0xe0][0xa5][0x87][0xe0][0xa4][0x8a][0xe0][0xa4][0xa8] [0xe0][0xa4][0x86][0xe0][0xa4][0xb2][0xe0][0xa5][0x8b] [0xe0][0xa4][0x86][0xe0][0xa4][0xb9][0xe0][0xa5][0x8b][0xe0][0xa4][0xa4]. \\n\\n[0xe0][0xa4][0xa4][0xe0][0xa5][0x80] [0xe0][0xa4][0xae][0xe0][0xa5][0x8d][0xe0][0xa4][0xb9][0xe0][0xa4][0xa3][0xe0][0xa4][0x9c][0xe0][0xa5][0x87] [0xe2][0x80][0x9c][0xe0][0xa4][0xa8][0xe0][0xa4][0xb5][0xe0][0xa4][0x97][0xe0][0xa5][0x81][0xe0][0xa4][0xb0][0xe0][0xa5][0x81][0xe0][0xa4][0x95][0xe0][0xa5][0x81][0xe0][0xa4][0xb2][0xe2][0x80][0x9d]\\n\\n[0xe0][0xa4][0xaa][0xe0][0xa5][0x81][0xe0][0xa4][0xa3][0xe0][0xa5][0x87] [0xe0][0xa4][0xb6][0xe0][0xa4][0xb9][0xe0][0xa4][0xb0][0xe0][0xa4][0xbe][0xe0][0xa4][0xa4]..\\n[0xe0][0xa4][0xaa][0xe0][0xa5][0x82][0xe0][0xa4][0xb0][0xe0][0xa5][0x8d][0xe0][0xa4][0xa3][0xe0][0xa4][0xa4]: [0xe0][0xa4][0xa8][0xe0][0xa4][0xbf][0xe0][0xa4][0xb6][0xe0][0xa5][0x81][0xe0][0xa4][0xb2][0xe0][0xa5][0x8d][0xe0][0xa4][0x95][0xf0][0x9f][0x86][0x93]\\n[0xe0][0xa4][0xaa][0xe0][0xa5][0x82][0xe0][0xa4][0xb0][0xe0][0xa5][0x8d][0xe0][0xa4][0xa3][0xe0][0xa4][0xa4]: [0xe0][0xa4][0xa8][0xe0][0xa4][0xbf][0xe0][0xa4][0xb5][0xe0][0xa4][0xbe][0xe0][0xa4][0xb8][0xe0][0xa5][0x80] [0xf0][0x9f][0x8f][0xa4]\\n[0xf0][0x9f][0x92][0xaf]% [0xe0][0xa4][0x9c][0xe0][0xa5][0x89][0xe0][0xa4][0xac] [0xe0][0xa4][0x97][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa4][0xbe][0xe0][0xa4][0xb0][0xe0][0xa4][0x82][0xe0][0xa4][0x9f][0xe0][0xa5][0x80]\\n\\n[0xe0][0xa4][0xaf][0xe0][0xa4][0xbe] [0xe0][0xa4][0xb8][0xe0][0xa4][0x82][0xe0][0xa4][0xa6][0xe0][0xa4][0xb0][0xe0][0xa5][0x8d][0xe0][0xa4][0xad][0xe0][0xa4][0xbe][0xe0][0xa4][0xa4][0xe0][0xa5][0x80][0xe0][0xa4][0xb2] [0xe0][0xa4][0xb5][0xe0][0xa5][0x87][0xe0][0xa4][0xac][0xe0][0xa4][0xbf][0xe0][0xa4][0xa8][0xe0][0xa4][0xbe][0xe0][0xa4][0xb0] [0xe0][0xa4][0xa6][0xe0][0xa4][0xbf][0xe0][0xa4][0xa8][0xe0][0xa4][0xbe][0xe0][0xa4][0x82][0xe0][0xa4][0x95] {{1}} [0xe0][0xa4][0xb2][0xe0][0xa4][0xbe] [0xe0][0xa4][0xb8][0xe0][0xa4][0x82][0xe0][0xa4][0xa7][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa4][0xbe][0xe0][0xa4][0x95][0xe0][0xa4][0xbe][0xe0][0xa4][0xb3][0xe0][0xa5][0x80] {{2}} [0xe0][0xa4][0xaf][0xe0][0xa4][0xbe] [0xe0][0xa4][0xb5][0xe0][0xa5][0x87][0xe0][0xa4][0xb3][0xe0][0xa4][0xbe][0xe0][0xa4][0xa4] [0xe0][0xa4][0x86][0xe0][0xa4][0xaf][0xe0][0xa5][0x8b][0xe0][0xa4][0x9c][0xe0][0xa4][0xbf][0xe0][0xa4][0xa4] [0xe0][0xa4][0x95][0xe0][0xa5][0x87][0xe0][0xa4][0xb2][0xe0][0xa4][0xbe] [0xe0][0xa4][0x86][0xe0][0xa4][0xb9][0xe0][0xa5][0x87].\\n\\n[0xe0][0xa4][0xb9][0xe0][0xa4][0xbe] [0xe0][0xa4][0xb5][0xe0][0xa5][0x87][0xe0][0xa4][0xac][0xe0][0xa4][0xbf][0xe0][0xa4][0xa8][0xe0][0xa4][0xbe][0xe0][0xa4][0xb0] [0xe0][0xa4][0x9d][0xe0][0xa5][0x82][0xe0][0xa4][0xae] [0xe0][0xa4][0xb5][0xe0][0xa4][0xb0] [0xe0][0xa4][0xb9][0xe0][0xa5][0x8b][0xe0][0xa4][0xa3][0xe0][0xa4][0xbe][0xe0][0xa4][0xb0] [0xe0][0xa4][0x85][0xe0][0xa4][0xb8][0xe0][0xa5][0x82][0xe0][0xa4][0xa8] \\n[0xe0][0xa4][0x9d][0xe0][0xa5][0x82][0xe0][0xa4][0xae] [0xe0][0xa4][0xb2][0xe0][0xa4][0xbf][0xe0][0xa4][0x82][0xe0][0xa4][0x95] : {{3}}\\n\\n[0xe0][0xa4][0xb9][0xe0][0xa4][0xbe] [0xe0][0xa4][0xaa][0xe0][0xa5][0x8d][0xe0][0xa4][0xb0][0xe0][0xa5][0x8b][0xe0][0xa4][0x97][0xe0][0xa5][0x8d][0xe0][0xa4][0xb0][0xe0][0xa4][0xbe][0xe0][0xa4][0xae] [0xe0][0xa4][0xab][0xe0][0xa4][0x95][0xe0][0xa5][0x8d][0xe0][0xa4][0xa4] [0xe0][0xa4][0xae][0xe0][0xa5][0x81][0xe0][0xa4][0xb2][0xe0][0xa5][0x80][0xe0][0xa4][0x82][0xe0][0xa4][0x95][0xe0][0xa4][0xb0][0xe0][0xa4][0xa4][0xe0][0xa4][0xbe] [0xf0][0x9f][0x92][0x83] [0xe0][0xa4][0x86][0xe0][0xa4][0xb9][0xe0][0xa5][0x87] [0xe0][0xa4][0xaf][0xe0][0xa4][0xbe][0xe0][0xa4][0x9a][0xe0][0xa5][0x80] [0xe0][0xa4][0xa8][0xe0][0xa5][0x8b][0xe0][0xa4][0x82][0xe0][0xa4][0xa6] [0xe0][0xa4][0x98][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa4][0xbe][0xe0][0xa4][0xb5][0xe0][0xa5][0x80].\\n\\n[0xe0][0xa4][0xaf][0xe0][0xa4][0xbe] [0xe0][0xa4][0xb5][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa4][0xa4][0xe0][0xa4][0xbf][0xe0][0xa4][0xb0][0xe0][0xa4][0xbf][0xe0][0xa4][0x95][0xe0][0xa5][0x8d][0xe0][0xa4][0xa4] [0xe0][0xa4][0x87][0xe0][0xa4][0xa4][0xe0][0xa4][0xb0] [0xe0][0xa4][0xb8][0xe0][0xa4][0x82][0xe0][0xa4][0xa7][0xe0][0xa5][0x80][0xe0][0xa4][0x95][0xe0][0xa4][0xb0][0xe0][0xa4][0xbf][0xe0][0xa4][0xa4][0xe0][0xa4][0xbe] [0xe0][0xa4][0x86][0xe0][0xa4][0xae][0xe0][0xa4][0x9a][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa4][0xbe] [0xe0][0xa4][0xaa][0xe0][0xa5][0x81][0xe0][0xa4][0xa2][0xe0][0xa5][0x80][0xe0][0xa4][0xb2] [0xe0][0xa4][0xae][0xe0][0xa5][0x87][0xe0][0xa4][0xb8][0xe0][0xa5][0x87][0xe0][0xa4][0x9c] [0xe0][0xa4][0x9a][0xe0][0xa5][0x80] [0xe0][0xa4][0xb5][0xe0][0xa4][0xbe][0xe0][0xa4][0x9f] [0xe0][0xa4][0xaa][0xe0][0xa4][0xb9][0xe0][0xa4][0xbe] [0xf0][0x9f][0xa4][0xb7][0xf0][0x9f][0x8f][0xbb][0xe2][0x80][0x8d][0xe2][0x99][0x82][0xef][0xb8][0x8f]\\n\\n[0xe0][0xa4][0x9a][0xe0][0xa4][0xb2][0xe0][0xa4][0xbe] [0xe0][0xa4][0xa4][0xe0][0xa4][0xb0] [0xe0][0xa4][0xae][0xe0][0xa4][0x97] [0xe0][0xa4][0xb5][0xe0][0xa5][0x87][0xe0][0xa4][0xac][0xe0][0xa4][0xbf][0xe0][0xa4][0xa8][0xe0][0xa4][0xbe][0xe0][0xa4][0xb0] [0xe0][0xa4][0xae][0xe0][0xa4][0xa7][0xe0][0xa5][0x8d][0xe0][0xa4][0xaf][0xe0][0xa5][0x87] [0xe0][0xa4][0xad][0xe0][0xa5][0x87][0xe0][0xa4][0x9f][0xe0][0xa5][0x82][0xe0][0xa4][0xaf][0xe0][0xa4][0xbe] !! [0xe2][0x8f][0xb0]\",\"id\":\"3310\",\"insertedAt\":\"2022-04-17T14:02:05Z\",\"isActive\":true,\"isHsm\":true,\"isSource\":false,\"label\":\"Navgurukul_apr22\",\"language\":{\"label\":\"Bengali\"},\"messageMedia\":null,\"shortcode\":\"navgurukul_webinar\",\"translations\":\"{}\",\"type\":\"TEXT\",\"updatedAt\":\"2022-04-18T00:00:30Z\"}]}}";
        stubRequest("/api", sessionTemplates);

        Object json = mapper.readValue(this.getClass().getResource("/ref/glific/messageTemplateRequest.json"), Object.class);

        GlificMessageTemplateResponse response = glificRestClient.callAPI(json,
                new ParameterizedTypeReference<GlificResponse<GlificMessageTemplateResponse>>() {
                });

        assertThat(response.getSessionTemplates()).isNotEmpty();
        assertThat(response.getSessionTemplates().size()).isEqualTo(3);
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldThrowRegularExceptionsForBadHttpResponseCodes() throws GlificNotConfiguredException {
        wireMockServer.addStubMapping(WireMock.post("/api")
                .willReturn(badRequest()).build());

        glificRestClient.callAPI("HTTP/1.1 400 Bad Request",
                new ParameterizedTypeReference<GlificResponse<GlificMessageTemplateResponse>>() {
                });
    }

    @Test(expected = GlificConnectException.class)
    public void shouldThrowRegularExceptionsFor200ResponseWithErrors() throws IOException, GlificNotConfiguredException {
        String errorResponse = "{\"errors\":[{\"locations\":[{\"column\":122,\"line\":1}],\"message\":\"Cannot query field \\\"nonExistentItemKey\\\" on type \\\"SessionTemplate\\\".\"}]}";
        stubRequest("/api", errorResponse);

        Object badRequest = mapper.readValue(this.getClass().getResource("/ref/glific/badMessageTemplateRequest.json"), Object.class);

        try {
            glificRestClient.callAPI(badRequest,
                    new ParameterizedTypeReference<GlificResponse<GlificMessageTemplateResponse>>() {
                    });
        } catch (GlificConnectException glificConnectException) {
            assertThat(glificConnectException).hasMessage("Cannot query field \"nonExistentItemKey\" on type \"SessionTemplate\".");
            throw glificConnectException;
        }
    }

    private void stubRequest(String url, String sessionTemplates) {
        wireMockServer.addStubMapping(WireMock.post(url)
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(sessionTemplates)).build());
    }
}
