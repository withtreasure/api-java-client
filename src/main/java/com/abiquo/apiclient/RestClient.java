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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.abiquo.apiclient.json.Json;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.task.TaskDto;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.net.HttpHeaders;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Uninterruptibles;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class RestClient
{
    private final OkHttpClient client;

    private final Json json;

    private final String baseURL;

    private final String apiVersion;

    private final String authHeader;

    // Package protected. To be used only by the ApiClient
    RestClient(final String username, final String password, final String baseURL,
        final String apiVersion, final SSLConfiguration sslConfiguration, final Json json)
    {
        authHeader =
            Credentials.basic(checkNotNull(username, "username cannot be null"),
                checkNotNull(password, "password cannot be null"));
        this.json = checkNotNull(json, "json cannot be null");
        this.baseURL = checkNotNull(baseURL, "baseURL cannot be null");
        this.apiVersion = checkNotNull(apiVersion, "apiVersion cannot be null");

        client = new OkHttpClient();
        client.setReadTimeout(0, TimeUnit.MILLISECONDS);

        if (sslConfiguration != null)
        {
            client.setHostnameVerifier(sslConfiguration.hostnameVerifier());
            client.setSslSocketFactory(sslConfiguration.sslContext().getSocketFactory());
        }

        client.setAuthenticator(new Authenticator()
        {
            @Override
            public Request authenticate(final Proxy proxy, final Response response)
            {
                return response.request().newBuilder()
                    .header(HttpHeaders.AUTHORIZATION, authHeader).build();
            }

            @Override
            public Request authenticateProxy(final Proxy proxy, final Response response)
            {
                return null;
            }
        });
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
        try
        {
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).get().build();

            Response response = client.newCall(request).execute();
            return json.read(response.body().charStream(), returnClass);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public <T extends SingleResourceTransportDto> T get(final String uri, final String accept,
        final TypeToken<T> returnType)
    {
        try
        {
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).get().build();

            Response response = client.newCall(request).execute();
            return json.read(response.body().charStream(), returnType);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public <T extends SingleResourceTransportDto> T get(final String uri,
        final Map<String, Object> queryParams, final String accept, final Class<T> returnClass)
    {
        try
        {
            Request request =
                new Request.Builder().url(absolute(uri) + "?" + queryLine(queryParams))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).get().build();

            Response response = client.newCall(request).execute();
            return json.read(response.body().charStream(), returnClass);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public <T extends SingleResourceTransportDto> T get(final String uri,
        final Map<String, Object> queryParams, final String accept, final TypeToken<T> returnType)
    {
        try
        {
            Request request =
                new Request.Builder().url(absolute(uri) + "?" + queryLine(queryParams))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).get().build();

            Response response = client.newCall(request).execute();
            return json.read(response.body().charStream(), returnType);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public void delete(final String uri)
    {
        try
        {
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader).delete().build();
            client.newCall(request).execute();
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final String contentType, final SingleResourceTransportDto body, final Class<T> returnClass)
    {
        try
        {
            RequestBody requestBody =
                RequestBody.create(MediaType.parse(withVersion(contentType)), json.write(body));
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).post(requestBody).build();

            Response response = client.newCall(request).execute();
            return json.read(response.body().charStream(), returnClass);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final String contentType, final SingleResourceTransportDto body,
        final TypeToken<T> returnType)
    {
        try
        {
            RequestBody requestBody =
                RequestBody.create(MediaType.parse(withVersion(contentType)), json.write(body));
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).post(requestBody).build();

            Response response = client.newCall(request).execute();
            return json.read(response.body().charStream(), returnType);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final TypeToken<T> returnType)
    {
        try
        {
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).post(null).build();

            Response response = client.newCall(request).execute();
            return json.read(response.body().charStream(), returnType);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final String contentType, final SingleResourceTransportDto body, final Class<T> returnClass)
    {
        try
        {
            RequestBody requestBody =
                RequestBody.create(MediaType.parse(withVersion(contentType)), json.write(body));
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).put(requestBody).build();

            Response response = client.newCall(request).execute();
            return json.read(response.body().charStream(), returnClass);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final TypeToken<T> returnType)
    {
        try
        {
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).put(null).build();

            Response response = client.newCall(request).execute();
            return json.read(response.body().charStream(), returnType);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final String contentType, final SingleResourceTransportDto body,
        final TypeToken<T> returnType)
    {
        try
        {
            RequestBody requestBody =
                RequestBody.create(MediaType.parse(withVersion(contentType)), json.write(body));
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).put(requestBody).build();

            Response response = client.newCall(request).execute();
            return json.read(response.body().charStream(), returnType);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public void put(final String uri, final String accept, final String contentType,
        final SingleResourceTransportDto body)
    {
        try
        {
            RequestBody requestBody =
                RequestBody.create(MediaType.parse(withVersion(contentType)), json.write(body));
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.AUTHORIZATION, authHeader)
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).put(requestBody).build();

            client.newCall(request).execute();
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
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

    private String queryLine(final Map<String, Object> queryParams)
    {
        return Joiner.on('&').withKeyValueSeparator("=").join(queryParams);
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
