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

import static com.abiquo.apiclient.domain.ApiPath.HYPERVISORTYPES_URL;
import static com.google.common.base.Preconditions.checkNotNull;

import com.abiquo.server.core.cloud.HypervisorTypeDto;
import com.abiquo.server.core.cloud.HypervisorTypesDto;

public class ConfigApi
{
    private final RestClient client;

    // Package private constructor to be used only by the ApiClient
    ConfigApi(final RestClient client)
    {
        this.client = checkNotNull(client, "client cannot be null");
    }

    public HypervisorTypeDto getHypervisorType(final String type)
    {
        return client.get(String.format("%s/%s", HYPERVISORTYPES_URL, type),
            HypervisorTypeDto.MEDIA_TYPE, HypervisorTypeDto.class);
    }

    public Iterable<HypervisorTypeDto> getHypervisorTypes()
    {
        return client.list(HYPERVISORTYPES_URL, HypervisorTypesDto.MEDIA_TYPE,
            HypervisorTypesDto.class);
    }

}
