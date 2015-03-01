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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.testng.annotations.Test;

import com.abiquo.apiclient.ApiClient.SSLConfiguration;
import com.abiquo.apiclient.domain.PageIterator.AdvancingIterable;
import com.abiquo.apiclient.domain.exception.AuthorizationException;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.cloud.VirtualDatacenterDto;
import com.abiquo.server.core.cloud.VirtualDatacentersDto;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.cloud.VirtualMachineState;
import com.abiquo.server.core.task.TaskDto;
import com.abiquo.server.core.task.TaskState;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
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
        assertAccept(request, VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
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
        assertAccept(request, VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListAbsoluteLinkReturnsAnIterable() throws Exception
    {
        MockResponse vdcsResponse = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdcs.json"));

        server.enqueue(vdcsResponse);
        server.play();

        RESTLink link = new RESTLink("virtualdatacenters", baseUrl() + "/cloud/virtualdatacenters");
        link.setType(VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON);

        Iterable<VirtualDatacenterDto> vdcs =
            newApiClient().getClient().list(link, VirtualDatacentersDto.class);

        assertTrue(vdcs instanceof AdvancingIterable);
        assertEquals(AdvancingIterable.class.cast(vdcs).size(), 2);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters");
        assertAccept(request, VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testGetRelativeLinkReturnsAnIterable() throws Exception
    {
        MockResponse vdcsResponse = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdcs.json"));

        server.enqueue(vdcsResponse);
        server.play();

        RESTLink link = new RESTLink("virtualdatacenters", "/cloud/virtualdatacenters");
        link.setType(VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON);

        Iterable<VirtualDatacenterDto> vdcs =
            newApiClient().getClient().list(link, VirtualDatacentersDto.class);

        assertTrue(vdcs instanceof AdvancingIterable);
        assertEquals(AdvancingIterable.class.cast(vdcs).size(), 2);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters");
        assertAccept(request, VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
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
        assertAccept(request, VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON, "2.6");
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
        assertAccept(request, VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
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
        assertAccept(request, VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
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
        assertAccept(request, VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
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

    public void testWaitForTaskAcceptedRequest() throws Exception
    {
        TaskDto inProgress = new TaskDto();
        inProgress.setState(TaskState.PENDING);

        TaskDto completed = new TaskDto();
        completed.setState(TaskState.FINISHED_SUCCESSFULLY);

        // Enqueue two tasks: one in progress, and one completed
        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(json.write(inProgress)));
        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(json.write(completed)));
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
        assertAccept(first, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getStatusLink().getHref());
        assertAccept(second, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);
    }

    public void testWaitForTaskDto() throws Exception
    {
        TaskDto inProgress = new TaskDto();
        inProgress.setState(TaskState.PENDING);

        TaskDto completed = new TaskDto();
        completed.setState(TaskState.FINISHED_SUCCESSFULLY);

        // Enqueue two tasks: one in progress, and one completed
        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(json.write(inProgress)));
        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(json.write(completed)));
        server.play();

        TaskDto dto = new TaskDto();
        RESTLink link =
            new RESTLink("self",
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
        assertRequest(first, "GET", dto.searchLink("self").getHref());
        assertAccept(first, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.searchLink("self").getHref());
        assertAccept(second, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);
    }

    public void testWaitForTaskReachesTimeout() throws Exception
    {
        TaskDto inProgress = new TaskDto();
        inProgress.setState(TaskState.PENDING);

        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(json.write(inProgress)));
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
        assertAccept(request, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);
    }

    public void testWaitUntilUnlocked() throws Exception
    {
        VirtualMachineDto locked = new VirtualMachineDto();
        locked.setState(VirtualMachineState.LOCKED);

        VirtualMachineDto powerOn = new VirtualMachineDto();
        powerOn.setState(VirtualMachineState.ON);

        // Enqueue two tasks: one in progress, and one completed
        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(locked)));
        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(powerOn)));
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
        assertAccept(first, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getEditLink().getHref());
        assertAccept(second, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testWaitForUntilUnlockedReachesTimeout() throws Exception
    {
        VirtualMachineDto locked = new VirtualMachineDto();
        locked.setState(VirtualMachineState.LOCKED);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(locked)));
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
        assertAccept(request, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testQueryParametersAreURLEncoded() throws Exception
    {
        MockResponse vdcsResponse = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdcs.json"));

        server.enqueue(vdcsResponse);
        server.play();

        newApiClient()
            .getClient()
            .get(
                "/cloud/virtualdatacenters",
                ImmutableMap.<String, Object> of("foo", "param to encode", "wildcard",
                    "with*wildcard"), VirtualDatacentersDto.MEDIA_TYPE, VirtualDatacentersDto.class);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET",
            "/cloud/virtualdatacenters?foo=param%20to%20encode&wildcard=with%2Awildcard");
        assertAccept(request, VirtualDatacentersDto.MEDIA_TYPE,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testAuthenticatorDoesNotRetryOn401() throws Exception
    {
        MockResponse response401 =
            new MockResponse().setResponseCode(401).setHeader("WWW-Authenticate",
                "Basic realm=\"Abiquo Api\"");
        // Unused response. Just to make sure MWS does not hang
        MockResponse response200 = new MockResponse();

        server.enqueue(response401);
        server.enqueue(response200);
        server.play();

        try
        {
            newApiClient().getClient().get("/cloud/virtualdatacenters",
                VirtualDatacentersDto.MEDIA_TYPE, VirtualDatacenterDto.class);
            fail("Request should have failed and an AuthorizationException should have been thrown");
        }
        catch (AuthorizationException ex)
        {
            assertEquals(server.getRequestCount(), 1); // Request should not be retried
        }
    }

    public void testAuthenticatorDoesNotRetryOn403() throws Exception
    {
        MockResponse response403 =
            new MockResponse().setResponseCode(403).setHeader("WWW-Authenticate",
                "Basic realm=\"Abiquo Api\"");
        // Unused response. Just to make sure MWS does not hang
        MockResponse response200 = new MockResponse();

        server.enqueue(response403);
        server.enqueue(response200);
        server.play();

        try
        {
            newApiClient().getClient().get("/cloud/virtualdatacenters",
                VirtualDatacentersDto.MEDIA_TYPE, VirtualDatacenterDto.class);
            fail("Request should have failed and an AuthorizationException should have been thrown");
        }
        catch (AuthorizationException ex)
        {
            assertEquals(server.getRequestCount(), 1); // Request should not be retried
        }
    }

    private static class RelaxedSSLConfig implements SSLConfiguration
    {
        @Override
        public SSLContext sslContext()
        {
            try
            {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[] {new X509TrustManager()
                {
                    @Override
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] chain,
                        final String authType) throws CertificateException
                    {

                    }

                    @Override
                    public void checkClientTrusted(final X509Certificate[] chain,
                        final String authType) throws CertificateException
                    {

                    }
                }}, new SecureRandom());

                return sslContext;
            }
            catch (KeyManagementException | NoSuchAlgorithmException ex)
            {
                throw Throwables.propagate(ex);
            }
        }

        @Override
        public HostnameVerifier hostnameVerifier()
        {
            return new HostnameVerifier()
            {
                @Override
                public boolean verify(final String hostname, final SSLSession session)
                {
                    return true;
                }
            };
        }
    }
}
