/**
 * Copyright (C) 2008 - Abiquo Holdings S.L. All rights reserved.
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */

package com.abiquo.apiclient.api.enterprise;

import static com.abiquo.apiclient.api.ApiPath.ENTERPRISES_URL;
import static com.abiquo.apiclient.api.ApiPath.USERS_URL;
import static com.abiquo.apiclient.api.ApiPredicates.enterpriseName;
import static com.abiquo.apiclient.api.ApiPredicates.userName;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.find;

import com.abiquo.apiclient.rest.RestClient;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.enterprise.EnterprisesDto;
import com.abiquo.server.core.enterprise.UserDto;
import com.abiquo.server.core.enterprise.UsersDto;

public class EnterpriseApi
{
    private final RestClient client;

    public EnterpriseApi(final RestClient client)
    {
        this.client = client;

    }

    public EnterpriseDto createEnterprise(final String name)
    {
        EnterpriseDto enterprise = new EnterpriseDto();
        enterprise.setName(name);
        return client.post(ENTERPRISES_URL, EnterpriseDto.MEDIA_TYPE, EnterpriseDto.MEDIA_TYPE,
            enterprise, EnterpriseDto.class);
    }

    public EnterpriseDto findEnterprise(final String name)
    {
        EnterprisesDto enterprises =
            client.get(ENTERPRISES_URL, EnterprisesDto.MEDIA_TYPE, EnterprisesDto.class);
        return find(enterprises.getCollection(), enterpriseName(name));
    }

    public UserDto findUser(final String name)
    {

        UsersDto users = client.get(USERS_URL, UsersDto.MEDIA_TYPE, UsersDto.class);
        return find(users.getCollection(), userName(name));
    }

    public UserDto impersonateEnterprise(final String username, final EnterpriseDto enterprise)
    {
        UserDto user = findUser(username);
        user.modifyLink("enterprise", enterprise.getEditLink().getHref());
        return edit(user);
    }

    public <T extends SingleResourceTransportDto> T edit(final T dto)
    {
        RESTLink link =
            checkNotNull(dto.getEditLink(), "The given object does not have an edit link");

        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) dto.getClass();

        return client.put(link.getHref(), link.getType(), link.getType(), dto, clazz);
    }
}
