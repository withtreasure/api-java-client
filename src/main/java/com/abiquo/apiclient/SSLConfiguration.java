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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * Minimum information needed to create a trusted SSL connection.
 * 
 * @author Ignasi Barrera
 */
public interface SSLConfiguration
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
