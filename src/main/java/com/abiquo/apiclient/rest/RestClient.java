package com.abiquo.apiclient.rest;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MultivaluedMap;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.google.common.base.Throwables;
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

    public RestClient(final String username, final String password, final String baseURL)
    {
        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, 0);
        this.baseURL = baseURL;
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
            Throwables.propagate(ex);
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
        delete(dto.getEditLink().getHref());
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
        return client.resource(absolute(uri)).accept(accept).get(returnClass);
    }

    public <T extends SingleResourceTransportDto> T get(final String uri, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(absolute(uri)).accept(accept).get(returnType);
    }

    public <T extends SingleResourceTransportDto> T get(final String uri,
        final MultivaluedMap<String, String> queryParams, final String accept,
        final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).queryParams(queryParams).accept(accept)
            .get(returnClass);
    }

    public <T extends SingleResourceTransportDto> T get(final String uri,
        final MultivaluedMap<String, String> queryParams, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(absolute(uri)).queryParams(queryParams).accept(accept)
            .get(returnType);
    }

    public void delete(final String uri)
    {
        client.resource(absolute(uri)).delete();
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final String contentType, final SingleResourceTransportDto body, final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).accept(accept).type(contentType)
            .post(returnClass, body);
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final String contentType, final SingleResourceTransportDto body,
        final GenericType<T> returnType)
    {
        return client.resource(uri).accept(accept).type(contentType).post(returnType, body);
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).accept(accept).post(returnClass);
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(absolute(uri)).accept(accept).post(returnType);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).accept(accept).put(returnClass);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(uri).accept(accept).put(returnType);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final String type, final Class<T> returnClass)
    {
        return client.resource(absolute(uri)).accept(accept).type(type).put(returnClass);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final String type, final GenericType<T> returnType)
    {
        return client.resource(uri).accept(accept).type(type).put(returnType);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final String type, final SingleResourceTransportDto body, final Class<T> returnClass)
    {
        return client.resource(uri).accept(accept).type(type).put(returnClass, body);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final String type, final SingleResourceTransportDto body, final GenericType<T> returnType)
    {
        return client.resource(absolute(uri)).accept(accept).type(type).put(returnType, body);
    }

    public void put(final String uri, final String accept, final String type,
        final SingleResourceTransportDto body)
    {
        client.resource(absolute(uri)).accept(accept).type(type).put(body);
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

}
