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
package com.abiquo.apiclient.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import okio.BufferedSink;
import okio.Okio;

import com.google.common.base.Strings;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class LogUtils
{
    private static final Logger LOG = Logger.getLogger("abiquo.client");

    public static void logRequest(final Request request) throws IOException
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.log(Level.FINE, String.format(">> %s %s", request.method(), request.url()));

            Headers headers = request.headers();
            for (String header : headers.names())
            {
                LOG.log(Level.FINE, String.format(">> %s: %s", header, headers.get(header)));
            }

            RequestBody body = request.body();
            if (body != null)
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try
                {
                    BufferedSink buffer = Okio.buffer(Okio.sink(out));
                    body.writeTo(buffer);
                    buffer.flush();

                    byte[] bytes = out.toByteArray();
                    if (bytes.length > 0)
                    {
                        LOG.log(Level.FINE,
                            String.format(">> Content-Type: %s", body.contentType()));
                        LOG.log(Level.FINE, String.format(">> Body: %s", new String(bytes)));
                    }
                }
                finally
                {
                    out.close();
                }
            }
        }
    }

    public static void logResponse(final Response response, final String body)
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.log(Level.FINE, String.format("<< %d %s", response.code(), response.message()));
            if (!Strings.isNullOrEmpty(body))
            {
                LOG.log(Level.FINE, String.format("<< Body: %s", body));
            }
        }
    }
}
