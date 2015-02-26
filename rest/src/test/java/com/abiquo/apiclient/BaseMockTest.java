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

import static com.abiquo.apiclient.auth.BasicAuthentication.basic;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.tryFind;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.abiquo.apiclient.ApiClient.SSLConfiguration;
import com.abiquo.apiclient.json.Json;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

public class BaseMockTest
{
    protected static final String DEFAULT_USER = "foo";

    protected static final String DEFAULT_PASS = "bar";

    protected MockWebServer server;

    protected Json json;

    @BeforeSuite
    protected void configureLogger() throws IOException
    {
        try (InputStream in = RestClient.class.getResourceAsStream("/logging.properties"))
        {
            LogManager.getLogManager().readConfiguration(in);
        }
    }

    @BeforeMethod
    protected void setup()
    {
        server = new MockWebServer();
        json = new Json();
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
        return ApiClient.builder() //
            .endpoint(baseUrl()) //
            .authentication(basic(DEFAULT_USER, DEFAULT_PASS)) //
            .version(version) //
            .sslConfiguration(sslConfiguration) //
            .build();
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

    protected static void assertAccept(final RecordedRequest request, final String expectedType,
        final String expectedVersion)
    {
        assertHeader(request, HttpHeaders.ACCEPT, expectedType + "; version=" + expectedVersion);
    }

    protected static void assertContentType(final RecordedRequest request,
        final String expectedType, final String expectedVersion)
    {
        assertHeader(request, HttpHeaders.CONTENT_TYPE, expectedType + "; version="
            + expectedVersion + "; charset=utf-8");
    }

    protected static String payloadFromResource(final String resource) throws IOException
    {
        return Resources.toString(Resources.getResource(resource), Charsets.UTF_8);
    }

    public static RESTLink assertLinkExist(final SingleResourceTransportDto resource,
        final String href, final String expectedRel, final String expectedType)
    {
        Optional<RESTLink> link =
            tryFind(resource.searchLinksByHref(href), new Predicate<RESTLink>()
            {
                @Override
                public boolean apply(final RESTLink input)
                {
                    return expectedRel.equals(input.getRel())
                        && expectedType.equals(input.getType());
                }
            });

        assertTrue(link.isPresent(), String.format(
            "link with 'href' %s 'rel' %s and type '%s' was not found", href, expectedRel,
            expectedType));

        return link.get();
    }

    public <T extends SingleResourceTransportDto> T readBody(final RecordedRequest request,
        final Class<T> clazz) throws IOException
    {
        return json.read(request.getUtf8Body(), clazz);
    }

}
