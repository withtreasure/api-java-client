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

import static com.abiquo.apiclient.ApiPath.ENTERPRISES_URL;
import static com.abiquo.apiclient.ApiPath.LOGIN_URL;

import com.abiquo.apiclient.RestClient;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.enterprise.EnterprisesDto;
import com.abiquo.server.core.enterprise.UserDto;

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

    public EnterpriseDto getEnterprise(final String id)
    {
        return client.get(ENTERPRISES_URL + id, EnterpriseDto.MEDIA_TYPE, EnterpriseDto.class);
    }

    public EnterprisesDto listEnterprise()
    {
        return client.get(ENTERPRISES_URL, EnterprisesDto.MEDIA_TYPE, EnterprisesDto.class);
    }

    public UserDto getCurrentUser()
    {
        return client.get(LOGIN_URL, UserDto.MEDIA_TYPE, UserDto.class);
    }

}
