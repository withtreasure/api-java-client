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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

import org.testng.annotations.Test;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.cloud.VirtualDatacenterDto;
import com.abiquo.server.core.cloud.VirtualDatacentersDto;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.cloud.VirtualMachineState;
import com.abiquo.server.core.task.TaskDto;
import com.abiquo.server.core.task.TaskState;
import com.squareup.okhttp.internal.SslContextBuilder;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

@Test
public class RestClientTest extends BaseMockTest
{
    public void testGetAbsoluteLink() throws Exception
    {
        MockResponse vdcsResponse = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdcs.json"));

        server.enqueue(vdcsResponse);
        server.play();

        RESTLink link = new RESTLink("virtualdatacenters", baseUrl() + "/cloud/virtualdatacenters");
        link.setType(VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON);

        newApiClient().getClient().get(link, VirtualDatacentersDto.class);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters");
        assertHeader(request, "Accept", VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    public void testGetRelativeLink() throws Exception
    {
        MockResponse vdcsResponse = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdcs.json"));

        server.enqueue(vdcsResponse);
        server.play();

        RESTLink link = new RESTLink("virtualdatacenters", "/cloud/virtualdatacenters");
        link.setType(VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON);

        newApiClient().getClient().get(link, VirtualDatacentersDto.class);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters");
        assertHeader(request, "Accept", VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    public void testGetWithCustomVersion() throws Exception
    {
        MockResponse vdcsResponse = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdcs.json"));

        server.enqueue(vdcsResponse);
        server.play();

        RESTLink link = new RESTLink("virtualdatacenters", "/cloud/virtualdatacenters");
        link.setType(VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON);

        newApiClient("2.6").getClient().get(link, VirtualDatacentersDto.class);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters");
        assertHeader(request, "Accept", VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON
            + "; version=2.6");
    }

    public void testConnectionFailsIfSSLConfigurationMissing() throws Exception
    {
        server.useHttps(SslContextBuilder.localhost().getSocketFactory(), false);
        server.enqueue(new MockResponse().setResponseCode(204));
        server.play();

        try
        {
            newApiClient().getClient().delete("/");
            fail("SSL handshake should have failed");
        }
        catch (Exception ex)
        {
            assertTrue(ex.getCause() instanceof SSLHandshakeException);
        }
    }

    public void testConnectWithSSL() throws Exception
    {
        server.useHttps(SslContextBuilder.localhost().getSocketFactory(), false);
        server.enqueue(new MockResponse().setResponseCode(204));
        server.play();

        newApiClient(new RelaxedSSLConfig()).getClient().delete("/");

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "DELETE", "/");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "The given object does not have an edit link")
    public void testEditMissingLink() throws Exception
    {
        server.play();
        newApiClient().getClient().edit(new VirtualDatacenterDto());
    }

    public void testEdit() throws Exception
    {
        MockResponse vdcResponse = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdc.json"));

        server.enqueue(vdcResponse);
        server.play();

        VirtualDatacenterDto dto = new VirtualDatacenterDto();
        RESTLink link = new RESTLink("edit", "/cloud/virtualdatacenters/1");
        link.setType(VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getClient().edit(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "PUT", "/cloud/virtualdatacenters/1");
        assertHeader(request, "Accept", VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
        assertHeader(request, "Content-Type", VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON
            + "; version=" + SingleResourceTransportDto.API_VERSION);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "The given object does not have an edit/self link")
    public void testRefreshMissingLink() throws Exception
    {
        server.play();
        newApiClient().getClient().refresh(new VirtualDatacenterDto());
    }

    public void testRefreshWithEditLink() throws Exception
    {
        MockResponse vdcResponse = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdc.json"));

        server.enqueue(vdcResponse);
        server.play();

        VirtualDatacenterDto dto = new VirtualDatacenterDto();
        RESTLink link = new RESTLink("edit", "/cloud/virtualdatacenters/1");
        link.setType(VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getClient().refresh(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters/1");
        assertHeader(request, "Accept", VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    public void testRefreshWithSelfLink() throws Exception
    {
        MockResponse vdcResponse = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdc.json"));

        server.enqueue(vdcResponse);
        server.play();

        VirtualDatacenterDto dto = new VirtualDatacenterDto();
        RESTLink link = new RESTLink("self", "/cloud/virtualdatacenters/1");
        link.setType(VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getClient().refresh(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters/1");
        assertHeader(request, "Accept", VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "The given object does not have an edit link")
    public void testDeleteMissingLink() throws Exception
    {
        server.play();
        newApiClient().getClient().delete(new VirtualDatacenterDto());
    }

    public void testDelete() throws Exception
    {
        server.enqueue(new MockResponse().setResponseCode(204));
        server.play();

        VirtualDatacenterDto dto = new VirtualDatacenterDto();
        RESTLink link = new RESTLink("edit", "/cloud/virtualdatacenters/1");
        link.setType(VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getClient().delete(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "DELETE", "/cloud/virtualdatacenters/1");
    }

    public void testWaitForTask() throws Exception
    {
        TaskDto inProgress = new TaskDto();
        inProgress.setState(TaskState.PENDING);

        TaskDto completed = new TaskDto();
        completed.setState(TaskState.FINISHED_SUCCESSFULLY);

        // Enqueue two tasks: one in progress, and one completed
        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(toJson(inProgress)));
        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(toJson(completed)));
        server.play();

        AcceptedRequestDto<String> dto = new AcceptedRequestDto<String>();
        RESTLink link =
            new RESTLink("status",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/tasks/1");
        link.setType(TaskDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        TaskDto task = newApiClient().getClient().waitForTask(dto, 100, 500, TimeUnit.MILLISECONDS);

        // Verify the returned status is the right one
        assertEquals(task.getState(), TaskState.FINISHED_SUCCESSFULLY);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest first = server.takeRequest();
        assertRequest(first, "GET", dto.getStatusLink().getHref());
        assertHeader(first, "Accept", TaskDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getStatusLink().getHref());
        assertHeader(second, "Accept", TaskDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    public void testWaitForTaskReachesTimeout() throws Exception
    {
        TaskDto inProgress = new TaskDto();
        inProgress.setState(TaskState.PENDING);

        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(toJson(inProgress)));
        server.play();

        AcceptedRequestDto<String> dto = new AcceptedRequestDto<String>();
        RESTLink link =
            new RESTLink("status",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/tasks/1");
        link.setType(TaskDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        try
        {
            newApiClient().getClient().waitForTask(dto, 100, 50, TimeUnit.MILLISECONDS);
            fail("Test should have failed without having reached the desired status in the given timeout");
        }
        catch (Exception ex)
        {
            // Expected exception. Ignore it to verify the recorded requests
            assertEquals(ex.getMessage(), "Task did not complete in the configured timeout");
        }

        // Verify that only one request was made before the method timed out
        assertEquals(server.getRequestCount(), 1);

        // Verify the request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "GET", dto.getStatusLink().getHref());
        assertHeader(request, "Accept", TaskDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    public void testWaitUntilUnlocked() throws Exception
    {
        VirtualMachineDto locked = new VirtualMachineDto();
        locked.setState(VirtualMachineState.LOCKED);

        VirtualMachineDto powerOn = new VirtualMachineDto();
        powerOn.setState(VirtualMachineState.ON);

        // Enqueue two tasks: one in progress, and one completed
        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(toJson(locked)));
        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(toJson(powerOn)));
        server.play();

        VirtualMachineDto dto = new VirtualMachineDto();
        RESTLink link =
            new RESTLink("edit",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1");
        link.setType(VirtualMachineDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        VirtualMachineDto vm =
            newApiClient().getClient().waitUntilUnlocked(dto, 100, 300, TimeUnit.MILLISECONDS);

        // Verify the returned status is the right one
        assertEquals(vm.getState(), VirtualMachineState.ON);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest first = server.takeRequest();
        assertRequest(first, "GET", dto.getEditLink().getHref());
        assertHeader(first, "Accept", VirtualMachineDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getEditLink().getHref());
        assertHeader(second, "Accept", VirtualMachineDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    public void testWaitForUntilUnlockedReachesTimeout() throws Exception
    {
        VirtualMachineDto locked = new VirtualMachineDto();
        locked.setState(VirtualMachineState.LOCKED);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(toJson(locked)));
        server.play();

        VirtualMachineDto dto = new VirtualMachineDto();
        RESTLink link =
            new RESTLink("edit",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1");
        link.setType(VirtualMachineDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        try
        {
            newApiClient().getClient().waitUntilUnlocked(dto, 100, 50, TimeUnit.MILLISECONDS);
            fail("Test should have failed without having reached the desired status in the given timeout");
        }
        catch (Exception ex)
        {
            // Expected exception. Ignore it to verify the recorded requests
            assertEquals(ex.getMessage(),
                "Virtual machine did not reach the desired state in the configured timeout");
        }

        // Verify that only one request was made before the method timed out
        assertEquals(server.getRequestCount(), 1);

        // Verify the request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "GET", dto.getEditLink().getHref());
        assertHeader(request, "Accept", VirtualMachineDto.SHORT_MEDIA_TYPE_JSON + "; version="
            + SingleResourceTransportDto.API_VERSION);
    }

    private static class RelaxedSSLConfig implements SSLConfiguration
    {
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType)
            throws CertificateException
        {

        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType)
            throws CertificateException
        {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }

        @Override
        public boolean verify(final String hostname, final SSLSession session)
        {
            return true;
        }
    }
}
