// Copyright (c) YugaByte, Inc.

package com.yugabyte.yw.common;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.yugabyte.yw.common.ApiHelper;
import com.yugabyte.yw.common.SmartKeyEARService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import org.mockito.runners.MockitoJUnitRunner;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import play.libs.Json;

@RunWith(MockitoJUnitRunner.class)
public class SmartKeyEARServiceTest {
    ApiHelper mockApiHelper;
    TestEncryptionAtRestService encryptionService;

    String testKeyProvider = "SMARTKEY";
    String testAlgorithm = "AES";
    int testKeySize = 256;
    UUID testUniUUID = UUID.randomUUID();
    UUID testCustomerUUID = UUID.randomUUID();
    Map<String, String> config = null;

    String mockKid = "9ffd3e51-19e5-41db-ab30-e78910ec743d";
    String mockEncryptionKey = "tcIQ6E6HJu4m3C4NbVf/1yNe/6jYi/0LAYDsIouwcnU=";

    String getKeyMockResponse = String.format(
            "{\n" +
                    "    \"acct_id\": \"f1e307cb-1931-45ca-a0cb-216b7001a4a9\",\n" +
                    "    \"activation_date\": \"20190924T232220Z\",\n" +
                    "    \"created_at\": \"20190924T232220Z\",\n" +
                    "    \"creator\": {\n" +
                    "        \"app\": \"49d3c1b9-20ca-48ef-b82a-94877cfb2f3e\"\n" +
                    "    },\n" +
                    "    \"description\": \"Test Description\",\n" +
                    "    \"enabled\": true,\n" +
                    "    \"key_ops\": [\n" +
                    "        \"EXPORT\",\n" +
                    "        \"APPMANAGEABLE\"\n" +
                    "    ],\n" +
                    "    \"key_size\": 256,\n" +
                    "    \"kid\": \"4da0ddf6-7283-4456-a636-26b4e1171390\",\n" +
                    "    \"lastused_at\": \"19700101T000000Z\",\n" +
                    "    \"name\": \"Test Daniel Object 11\",\n" +
                    "    \"never_exportable\": false,\n" +
                    "    \"obj_type\": \"AES\",\n" +
                    "    \"origin\": \"FortanixHSM\",\n" +
                    "    \"public_only\": false,\n" +
                    "    \"state\": \"Active\",\n" +
                    "    \"value\": \"%s\",\n" +
                    "    \"group_id\": \"bd5260f9-7448-49ff-b0af-276f801227cb\"\n" +
                    "}",
            mockEncryptionKey
    );
    String getKeyListMockResponse = "[]";
    String getAccessTokenMockResponse = "{\n" +
            "    \"token_type\": \"Bearer\",\n" +
            "    \"expires_in\": 18000,\n" +
            "    \"access_token\": \"LVSg7Qcjke2Vw3-VQ3nRpsGRMvQnbHBmsLex-a-Xjcm" +
            "9T4RolbwHHHLWyg0oOmhC2QbH5z4fuwV8EzPC-jmIzA\",\n" +
            "    \"entity_id\": \"49d3c1b9-20ca-48ef-b82a-94877cfb2f3e\"\n" +
            "}";
    String postCreateMockResponse = String.format(
            "{\n" +
                    "    \"acct_id\": \"f1e307cb-1931-45ca-a0cb-216b7001a4a9\",\n" +
                    "    \"activation_date\": \"20190924T232220Z\",\n" +
                    "    \"created_at\": \"20190924T232220Z\",\n" +
                    "    \"creator\": {\n" +
                    "        \"app\": \"49d3c1b9-20ca-48ef-b82a-94877cfb2f3e\"\n" +
                    "    },\n" +
                    "    \"description\": \"Test Description\",\n" +
                    "    \"enabled\": true,\n" +
                    "    \"key_ops\": [\n" +
                    "        \"EXPORT\",\n" +
                    "        \"APPMANAGEABLE\"\n" +
                    "    ],\n" +
                    "    \"key_size\": 256,\n" +
                    "    \"kid\": \"%s\",\n" +
                    "    \"lastused_at\": \"19700101T000000Z\",\n" +
                    "    \"name\": \"Test Daniel Object 11\",\n" +
                    "    \"never_exportable\": false,\n" +
                    "    \"obj_type\": \"AES\",\n" +
                    "    \"origin\": \"FortanixHSM\",\n" +
                    "    \"public_only\": false,\n" +
                    "    \"state\": \"Active\",\n" +
                    "    \"group_id\": \"bd5260f9-7448-49ff-b0af-276f801227cb\"\n" +
                    "}",
            mockKid
    );

