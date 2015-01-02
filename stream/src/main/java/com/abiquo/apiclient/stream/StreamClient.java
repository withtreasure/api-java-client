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
package com.abiquo.apiclient.stream;

import static org.atmosphere.wasync.Event.CLOSE;
import static org.atmosphere.wasync.Event.ERROR;
import static org.atmosphere.wasync.Event.MESSAGE;

import java.io.Closeable;
import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Request.METHOD;
import org.atmosphere.wasync.Request.TRANSPORT;
import org.atmosphere.wasync.Socket;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.atmosphere.wasync.impl.AtmosphereRequest;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

import com.abiquo.event.json.module.AbiquoModule;
import com.abiquo.event.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Realm;

public class StreamClient implements Closeable
{
    private final AsyncHttpClient asyncClient;

    private final Socket socket;

    private final AtmosphereRequest request;

    private final ObjectMapper json;

    // Do not use directly. Use the builder.
    private StreamClient(final String endpoint, final String username, final String password,
        final SSLConfiguration sslConfiguration)
    {
        AsyncHttpClientConfig.Builder config = new AsyncHttpClientConfig.Builder();
        config.setRequestTimeoutInMs(-1);
        config.setIdleConnectionTimeoutInMs(-1);
        if (sslConfiguration != null)
        {
            config.setHostnameVerifier(sslConfiguration.hostnameVerifier());
            config.setSSLContext(sslConfiguration.sslContext());
        }

        config.setRealm(new Realm.RealmBuilder() //
            .setPrincipal(username) //
            .setPassword(password) //
            .setUsePreemptiveAuth(true) //
            .setScheme(Realm.AuthScheme.BASIC) //
            .build());

        asyncClient = new AsyncHttpClient(config.build());
        AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);

        request =
            client.newRequestBuilder().method(METHOD.GET)
                .uri(endpoint + "?Content-Type=application/json").transport(TRANSPORT.SSE).build();

        socket = client.create(client.newOptionsBuilder().runtime(asyncClient).build());

        json =
            new ObjectMapper().setAnnotationIntrospector( //
                new AnnotationIntrospectorPair(new JacksonAnnotationIntrospector(),
                    new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()))) //
                .registerModule(new AbiquoModule());
    }

    public Observable<Event> events() throws IOException
    {
        socket.open(request);

        return Observable.create(new OnSubscribe<Event>()
        {
            @Override
            public void call(final Subscriber< ? super Event> subscriber)
            {
                socket.on(MESSAGE, new Function<String>()
                {
                    @Override
                    public void on(final String rawEvent)
                    {
                        try
                        {
                            Event event = json.readValue(rawEvent, Event.class);
                            subscriber.onNext(event);
                        }
                        catch (IOException e)
                        {
                            subscriber.onError(e);
                        }
                    }
                }).on(ERROR, new Function<String>()
                {
                    @Override
                    public void on(final String rawEvent)
                    {
                        subscriber.onError(new RuntimeException("Unexpected error: " + rawEvent));
                    }
                }).on(CLOSE, new Function<String>()
                {
                    @Override
                    public void on(final String rawEvent)
                    {
                        subscriber.onCompleted();
                    }
                });
            }
        });
    }

    @Override
    public void close() throws IOException
    {
        asyncClient.close();
        socket.close();
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String endpoint;

        private String username;

        private String password;

        private SSLConfiguration sslConfiguration;

        public Builder endpoint(final String endpoint)
        {
            this.endpoint = endpoint;
            return this;
        }

        public Builder credentials(final String username, final String password)
        {
            this.username = username;
            this.password = password;
            return this;
        }

        public Builder sslConfiguration(final SSLConfiguration sslConfiguration)
        {
            this.sslConfiguration = sslConfiguration;
            return this;
        }

        public StreamClient build()
        {
            return new StreamClient(endpoint, username, password, sslConfiguration);
        }
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

}
