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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.net.HttpHeaders;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Request;

/**
 * HTTP Basic Authentication.
 * 
 * @author Ignasi Barrera
 */
public class BasicAuthentication implements Authentication
{
    private final String authHeader;

    // Use the static factory method
    private BasicAuthentication(final String authHeader)
    {
        this.authHeader = checkNotNull(authHeader, "authHeader cannot be null");
    }

    public static BasicAuthentication basic(final String username, final String password)
    {
        return new BasicAuthentication(Credentials.basic(
            checkNotNull(username, "username cannot be null"),
            checkNotNull(password, "password cannot be null")));
    }

    @Override
    public Request authenticate(final Request unauthenticated)
    {
        return unauthenticated.newBuilder().addHeader(HttpHeaders.AUTHORIZATION, authHeader)
            .build();
    }

}
