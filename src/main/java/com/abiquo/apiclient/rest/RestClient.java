package com.abiquo.apiclient.rest;

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

    public RestClient(final String username, final String password)
    {
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
            Throwables.propagate(ex);
        }

        client = Client.create(config);
        client.addFilter(new HTTPBasicAuthFilter(username, password));
    }

    public <T extends SingleResourceTransportDto> T get(final String uri, final String accept,
        final Class<T> returnClass)
    {
        return client.resource(uri).accept(accept).get(returnClass);
    }

    public <T extends SingleResourceTransportDto> T get(final String uri, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(uri).accept(accept).get(returnType);
    }

    public <T extends SingleResourceTransportDto> T get(final String uri,
        final MultivaluedMap<String, String> queryParams, final String accept,
        final Class<T> returnClass)
    {
        return client.resource(uri).queryParams(queryParams).accept(accept).get(returnClass);
    }

    public <T extends SingleResourceTransportDto> T get(final String uri,
        final MultivaluedMap<String, String> queryParams, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(uri).queryParams(queryParams).accept(accept).get(returnType);
    }

    public void delete(final String uri)
    {
        client.resource(uri).delete();
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final String contentType, final SingleResourceTransportDto body, final Class<T> returnClass)
    {
        return client.resource(uri).accept(accept).type(contentType).post(returnClass, body);
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
        return client.resource(uri).accept(accept).post(returnClass);
    }

    public <T extends SingleResourceTransportDto> T post(final String uri, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(uri).accept(accept).post(returnType);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final Class<T> returnClass)
    {
        return client.resource(uri).accept(accept).put(returnClass);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final GenericType<T> returnType)
    {
        return client.resource(uri).accept(accept).put(returnType);
    }

    public <T extends SingleResourceTransportDto> T put(final String uri, final String accept,
        final String type, final Class<T> returnClass)
    {
        return client.resource(uri).accept(accept).type(type).put(returnClass);
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
        return client.resource(uri).accept(accept).type(type).put(returnType, body);
    }

    public void put(final String uri, final String accept, final String type,
        final SingleResourceTransportDto body)
    {
        client.resource(uri).accept(accept).type(type).put(body);
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
}
