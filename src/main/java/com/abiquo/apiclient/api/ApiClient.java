package com.abiquo.apiclient.api;

import com.abiquo.apiclient.api.cloud.CloudApi;
import com.abiquo.apiclient.api.config.ConfigApi;
import com.abiquo.apiclient.api.enterprise.EnterpriseApi;
import com.abiquo.apiclient.api.infrastructure.InfrastructureApi;
import com.abiquo.apiclient.api.templates.TemplatesApi;
import com.abiquo.apiclient.rest.RestClient;

public class ApiClient
{
    private final RestClient client;

    private final EnterpriseApi enterpriseApi;

    private final InfrastructureApi infrastructureApi;

    private final CloudApi cloudApi;

    private final TemplatesApi templatesApi;

    private final ConfigApi configApi;

    public ApiClient(final String baseURL, final String username, final String password)
    {
        client = new RestClient(username, password, baseURL);
        enterpriseApi = new EnterpriseApi(client);
        infrastructureApi = new InfrastructureApi(client);
        cloudApi = new CloudApi(client);
        templatesApi = new TemplatesApi(client);
        configApi = new ConfigApi(client);
    }

    public RestClient getClient()
    {
        return client;
    }

    public EnterpriseApi getEnterpriseApi()
    {
        return enterpriseApi;
    }

    public InfrastructureApi getInfrastructureApi()
    {
        return infrastructureApi;
    }

    public CloudApi getCloudApi()
    {
        return cloudApi;
    }

    public TemplatesApi getTemplatesApi()
    {
        return templatesApi;
    }

    public ConfigApi getConfigApi()
    {
        return configApi;
    }

}
