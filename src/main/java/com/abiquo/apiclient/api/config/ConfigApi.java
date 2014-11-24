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

package com.abiquo.apiclient.api.config;

import static com.abiquo.apiclient.api.ApiPath.LICENSES_URL;

import com.abiquo.apiclient.rest.RestClient;
import com.abiquo.server.core.config.LicenseDto;

public class ConfigApi
{
    private final RestClient client;

    public ConfigApi(final RestClient client)
    {
        this.client = client;

    }

    public LicenseDto addLicense(final String licenseKey)
    {
        LicenseDto license = new LicenseDto();
        license.setCode(licenseKey);
        return client.post(LICENSES_URL, LicenseDto.MEDIA_TYPE, LicenseDto.MEDIA_TYPE, license,
            LicenseDto.class);
    }
}
