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

import static com.google.common.base.Preconditions.checkState;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.abiquo.model.transport.SingleResourceTransportDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

public class BaseMockTest
{
    protected static final String DEFAULT_USER = "foo";

    protected static final String DEFAULT_PASS = "bar";

    protected MockWebServer server;

    @BeforeMethod
    protected void setup()
    {
        server = new MockWebServer();
    }

    @AfterMethod(alwaysRun = true)
    protected void tearDown() throws IOException
    {
        server.shutdown(); // Should be safe even if not started
    }

    protected String baseUrl()
    {
        return server.getUrl("").toString();
    }

    protected ApiClient newApiClient()
    {
        return newApiClient((SSLConfiguration) null);
    }

    protected ApiClient newApiClient(final String version)
    {
        return newApiClient(version, null);
    }

    protected ApiClient newApiClient(final SSLConfiguration sslConfiguration)
    {
        return newApiClient(SingleResourceTransportDto.API_VERSION, sslConfiguration);
    }

    protected ApiClient newApiClient(final String version, final SSLConfiguration sslConfiguration)
    {
        checkState(server != null, "server has not been initialised");
        return new ApiClient(baseUrl(), DEFAULT_USER, DEFAULT_PASS, version, sslConfiguration);
    }

    protected static void assertHeader(final RecordedRequest request, final String headerName,
        final String expectedValue)
    {
        String value = request.getHeader(headerName);
        assertNotNull(value, headerName + " header was not present");
        assertEquals(value, expectedValue);
    }

    protected static void assertAuthentication(final RecordedRequest request)
    {
        assertHeader(request, "Authorization", "Basic Zm9vOmJhcg=="); // base64(DEFAULT_USER:DEFAULT_PASS)
    }

    protected static void assertRequest(final RecordedRequest request, final String method,
        final String path)
    {
        assertAuthentication(request);
        assertEquals(request.getMethod(), method);
        assertEquals(request.getPath(), path);
    }

    protected static String payloadFromResource(final String resource) throws IOException
    {
        return Resources.toString(Resources.getResource(resource), Charsets.UTF_8);
    }

    protected static String toJson(final SingleResourceTransportDto dto)
        throws JsonProcessingException
    {
        return new ObjectMapper().setAnnotationIntrospector(
            new AnnotationIntrospectorPair(new JacksonAnnotationIntrospector(),
                new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()))).writeValueAsString(
            dto);
    }

}
