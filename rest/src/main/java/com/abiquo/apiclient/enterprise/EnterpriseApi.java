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
package com.abiquo.apiclient.enterprise;

import static com.abiquo.apiclient.domain.ApiPath.ENTERPRISES_URL;
import static com.abiquo.apiclient.domain.ApiPath.LOGIN_URL;
import static com.abiquo.apiclient.domain.ApiPath.USERS_URL;
import static com.google.common.base.Preconditions.checkNotNull;

import com.abiquo.apiclient.RestClient;
import com.abiquo.apiclient.domain.options.EnterpriseListOptions;
import com.abiquo.apiclient.domain.options.UserListOptions;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.enterprise.EnterprisesDto;
import com.abiquo.server.core.enterprise.UserDto;
import com.abiquo.server.core.enterprise.UsersDto;

public class EnterpriseApi
{
    private final RestClient client;

    public EnterpriseApi(final RestClient client)
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

    public UserDto getCurrentUser()
    {
        return client.get(LOGIN_URL, UserDto.MEDIA_TYPE, UserDto.class);
    }

    public Iterable<UserDto> listUsers()
    {
        return client.list(USERS_URL, UsersDto.MEDIA_TYPE, UsersDto.class);
    }

    public Iterable<UserDto> listUsers(final UserListOptions options)
    {
        return client.list(USERS_URL, options.queryParams(), UsersDto.MEDIA_TYPE, UsersDto.class);
    }

}
