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

import static com.abiquo.apiclient.domain.PageIterator.flatten;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.testng.annotations.Test;

import com.abiquo.apiclient.BaseMockTest;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.infrastructure.DatacentersDto;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

@Test
public class PageIteratorTest extends BaseMockTest
{
    public void testOnePageDoesNotPerformRequests() throws Exception
    {
        DatacenterDto datacenter = new DatacenterDto();
        DatacenterDto datacenter2 = new DatacenterDto();
        DatacentersDto datacenters = new DatacentersDto();
        datacenters.add(datacenter);
        datacenters.add(datacenter2);

        server.play();

        Iterator<DatacenterDto> it = flatten(newApiClient().getClient(), datacenters).iterator();

        assertTrue(it.hasNext());
        assertEquals(it.next(), datacenter);
        assertTrue(it.hasNext());
        assertEquals(it.next(), datacenter2);
        assertFalse(it.hasNext());

        assertEquals(server.getRequestCount(), 0);
    }

    public void testMultiplePagesAreLazilyFetched() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", DatacentersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("dcs.json"));
        server.enqueue(response);
        server.play();

        DatacenterDto datacenter = new DatacenterDto();
        DatacenterDto datacenter2 = new DatacenterDto();
        DatacentersDto datacenters = new DatacentersDto();
        datacenters.add(datacenter);
        datacenters.add(datacenter2);
        datacenters.addLink(new RESTLink("next", server.getUrl("")
            + "/api/admin/datacenters?startwith=2"));

        Iterator<DatacenterDto> it = flatten(newApiClient().getClient(), datacenters).iterator();

        // First two elements are in the initial page. No request should be performed, as the
        // elements in the second page are still not needed
        assertTrue(it.hasNext());
        assertEquals(it.next(), datacenter);
        assertTrue(it.hasNext());
        assertEquals(it.next(), datacenter2);
        assertEquals(server.getRequestCount(), 0);

        // There is a second page, so it should be fetched now and its elements returned as normal
        // elements in the collection
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertEquals(server.getRequestCount(), 1);

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "GET", "/api/admin/datacenters?startwith=2");
        assertAccept(request, DatacentersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        // After reading the second page, there are no elements left
        assertFalse(it.hasNext());
    }
}
