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
package com.abiquo.apiclient.domain.options;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

public abstract class BaseOptions
{
    private static final Pattern URL_ENCODED_PATTERN = Pattern
        .compile(".*%[a-fA-F0-9][a-fA-F0-9].*");

    private final Map<String, Object> queryParams;

    public Map<String, Object> queryParams()
    {
        return queryParams;
    }

    protected BaseOptions(final Map<String, Object> queryParams)
    {
        this.queryParams =
            ImmutableMap.copyOf(checkNotNull(queryParams, "queryParams cannot be null"));
    }

    protected static void putIfPresent(final String key, final Object value,
        final Map<String, Object> map)
    {
        if (value != null)
        {
            map.put(key, urlEncode(value.toString()));
        }
    }

    public static boolean isUrlEncoded(final String in)
    {
        return URL_ENCODED_PATTERN.matcher(in).matches();
    }

    public static String urlEncode(final String value)
    {
        // Do not double encode
        if (isUrlEncoded(value))
        {
            return value;
        }

        try
        {
            String encoded = URLEncoder.encode(value, "UTF-8");
            // Services do not always handle '+' and '*' characters well, use the
            // well-supported '%20' and '%2A' instead.
            encoded = encoded.replace("+", "%20");
            encoded = encoded.replace("*", "%2A");

            return encoded;
        }
        catch (UnsupportedEncodingException ex)
        {
            throw Throwables.propagate(ex);
        }
    }
}
