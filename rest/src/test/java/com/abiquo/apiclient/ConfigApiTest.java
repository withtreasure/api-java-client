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

import static com.abiquo.apiclient.domain.ApiPath.HYPERVISORTYPES_URL;

import org.testng.annotations.Test;

import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.cloud.HypervisorTypeDto;
import com.abiquo.server.core.cloud.HypervisorTypesDto;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

@Test
public class ConfigApiTest extends BaseMockTest
{

    public void testGetHypervisorType() throws Exception
    {
        MockResponse vdcsResponse = new MockResponse() //
            .setHeader("Content-Type", HypervisorTypeDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("hypervisortype.json"));

        server.enqueue(vdcsResponse);
        server.play();

        newApiClient().getConfigApi().getHypervisorType("AMAZON");

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", String.format("%s/%s", HYPERVISORTYPES_URL, "AMAZON"));
        assertAccept(request, HypervisorTypeDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testGetHypervisorTypes() throws Exception
    {
        MockResponse vdcsResponse = new MockResponse() //
            .setHeader("Content-Type", HypervisorTypesDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("hypervisortypes.json"));

        server.enqueue(vdcsResponse);
        server.play();

        newApiClient().getConfigApi().getHypervisorTypes();

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", HYPERVISORTYPES_URL);
        assertAccept(request, HypervisorTypesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

}
