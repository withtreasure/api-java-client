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
import static com.abiquo.apiclient.domain.Links.create;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.abiquo.apiclient.domain.options.EnterpriseListOptions;
import com.abiquo.apiclient.domain.options.UserListOptions;
import com.abiquo.model.enumerator.AuthType;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.enterprise.EnterprisesDto;
import com.abiquo.server.core.enterprise.RoleDto;
import com.abiquo.server.core.enterprise.RolesDto;
import com.abiquo.server.core.enterprise.UserDto;
import com.abiquo.server.core.enterprise.UsersDto;
import com.abiquo.server.core.infrastructure.PublicCloudCredentialsDto;
import com.google.common.base.Joiner;

public class EnterpriseApi
{
    private final RestClient client;

    // Package private constructor to be used only by the ApiClient
    EnterpriseApi(final RestClient client)
    {
        this.client = checkNotNull(client, "client cannot be null");
    }

    public EnterpriseDto createEnterprise(final String name)
    {
        EnterpriseDto enterprise = new EnterpriseDto();
        enterprise.setName(name);
        return client.post(ENTERPRISES_URL, EnterpriseDto.MEDIA_TYPE, EnterpriseDto.MEDIA_TYPE,
            enterprise, EnterpriseDto.class);
    }

    public PublicCloudCredentialsDto addPublicCloudCredentials(final EnterpriseDto enterprise,
        final PublicCloudCredentialsDto credentials)
    {

        return client.post(
            String.format("%s/%s/credentials/", ENTERPRISES_URL, enterprise.getId()),
            PublicCloudCredentialsDto.MEDIA_TYPE, PublicCloudCredentialsDto.MEDIA_TYPE,
            credentials, PublicCloudCredentialsDto.class);
    }

    public EnterpriseDto getEnterprise(final String id)
    {
        return client
            .get(ENTERPRISES_URL + "/" + id, EnterpriseDto.MEDIA_TYPE, EnterpriseDto.class);
    }

    public Iterable<EnterpriseDto> listEnterprises()
    {
        return client.list(ENTERPRISES_URL, EnterprisesDto.MEDIA_TYPE, EnterprisesDto.class);
    }

    public Iterable<EnterpriseDto> listEnterprises(final EnterpriseListOptions options)
    {
        return client.list(ENTERPRISES_URL, options.queryParams(), EnterprisesDto.MEDIA_TYPE,
            EnterprisesDto.class);
    }

    public UserDto createUser(final String name, final String surname, final String nick,
        final String password, final String email, final String description, final boolean active,
        final String locale, final AuthType authType, final String publicSshKey,
        final List<Integer> availableVirtualDatacentersIds, final EnterpriseDto enterprise,
        final RoleDto role)
    {
        UserDto user = new UserDto();
        user.setName(name);
        user.setSurname(surname);
        user.setNick(nick);
        user.setPassword(password);
        user.setEmail(email);
        user.setDescription(description);
        user.setActive(active);
        user.setLocale(locale);
        user.setAuthType(authType.toString());
        user.setPublicSshKey(publicSshKey);

        user.setAvailableVirtualDatacenters(Joiner.on(",").skipNulls()
            .join(availableVirtualDatacentersIds));
        user.addLink(create("role", role.getEditLink().getHref(), role.getEditLink().getType()));

        return client.post(enterprise.searchLink("users").getHref(), UserDto.MEDIA_TYPE,
            UserDto.MEDIA_TYPE, user, UserDto.class);
    }

    public UserDto getCurrentUser()
    {
        return client.get(LOGIN_URL, UserDto.MEDIA_TYPE, UserDto.class);
    }

    public Iterable<UserDto> listUsers(final EnterpriseDto enterprise)
    {
        return client.list(enterprise.searchLink("users").getHref(), enterprise.searchLink("users")
            .getType(), UsersDto.class);
    }

    public Iterable<UserDto> listUsers(final EnterpriseDto enterprise, final UserListOptions options)
    {
        return client.list(enterprise.searchLink("users").getHref(), options.queryParams(),
            enterprise.searchLink("users").getType(), UsersDto.class);
    }

    public Iterable<RoleDto> listRoles()
    {
        return client.list(ROLES_URL, RolesDto.MEDIA_TYPE, RolesDto.class);
    }

}
