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
package com.abiquo.apiclient.auth;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.http.HttpRequest;
import okio.Buffer;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

/**
 * OAuth 1.0a authentication.
 * 
 * @author Ignasi Barrera
 */
public class OAuthAuthentication implements Authentication
{
    private final OAuthConsumer oauthConsumer;

    // Use the factory method
    private OAuthAuthentication(final String consumerKey, final String consumerSecret,
        final String accessToken, final String accessTokenSecret)
    {
        oauthConsumer =
            new OkHttpOAuthConsumer(checkNotNull(consumerKey, "consumerKey cannot be null"),
                checkNotNull(consumerSecret, "consumerSecret cannot be null"),
                checkNotNull(accessToken, "accessToken cannot be null"),
                checkNotNull(accessTokenSecret, "accessTokenSecret cannot be null"));
    }

    public static OAuthAuthentication oauth(final String consumerKey, final String consumerSecret,
        final String accessToken, final String accessTokenSecret)
    {
        return new OAuthAuthentication(consumerKey, consumerSecret, accessToken, accessTokenSecret);
    }

    @Override
    public Request authenticate(final Request unauthenticated)
    {
        try
        {
            return (Request) oauthConsumer.sign(unauthenticated).unwrap();
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Unexpected error signing the request", ex);
        }
    }

    private static class OkHttpOAuthConsumer extends AbstractOAuthConsumer
    {
        private static final long serialVersionUID = -585335662715371444L;

        public OkHttpOAuthConsumer(final String consumerKey, final String consumerSecret,
            final String accessToken, final String accessTokenSecret)
        {
            super(consumerKey, consumerSecret);
            setTokenWithSecret(accessToken, accessTokenSecret);
        }

        @Override
        protected HttpRequest wrap(final Object request)
        {
            checkArgument(request instanceof Request, "An OkHttp request is required");
            return new OkHttpRequestAdapter((Request) request);
        }
    }

    private static class OkHttpRequestAdapter implements HttpRequest
    {
        // This has to be mutable as the signature process will modify it
        private Request request;

        public OkHttpRequestAdapter(final Request request)
        {
            this.request = checkNotNull(request, "request cannot be null");
        }

        @Override
        public String getMethod()
        {
            return request.method();
        }

        @Override
        public String getRequestUrl()
        {
            return request.urlString();
        }

        @Override
        public void setRequestUrl(final String url)
        {
            throw new UnsupportedOperationException("Request URL cannot be modified");
        }

        @Override
        public void setHeader(final String name, final String value)
        {
            request = request.newBuilder().addHeader(name, value).build();
        }

        @Override
        public String getHeader(final String name)
        {
            return request.header(name);
        }

        @Override
        public Map<String, String> getAllHeaders()
        {
            Map<String, String> headers = new HashMap<String, String>();
            for (String header : request.headers().names())
            {
                headers.put(header, request.headers().get(header));
            }
            return headers;
        }

        @Override
        public InputStream getMessagePayload() throws IOException
        {
            RequestBody body = request.body();
            if (body == null)
            {
                return null;
            }
            Buffer buf = new Buffer();
            body.writeTo(buf);
            return buf.inputStream();
        }

        @Override
        public String getContentType()
        {
            RequestBody body = request.body();
            if (body == null)
            {
                return null;
            }
            MediaType contentType = body.contentType();
            return contentType == null ? null : contentType.toString();
        }

        @Override
        public Object unwrap()
        {
            return request;
        }

    }

}
