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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import com.abiquo.apiclient.auth.Authentication;
import com.abiquo.model.transport.SingleResourceTransportDto;

public class ApiClient
{
    private final RestClient client;

    private final EnterpriseApi enterpriseApi;

    private final InfrastructureApi infrastructureApi;

    private final CloudApi cloudApi;

    private final TemplatesApi templatesApi;

    private final ConfigApi configApi;

    private final String endpoint;

    private final Authentication authentication;

    private final String version;

    private final SSLConfiguration sslConfiguration;

    // Do not use directly. Use the builder.
    private ApiClient(final String endpoint, final Authentication authentication,
        final String version, final SSLConfiguration sslConfiguration)
    {
        client = new RestClient(authentication, endpoint, version, sslConfiguration);
        enterpriseApi = new EnterpriseApi(client);
        infrastructureApi = new InfrastructureApi(client);
        cloudApi = new CloudApi(client);
        templatesApi = new TemplatesApi(client);
        configApi = new ConfigApi(client);

        this.endpoint = endpoint;
        this.authentication = authentication;
        this.version = version;
        this.sslConfiguration = sslConfiguration;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String endpoint;

        private Authentication authentication;

        private String version = SingleResourceTransportDto.API_VERSION;

        private SSLConfiguration sslConfiguration;

        public Builder endpoint(final String endpoint)
        {
            this.endpoint = endpoint;
            return this;
        }

        public Builder authentication(final Authentication authentication)
        {
            this.authentication = authentication;
            return this;
        }

        public Builder version(final String version)
        {
            this.version = version;
            return this;
        }

        public Builder sslConfiguration(final SSLConfiguration sslConfiguration)
        {
            this.sslConfiguration = sslConfiguration;
            return this;
        }

        public ApiClient build()
        {
            return new ApiClient(endpoint, authentication, version, sslConfiguration);
        }
    }

    public Builder toBuilder()
    {
        return builder().endpoint(endpoint).authentication(authentication).version(version)
            .sslConfiguration(sslConfiguration);
    }

    public static interface SSLConfiguration
    {
        /**
         * Provides the SSLContext to be used in the SSL sessions.
         */
        public SSLContext sslContext();

        /**
         * Provides the hostname verifier to be used in the SSL sessions.
         */
        public HostnameVerifier hostnameVerifier();
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public SSLConfiguration getSSLConfiguration()
    {
        return sslConfiguration;
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
