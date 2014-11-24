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

import static com.abiquo.server.core.cloud.VirtualMachineState.LOCKED;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MultivaluedMap;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.task.TaskDto;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class RestClient
{
    private Client client;

    private final String baseURL;

    private final String apiVersion;

    public RestClient(final String username, final String password, final String baseURL)
    {
        this(username, password, baseURL, SingleResourceTransportDto.API_VERSION);
    }

    public RestClient(final String username, final String password, final String baseURL,
        final String apiVersion)
    {
        checkNotNull(username, "username cannot be null");
        checkNotNull(password, "password cannot be null");
        this.baseURL = checkNotNull(baseURL, "baseURL cannot be null");
        this.apiVersion = checkNotNull(apiVersion, "apiVersion cannot be null");

        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, 0);

        try
        {
            // SSL configuration
            RelaxedSSLConfig sslConfig = new RelaxedSSLConfig();
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] {sslConfig}, new SecureRandom());
            config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                new HTTPSProperties(sslConfig, sslContext));
        }
        catch (NoSuchAlgorithmException | KeyManagementException ex)
        {
            throw Throwables.propagate(ex);
        }

        client = Client.create(config);
        client.addFilter(new HTTPBasicAuthFilter(username, password));
    }

    public <T extends SingleResourceTransportDto> T get(final RESTLink link, final Class<T> clazz)
    {
        return get(link.getHref(), link.getType(), clazz);
    }

    public <T extends SingleResourceTransportDto> T edit(final T dto)
    {
        RESTLink link =
            checkNotNull(dto.getEditLink(), "The given object does not have an edit link");

        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) dto.getClass();

        return put(link.getHref(), link.getType(), link.getType(), dto, clazz);
    }

    public void delete(final SingleResourceTransportDto dto)
    {
        RESTLink link =
            checkNotNull(dto.getEditLink(), "The given object does not have an edit link");
        delete(link.getHref());
    }

    public <T extends SingleResourceTransportDto> T refresh(final T dto)
    {
        RESTLink link = dto.getEditLink();
        if (link == null)
        {
            link = dto.searchLink("self");
        }

        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) dto.getClass();

        checkNotNull(link, "The given object does not have an edit/self link");
        return get(link.getHref(), link.getType(), clazz);
    }

    public <T extends SingleResourceTransportDto> T get(final String uri, final String accept,
        final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept)).get(returnClass);
    }

    public <T extends SingleResourceTransportDto> T get(final String uri, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept)).get(returnType);
    }

    public <T extends SingleResourceTransportDto> T get(final String uri,
        final MultivaluedMap<String, String> queryParams, final String accept,
        final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).queryParams(queryParams).accept(withVersion(accept))
            .get(returnClass);
    }

    public <T extends SingleResourceTransportDto> T get(final String uri,
        final MultivaluedMap<String, String> queryParams, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(absolute(uri)).queryParams(queryParams).accept(withVersion(accept))
            .get(returnType);
    }

    public void delete(final String uri)
    {
        client.resource(absolute(uri)).delete();
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final String contentType, final SingleResourceTransportDto body, final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept))
            .type(withVersion(contentType)).post(returnClass, body);
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final String contentType, final SingleResourceTransportDto body,
        final GenericType<T> returnType)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept))
            .type(withVersion(contentType)).post(returnType, body);
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept)).post(returnClass);
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept)).post(returnType);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept)).put(returnClass);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept)).put(returnType);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final String type, final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept)).type(withVersion(type))
            .put(returnClass);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final String type, final GenericType<T> returnType)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept)).type(withVersion(type))
            .put(returnType);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final String type, final SingleResourceTransportDto body, final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept)).type(withVersion(type))
            .put(returnClass, body);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final String type, final SingleResourceTransportDto body, final GenericType<T> returnType)
    {
        return client.resource(absolute(uri)).accept(withVersion(accept)).type(withVersion(type))
            .put(returnType, body);
    }

    public void put(final String uri, final String accept, final String type,
        final SingleResourceTransportDto body)
    {
        client.resource(absolute(uri)).accept(withVersion(accept)).type(withVersion(type))
            .put(body);
    }

    private static class RelaxedSSLConfig implements X509TrustManager, HostnameVerifier
    {
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType)
            throws CertificateException
        {

        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType)
            throws CertificateException
        {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }

        @Override
        public boolean verify(final String hostname, final SSLSession session)
        {
            return true;
        }

    }

    private String absolute(final String path)
    {
        try
        {
            new URL(path);
        }
        catch (MalformedURLException e)
        {
            return baseURL + (path.startsWith("/") ? path : "/" + path);
        }
        return path;
    }

    private String withVersion(final String mediaType)
    {
        return mediaType.contains("version=") ? mediaType : mediaType + "; version=" + apiVersion;
    }

    public TaskDto waitForTask(final AcceptedRequestDto< ? > acceptedRequest,
        final int pollInterval, final int maxWait, final TimeUnit timeUnit)
    {
        RESTLink status = acceptedRequest.getStatusLink();

        Stopwatch watch = Stopwatch.createStarted();
        while (watch.elapsed(timeUnit) < maxWait)
        {
            TaskDto task = get(status.getHref(), TaskDto.MEDIA_TYPE, TaskDto.class);

            switch (task.getState())
            {
                case FINISHED_SUCCESSFULLY:
                case FINISHED_UNSUCCESSFULLY:
                case ABORTED:
                case ACK_ERROR:
                case CANCELLED:
                    return task;
                case PENDING:
                case QUEUEING:
                case STARTED:
                    // Do nothing and keep waiting
                    break;
            }

            Uninterruptibles.sleepUninterruptibly(pollInterval, timeUnit);
        }

        throw new RuntimeException("Task did not complete in the configured timeout");
    }

    public VirtualMachineDto waitUntilUnlocked(final VirtualMachineDto vm, final int pollInterval,
        final int maxWait, final TimeUnit timeUnit)
    {
        Stopwatch watch = Stopwatch.createStarted();
        while (watch.elapsed(timeUnit) < maxWait)
        {
            VirtualMachineDto refreshed = refresh(vm);
            if (!LOCKED.equals(refreshed.getState()))
            {
                return refreshed;
            }

            Uninterruptibles.sleepUninterruptibly(pollInterval, timeUnit);
        }

        throw new RuntimeException("Virtual machine did not reach the desired state in the configured timeout");
    }

}