    String postCreateMockErroneousResponse = "JSON error: unknown variant `ABC`, expected one " +
            "of `Aes`, `Des`, `Des3`, `Rsa`, `Ec`, `Opaque`, `Hmac`, `Secret`, `Certificate` " +
            "at line 4 column 22";

    ArrayNode keyOps = Json.newArray().add("EXPORT").add("APPMANAGEABLE");
    ObjectNode payload = Json.newObject();

    private class TestEncryptionAtRestService extends SmartKeyEARService {
        TestEncryptionAtRestService() {
            super(mockApiHelper, testKeyProvider);
        }
        @Override
        public ObjectNode getAuthConfig(UUID customerUUID) {
            return Json.newObject()
                    .put("api_key", "test key value")
                    .put("base_url", "api.amer.smartkey.io");
        }
    }

    @Before
    public void setUp() {
        payload.put("name", testUniUUID.toString());
        payload.put("obj_type", testAlgorithm);
        payload.put("key_size", testKeySize);
        payload.set("key_ops", keyOps);
        mockApiHelper = mock(ApiHelper.class);
        when(mockApiHelper.getRequest(
                eq(String.format("https://api.amer.smartkey.io/crypto/v1/keys/%s/export", mockKid)),
                anyMap()
        )).thenReturn(Json.parse(getKeyMockResponse));
        when(mockApiHelper.getRequest(
                eq("https://api.amer.smartkey.io/crypto/v1/keys"),
                anyMap(), anyMap())).thenReturn(Json.parse(getKeyListMockResponse));
        when(mockApiHelper.postRequest(anyString(), eq(null), anyMap()))
                .thenReturn(Json.parse(getAccessTokenMockResponse));
        when(mockApiHelper.postRequest(anyString(), eq(payload), anyMap()))
                .thenReturn(Json.parse(postCreateMockResponse));
        encryptionService = new TestEncryptionAtRestService();
        config = ImmutableMap.of(
                "kms_provider", testKeyProvider,
                "algorithm", testAlgorithm,
                "key_size", Integer.toString(testKeySize)
        );
    }

    @Test
    public void testCreateEncryptionKeyInvalidEncryptionAlgorithm() {
        Map<String, String> testConfig = new HashMap<>(config);
        testConfig.replace("algorithm", "nonsense");
        assertNull(
                encryptionService
                        .createAndRetrieveEncryptionKey(testUniUUID, testCustomerUUID, testConfig)
        );
    }

    @Test
    public void testCreateEncryptionKeyInvalidEncryptionKeySize() {
        Map<String, String> testConfig = new HashMap<>(config);
        testConfig.replace("key_size", "257");
        assertNull(
                encryptionService
                        .createAndRetrieveEncryptionKey(testUniUUID, testCustomerUUID, testConfig)
        );
    }

    @Test
    public void testCreateAndRetrieveEncryptionKeySuccess() {
        String encryptionKey = encryptionService
                .createAndRetrieveEncryptionKey(testUniUUID, testCustomerUUID, config);
        assertEquals(encryptionKey, mockEncryptionKey);
    }

    @Test
    public void testCreateAndRetrieveEncryptionKeyFailure() {
        ObjectNode testPayload = Json.newObject()
                .put("name", testUniUUID.toString())
                .put("obj_type", testAlgorithm)
                .put("key_size", 128);
        testPayload.set("key_ops", keyOps);
        when(mockApiHelper.postRequest(anyString(), eq(testPayload), anyMap()))
                .thenReturn(Json.newObject().put("error", postCreateMockErroneousResponse));
        Map<String, String> testConfig = ImmutableMap.of(
                "kms_provider", testKeyProvider,
                "algorithm", testAlgorithm,
                "key_size", Integer.toString(128)
        );
        assertNull(
                encryptionService
                        .createAndRetrieveEncryptionKey(testUniUUID, testCustomerUUID, testConfig)
        );
    }
}
