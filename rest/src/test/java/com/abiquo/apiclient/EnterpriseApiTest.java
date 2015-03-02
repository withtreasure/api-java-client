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

import static com.abiquo.apiclient.domain.ApiPath.ENTERPRISES_URL;
import static com.abiquo.apiclient.domain.ApiPath.LOGIN_URL;
import static com.abiquo.apiclient.domain.ApiPath.ROLES_URL;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.abiquo.apiclient.domain.options.EnterpriseListOptions;
import com.abiquo.apiclient.domain.options.UserListOptions;
import com.abiquo.model.enumerator.AuthType;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.enterprise.EnterprisesDto;
import com.abiquo.server.core.enterprise.RoleDto;
import com.abiquo.server.core.enterprise.RolesDto;
import com.abiquo.server.core.enterprise.UserDto;
import com.abiquo.server.core.enterprise.UsersDto;
import com.abiquo.server.core.infrastructure.PublicCloudCredentialsDto;
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
        assertAccept(request, EnterpriseDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, EnterpriseDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        EnterpriseDto requestBody = readBody(request, EnterpriseDto.class);
        assertEquals(requestBody.getName(), "Abiquo");
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

        assertRequest(request, "GET", ENTERPRISES_URL + "/1");
        assertAccept(request, EnterpriseDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListEnterprises() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", EnterprisesDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("ents.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getEnterpriseApi().listEnterprises();

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", ENTERPRISES_URL);
        assertAccept(request, EnterprisesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListEnterprisesWithOptions() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", EnterprisesDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("ents.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getEnterpriseApi().listEnterprises(
            EnterpriseListOptions.builder().limit(0).idScope(4).build());

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", ENTERPRISES_URL + "?idScope=4&limit=0");
        assertAccept(request, EnterprisesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
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
        assertAccept(request, UserDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);
    }

    public void testListUsers() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", UsersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("users.json"));

        server.enqueue(response);
        server.play();

        EnterpriseDto dto = new EnterpriseDto();
        RESTLink link = new RESTLink("users", "/admin/enterprises/1/users");
        link.setType(UsersDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getEnterpriseApi().listUsers(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/admin/enterprises/1/users");
        assertAccept(request, UsersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListUsersWithOptions() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", UsersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("users.json"));

        server.enqueue(response);
        server.play();

        EnterpriseDto dto = new EnterpriseDto();
        RESTLink link = new RESTLink("users", "/admin/enterprises/1/users");
        link.setType(UsersDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getEnterpriseApi().listUsers(dto,
            UserListOptions.builder().limit(0).connected(true).build());

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/admin/enterprises/1/users?connected=true&limit=0");
        assertAccept(request, UsersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testCreateUser() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", UserDto.SHORT_MEDIA_TYPE_JSON).setBody(
                payloadFromResource("user.json"));

        server.enqueue(response);
        server.play();

        EnterpriseDto enterprise = new EnterpriseDto();
        RESTLink link = new RESTLink("users", "/admin/enterprises/1/users");
        link.setType(UsersDto.SHORT_MEDIA_TYPE_JSON);
        enterprise.addLink(link);

        RoleDto role = new RoleDto();
        link = new RESTLink("edit", "/admin/roles/1");
        link.setType(RoleDto.SHORT_MEDIA_TYPE_JSON);
        role.addLink(link);

        List<Integer> availableVdcsIds = new ArrayList<Integer>();
        availableVdcsIds.add(1);
        availableVdcsIds.add(7);

        newApiClient().getEnterpriseApi().createUser("Cloud", "Administrator", "admin", "foo",
            "e@gmail.com", "Main administrator", true, "en_US", AuthType.ABIQUO, "bar",
            availableVdcsIds, enterprise, role);

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST", "/admin/enterprises/1/users");
        assertAccept(request, UserDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);
        assertContentType(request, UserDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        UserDto requestBody = readBody(request, UserDto.class);
        assertEquals(requestBody.getName(), "Cloud");
        assertEquals(requestBody.getSurname(), "Administrator");
        assertEquals(requestBody.getNick(), "admin");
        assertEquals(requestBody.getPassword(), "foo");
        assertEquals(requestBody.getEmail(), "e@gmail.com");
        assertEquals(requestBody.getDescription(), "Main administrator");
        assertEquals(requestBody.isActive(), true);
        assertEquals(requestBody.getLocale(), "en_US");
        assertEquals(requestBody.getAuthType(), "ABIQUO");
        assertEquals(requestBody.getPublicSshKey(), "bar");
        assertEquals(requestBody.getAvailableVirtualDatacenters(), "1,7");
        assertLinkExist(requestBody, requestBody.searchLink("role").getHref(), "role",
            RoleDto.SHORT_MEDIA_TYPE_JSON);

    }

    public void testListRoles() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", RolesDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("roles.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getEnterpriseApi().listRoles();

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", ROLES_URL);
        assertAccept(request, RolesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testAddPublicCloudCredentials() throws Exception
    {

        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", PublicCloudCredentialsDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("publiccredentials.json"));// Add the respective json

        server.enqueue(response);
        server.play();

        EnterpriseDto enterprise = new EnterpriseDto();
        enterprise.setId(1);

        PublicCloudCredentialsDto credentials = new PublicCloudCredentialsDto();
        // is this useful ?
        credentials.setAccess("providerAccess");
        credentials.setKey("providerKey");

        newApiClient().getEnterpriseApi().addPublicCloudCredentials(enterprise, credentials);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "POST",
            String.format("%s/%s/credentials/", ENTERPRISES_URL, enterprise.getId()));
        assertAccept(request, PublicCloudCredentialsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, PublicCloudCredentialsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        PublicCloudCredentialsDto requestBody = readBody(request, PublicCloudCredentialsDto.class);
        assertEquals(requestBody.getAccess(), "providerAccess");
        assertEquals(requestBody.getKey(), "providerKey");
    }
}
