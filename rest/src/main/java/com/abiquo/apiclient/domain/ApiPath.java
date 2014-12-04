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
package com.abiquo.apiclient.domain;

public final class ApiPath
{
    public static final String ENTERPRISES_URL = "/admin/enterprises";

    public static final String DATACENTERS_URL = "/admin/datacenters";

    public static final String LOADLEVELRULES_URL = "/admin/rules/machineLoadLevel";

    public static final String PUBLIC_CLOUD_REGIONS_URL = "/admin/publiccloudregions";

    public static final String LOCATIONS_URL = "/cloud/locations";

    public static final String VIRTUALDATACENTERS_URL = "/cloud/virtualdatacenters";

    public static final String LOGIN_URL = "/login";

    public static final String ROLES_URL = "/admin/roles";

    public static final String HYPERVISORTYPES_URL = "/config/hypervisortypes";

    private ApiPath()
    {
        throw new AssertionError("Constant class. Clients shouldn't instantiate it directly.");
    }
}
