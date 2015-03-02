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

import static com.abiquo.apiclient.domain.PageIterator.flatten;
import static com.abiquo.apiclient.domain.options.BaseOptions.urlEncode;
import static com.abiquo.apiclient.util.LogUtils.logRequest;
import static com.abiquo.apiclient.util.LogUtils.logResponse;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.transformValues;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.abiquo.apiclient.ApiClient.SSLConfiguration;
import com.abiquo.apiclient.auth.Authentication;
import com.abiquo.apiclient.domain.exception.AbiquoException;
import com.abiquo.apiclient.domain.exception.AuthorizationException;
import com.abiquo.apiclient.domain.exception.HttpException;
import com.abiquo.apiclient.interceptors.AuthenticationInterceptor;
import com.abiquo.apiclient.json.Json;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.model.transport.WrapperDto;
import com.abiquo.model.transport.error.ErrorsDto;
import com.abiquo.server.core.cloud.VirtualApplianceDto;
import com.abiquo.server.core.cloud.VirtualApplianceState;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.cloud.VirtualMachineState;
import com.abiquo.server.core.task.TaskDto;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.net.HttpHeaders;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Uninterruptibles;
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

    // Package protected. To be used only by the ApiClient
    RestClient(final Authentication authentication, final String baseURL, final String apiVersion,
        final SSLConfiguration sslConfiguration)
    {
        this.json = new Json();
        this.baseURL = checkNotNull(baseURL, "baseURL cannot be null");
        this.apiVersion = checkNotNull(apiVersion, "apiVersion cannot be null");

        client = new OkHttpClient();
        client.setReadTimeout(0, TimeUnit.MILLISECONDS);
        client.networkInterceptors().add(new AuthenticationInterceptor(authentication));

        if (sslConfiguration != null)
        {
            client.setHostnameVerifier(sslConfiguration.hostnameVerifier());
            client.setSslSocketFactory(sslConfiguration.sslContext().getSocketFactory());
        }
    }

    /**
     * Changes made to the returned client will affect all the subsequent requests.
     * <p>
     * If you want to create a new client without affecting the existing one, use the
     * {@link OkHttpClient#clone()} method on the returned client.
     */
    public OkHttpClient rawClient()
    {
        return client;
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

    public <T extends SingleResourceTransportDto, W extends WrapperDto<T>> Iterable<T> list(
        final RESTLink link, final Class<W> clazz)
    {
        return flatten(this, get(link, clazz));
    }

    public <T extends SingleResourceTransportDto, W extends WrapperDto<T>> Iterable<T> list(
        final String uri, final String accept, final Class<W> returnClass)
    {
        return flatten(this, get(uri, accept, returnClass));
    }

    public <T extends SingleResourceTransportDto, W extends WrapperDto<T>> Iterable<T> list(
        final String uri, final String accept, final TypeToken<W> returnType)
    {
        return flatten(this, get(uri, accept, returnType));
    }

    public <T extends SingleResourceTransportDto, W extends WrapperDto<T>> Iterable<T> list(
        final String uri, final Map<String, Object> queryParams, final String accept,
        final Class<W> returnClass)
    {
        return flatten(this, get(uri, queryParams, accept, returnClass));
    }

    public <T extends SingleResourceTransportDto, W extends WrapperDto<T>> Iterable<T> list(
        final String uri, final Map<String, Object> queryParams, final String accept,
        final TypeToken<W> returnType)
    {
        return flatten(this, get(uri, queryParams, accept, returnType));
    }

    public <T extends SingleResourceTransportDto> T get(final RESTLink link, final Class<T> clazz)
    {
        return get(link.getHref(), link.getType(), clazz);
    }

    public <T extends SingleResourceTransportDto> T get(final String uri, final String accept,
        final Class<T> returnClass)
    {
        try
        {
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).get().build();

            return execute(request, returnClass);
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
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).get().build();

            return execute(request, returnType);
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
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).get().build();

            return execute(request, returnClass);
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
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).get().build();

            return execute(request, returnType);
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
            Request request = new Request.Builder().url(absolute(uri)).delete().build();
            execute(request, (Class< ? >) null);
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
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).post(requestBody).build();

            return execute(request, returnClass);
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
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).post(requestBody).build();

            return execute(request, returnType);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final Class<T> returnClass)
    {
        try
        {
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).post(null).build();

            return execute(request, returnClass);
        }
        catch (IOException ex)
        {
            throw Throwables.propagate(ex);
        }
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final String contentType, final String body, final Class<T> returnClass)
    {
        try
        {
            RequestBody requestBody =
                RequestBody.create(MediaType.parse(withVersion(contentType)), body);
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).post(requestBody).build();

            return execute(request, returnClass);
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
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).post(null).build();

            return execute(request, returnType);
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
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).put(requestBody).build();

            return execute(request, returnClass);
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
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).put(null).build();

            return execute(request, returnType);
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
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).put(requestBody).build();

            return execute(request, returnType);
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
            String rawBody = json.write(body);
            RequestBody requestBody =
                RequestBody.create(MediaType.parse(withVersion(contentType)), rawBody);
            Request request =
                new Request.Builder().url(absolute(uri))
                    .addHeader(HttpHeaders.ACCEPT, withVersion(accept)).put(requestBody).build();

            execute(request, (Class< ? >) null);
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
        Map<String, Object> queryParamsSorted = new TreeMap<String, Object>(queryParams);
        return Joiner.on('&').withKeyValueSeparator("=")
            .join(transformValues(queryParamsSorted, new Function<Object, String>()
            {
                @Override
                public String apply(final Object input)
                {
                    return urlEncode(input.toString());
                }
            }));
    }

    public TaskDto waitForTask(final AcceptedRequestDto< ? > acceptedRequest,
        final int pollInterval, final int maxWait, final TimeUnit timeUnit)
    {
        RESTLink status = acceptedRequest.getStatusLink();

        return waitForTask(status, pollInterval, maxWait, timeUnit);
    }

    public TaskDto waitForTask(final TaskDto taskDto, final int pollInterval, final int maxWait,
        final TimeUnit timeUnit)
    {
        return waitForTask(taskDto.searchLink("self"), pollInterval, maxWait, timeUnit);
    }

    private TaskDto waitForTask(final RESTLink restLink, final int pollInterval, final int maxWait,
        final TimeUnit timeUnit)
    {
        final Stopwatch watch = Stopwatch.createStarted();
        while (watch.elapsed(timeUnit) < maxWait)
        {
            TaskDto updatedTask = get(restLink.getHref(), TaskDto.MEDIA_TYPE, TaskDto.class);
            switch (updatedTask.getState())
            {
                case FINISHED_SUCCESSFULLY:
                case FINISHED_UNSUCCESSFULLY:
                case ABORTED:
                case ACK_ERROR:
                case CANCELLED:
                    return updatedTask;
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
            if (!VirtualMachineState.LOCKED.equals(refreshed.getState()))
            {
                return refreshed;
            }

            Uninterruptibles.sleepUninterruptibly(pollInterval, timeUnit);
        }

        throw new RuntimeException("Virtual machine did not reach the desired state in the configured timeout");
    }

    public VirtualApplianceDto waitUntilUnlocked(final VirtualApplianceDto vapp,
        final int pollInterval, final int maxWait, final TimeUnit timeUnit)
    {
        Stopwatch watch = Stopwatch.createStarted();
        while (watch.elapsed(timeUnit) < maxWait)
        {
            VirtualApplianceDto refreshed = refresh(vapp);
            if (!VirtualApplianceState.LOCKED.equals(refreshed.getState()))
            {
                return refreshed;
            }

            Uninterruptibles.sleepUninterruptibly(pollInterval, timeUnit);
        }

        throw new RuntimeException("Virtual appliance did not reach the desired state in the configured timeout");
    }

    private <T> T execute(final Request request, final Class<T> resultClass) throws IOException
    {
        logRequest(request);

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        logResponse(response, responseBody);
        checkResponse(request, response, responseBody);

        return !Strings.isNullOrEmpty(responseBody) && resultClass != null ? json.read(
            responseBody, resultClass) : null;
    }

    private <T> T execute(final Request request, final TypeToken<T> returnType) throws IOException
    {
        logRequest(request);

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        logResponse(response, responseBody);
        checkResponse(request, response, responseBody);

        return !Strings.isNullOrEmpty(responseBody) && returnType != null ? json.read(responseBody,
            returnType) : null;
    }

    private void checkResponse(final Request request, final Response response,
        final String responseBody) throws IOException
    {
        int responseCode = response.code();
        if (responseCode == 401 || responseCode == 403)
        {
            throw new AuthorizationException(responseCode, response.message());
        }
        else if (responseCode >= 400)
        {
            if (responseBody == null)
            {
                throw new HttpException(responseCode, response.message());
            }

            try
            {
                ErrorsDto errors = json.read(responseBody, ErrorsDto.class);
                throw new AbiquoException(responseCode, errors);
            }
            catch (Exception ex)
            {
                Throwables.propagateIfInstanceOf(ex, AbiquoException.class);
                throw new HttpException(responseCode, response.message() + ". Body: "
                    + responseBody);
            }
        }
    }

}
