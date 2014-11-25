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

import static com.abiquo.apiclient.ApiPath.ENTERPRISES_URL;
import static com.abiquo.apiclient.ApiPath.LOGIN_URL;
import static com.abiquo.apiclient.ApiPath.USERS_URL;

import org.testng.annotations.Test;

import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.enterprise.EnterprisesDto;
import com.abiquo.server.core.enterprise.UserDto;
import com.abiquo.server.core.enterprise.UsersDto;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

@Test
public class EnterpriseApiTest extends BaseMockTest
{

    public void testCreateEnterprise() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", EnterpriseDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("ent.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getEnterpriseApi().createEnterprise("Abiquo");

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "POST", ENTERPRISES_URL);
        assertHeader(request, "Accept", EnterpriseDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
        assertHeader(request, "Content-Type", EnterpriseDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    public void testGetEnterprise() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", EnterpriseDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("ent.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getEnterpriseApi().getEnterprise("1");

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", ENTERPRISES_URL + "1");
        assertHeader(request, "Accept", EnterpriseDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    public void testListEnterprises() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", EnterprisesDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("ents.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getEnterpriseApi().listEnterprise();

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", ENTERPRISES_URL);
        assertHeader(request, "Accept", EnterprisesDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    public void testGetCurrentUser() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", UserDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("user.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getEnterpriseApi().getCurrentUser();

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", LOGIN_URL);
        assertHeader(request, "Accept", UserDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    public void testListUsers() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", UsersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("users.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getEnterpriseApi().listUsers();

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", USERS_URL);
        assertHeader(request, "Accept", UsersDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }
}
