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

import com.squareup.okhttp.Request;

/**
 * Generic interface to the different authentication schemes of the Abiquo API.
 * <p>
 * Some authentication schemes such as OAuth will sign the request headers, so the request
 * authentication should be performed just before sending the request over the wire to make sure the
 * underlying HTTP libraries don't add or modify the existing headers (breaking the signature).
 * 
 * @author Ignasi Barrera
 */
public interface Authentication
{
    /**
     * Authenticates the given request.
     * 
     * @param unauthenticated The request to authenticate.
     * @return The authenticated request.
     */
    public Request authenticate(Request unauthenticated);
}
