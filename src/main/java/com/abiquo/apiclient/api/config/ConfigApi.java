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
