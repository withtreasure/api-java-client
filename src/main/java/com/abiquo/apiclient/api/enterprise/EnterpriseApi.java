package com.abiquo.apiclient.api.enterprise;

import com.abiquo.apiclient.api.ApiClient;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.enterprise.EnterprisesDto;

public class EnterpriseApi extends ApiClient
{

    public EnterpriseApi(final String baseURL, final String username, final String password)
    {
        super(baseURL, username, password);
    }

    public static EnterpriseDto createEnterprise(final String name)
    {
        EnterpriseDto enterprise = new EnterpriseDto();
        enterprise.setName(name);
        return client.post(absolute("/admin/enterprises"), EnterpriseDto.MEDIA_TYPE,
            EnterpriseDto.MEDIA_TYPE, enterprise, EnterpriseDto.class);
    }

    public static EnterpriseDto findEnterprise(final String name)
    {
        EnterprisesDto enterprises =
            client.get(absolute("/admin/enterprises"), EnterprisesDto.MEDIA_TYPE,
                EnterprisesDto.class);
        return enterprises.getCollection().stream()
            .filter(enterprise -> enterprise.getName().equals(name)).findFirst().get();
    }
}
