/**
 * Copyright (C) 2008 Abiquo Holdings S.L.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.abiquo.apiclient;

import static com.abiquo.apiclient.auth.OAuthAuthentication.oauth;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.abiquo.model.transport.SingleResourceTransportDto;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

@Test
public class OAuthAuthenticationTest extends BaseMockTest
{
    private static final String OAUTH_PREFIX = "OAuth ";

    private static final List<String> OAUTH_PARAMS = ImmutableList.of("oauth_consumer_key",
        "oauth_token", "oauth_signature_method", "oauth_signature", "oauth_timestamp",
        "oauth_nonce", "oauth_version");

    public void testOAuthAuthentication() throws Exception
    {
        server.enqueue(new MockResponse());
        server.play();

        ApiClient api = ApiClient.builder() //
            .endpoint(baseUrl()) //
            .authentication( //
                oauth("consumer-key", //
                    "consumer-secret", //
                    "access-token", //
                    "access-token-secret")) //
            .build();

        api.getClient().get("/", "application/json", SingleResourceTransportDto.class);

        RecordedRequest request = server.takeRequest();

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith(OAUTH_PREFIX));

        // Verify the OAuth authentication parameters, but don't verify the values.
        // We trust the underlying OAuth library
        Map<String, String> headerParams =
            Splitter.on(',').omitEmptyStrings().trimResults().withKeyValueSeparator('=')
                .split(authHeader.substring(OAUTH_PREFIX.length()));
        assertTrue(headerParams.keySet().containsAll(OAUTH_PARAMS));
    }

}
