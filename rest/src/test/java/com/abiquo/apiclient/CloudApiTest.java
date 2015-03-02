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

import static com.abiquo.apiclient.domain.ApiPath.VIRTUALDATACENTERS_URL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.abiquo.apiclient.domain.options.ExternalIpListOptions;
import com.abiquo.apiclient.domain.options.VirtualApplianceListOptions;
import com.abiquo.apiclient.domain.options.VirtualDatacenterListOptions;
import com.abiquo.apiclient.domain.options.VirtualMachineListOptions;
import com.abiquo.model.enumerator.NetworkType;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;
import com.abiquo.server.core.cloud.VirtualApplianceDto;
import com.abiquo.server.core.cloud.VirtualApplianceState;
import com.abiquo.server.core.cloud.VirtualAppliancesDto;
import com.abiquo.server.core.cloud.VirtualDatacenterDto;
import com.abiquo.server.core.cloud.VirtualDatacentersDto;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.cloud.VirtualMachineState;
import com.abiquo.server.core.cloud.VirtualMachineStateDto;
import com.abiquo.server.core.cloud.VirtualMachineTaskDto;
import com.abiquo.server.core.cloud.VirtualMachinesDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.infrastructure.network.ExternalIpsDto;
import com.abiquo.server.core.infrastructure.network.VLANNetworkDto;
import com.abiquo.server.core.infrastructure.network.VMNetworkConfigurationsDto;
import com.abiquo.server.core.infrastructure.storage.TierDto;
import com.abiquo.server.core.infrastructure.storage.TiersDto;
import com.abiquo.server.core.infrastructure.storage.VolumeManagementDto;
import com.abiquo.server.core.infrastructure.storage.VolumesManagementDto;
import com.abiquo.server.core.task.TaskDto;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

@Test
public class CloudApiTest extends BaseMockTest
{
    public void testCreateVirtualAppliance() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vapp.json"));

        server.enqueue(response);
        server.play();

        VirtualDatacenterDto dto = new VirtualDatacenterDto();
        RESTLink link = new RESTLink("virtualappliances", "/cloud/virtualdatacenters/1");
        link.setType(VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        newApiClient().getCloudApi().createVirtualAppliance(dto, "VAPP");

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST", VIRTUALDATACENTERS_URL + "/1");
        assertAccept(request, VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        VirtualApplianceDto requestBody = readBody(request, VirtualApplianceDto.class);
        assertEquals(requestBody.getName(), "VAPP");
    }

    public void testCreateVirtualDatacenter() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdc.json"));

        server.enqueue(response);
        server.play();

        DatacenterDto location = new DatacenterDto();
        RESTLink link = new RESTLink("self", "/cloud/locations/1");
        link.setType(DatacenterDto.SHORT_MEDIA_TYPE_JSON);
        location.addLink(link);

        EnterpriseDto enterprise = new EnterpriseDto();
        link = new RESTLink("edit", "/admin/enterprises/1");
        link.setType(EnterpriseDto.SHORT_MEDIA_TYPE_JSON);
        enterprise.addLink(link);

        newApiClient().getCloudApi().createVirtualDatacenter(location, enterprise, "VDC", "KVM",
            "192.168.0.0", "192.168.0.1", "default_private_network");

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST", VIRTUALDATACENTERS_URL);
        assertAccept(request, VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        VirtualDatacenterDto requestBody = readBody(request, VirtualDatacenterDto.class);
        assertEquals(requestBody.getName(), "VDC");
        assertEquals(requestBody.getHypervisorType(), "KVM");

        VLANNetworkDto vlanDto = requestBody.getVlan();
        assertNotNull(vlanDto);
        assertEquals(vlanDto.getName(), "default_private_network");
        assertEquals(vlanDto.getAddress(), "192.168.0.0");
        assertEquals(vlanDto.getGateway(), "192.168.0.1");
        assertEquals(vlanDto.getMask(), new Integer(24));
        assertEquals(vlanDto.getType(), NetworkType.INTERNAL);

        assertLinkExist(requestBody, enterprise.getEditLink().getHref(), "enterprise",
            EnterpriseDto.SHORT_MEDIA_TYPE_JSON);
        assertLinkExist(requestBody, location.searchLink("self").getHref(), "location",
            DatacenterDto.SHORT_MEDIA_TYPE_JSON);
    }

    public void testCreateVirtualMachine() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualMachineDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vm.json"));

        server.enqueue(response);
        server.play();

        VirtualMachineTemplateDto template = new VirtualMachineTemplateDto();
        RESTLink link =
            new RESTLink("edit",
                "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/109");
        link.setType(VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);
        template.addLink(link);

        VirtualApplianceDto vapp = new VirtualApplianceDto();
        link =
            new RESTLink("virtualmachines",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines");
        link.setType(VirtualMachinesDto.SHORT_MEDIA_TYPE_JSON);
        vapp.addLink(link);

        newApiClient().getCloudApi().createVirtualMachine(template, vapp);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "POST",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines");
        assertAccept(request, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        VirtualMachineDto requestBody = readBody(request, VirtualMachineDto.class);
        assertEquals(requestBody.getVdrpEnabled(), Boolean.TRUE);
        assertLinkExist(requestBody, template.getEditLink().getHref(), "virtualmachinetemplate",
            VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);
    }

    public void testCreateVolume() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VolumeManagementDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("volume.json"));

        server.enqueue(response);
        server.play();

        VirtualDatacenterDto vdc = new VirtualDatacenterDto();
        RESTLink link = new RESTLink("volumes", "/cloud/virtualdatacenters/1/volumes");
        link.setType(VolumesManagementDto.SHORT_MEDIA_TYPE_JSON);
        vdc.addLink(link);

        TierDto tier = new TierDto();
        link = new RESTLink("self", "/cloud/virtualdatacenters/1/tiers/1");
        link.setType(TierDto.SHORT_MEDIA_TYPE_JSON);
        tier.addLink(link);

        newApiClient().getCloudApi().createVolume(vdc, "volumeTest", 1024, tier);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "POST", "/cloud/virtualdatacenters/1/volumes");
        assertAccept(request, VolumeManagementDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VolumeManagementDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        VolumeManagementDto requestBody = readBody(request, VolumeManagementDto.class);
        assertEquals(requestBody.getName(), "volumeTest");
        assertEquals(requestBody.getSizeInMB(), 1024);
        assertLinkExist(requestBody, tier.searchLink("self").getHref(), "tier",
            TierDto.SHORT_MEDIA_TYPE_JSON);
    }

    public void testDeployVirtualAppliance() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("acceptedRequest.json"));

        server.enqueue(response);

        VirtualApplianceDto deployed = new VirtualApplianceDto();
        deployed.setState(VirtualApplianceState.DEPLOYED);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(deployed)));

        server.play();

        VirtualApplianceDto dto = new VirtualApplianceDto();
        RESTLink link =
            new RESTLink("deploy", "/cloud/virtualdatacenters/1/virtualappliances/1/action/deploy");
        link.setType(AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        link = new RESTLink("edit", "/cloud/virtualdatacenters/1/virtualappliances/1");
        link.setType(VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        VirtualApplianceDto vapp =
            newApiClient().getCloudApi().deploy(dto, 1, 300, TimeUnit.SECONDS);

        // Verify the returned status is the right one
        assertEquals(vapp.getState(), VirtualApplianceState.DEPLOYED);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/cloud/virtualdatacenters/1/virtualappliances/1/action/deploy?force=false");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getEditLink().getHref());
        assertAccept(second, VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testDeployVirtualApplianceWithForce() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("acceptedRequest.json"));

        server.enqueue(response);

        VirtualApplianceDto deployed = new VirtualApplianceDto();
        deployed.setState(VirtualApplianceState.DEPLOYED);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(deployed)));

        server.play();

        VirtualApplianceDto dto = new VirtualApplianceDto();
        RESTLink link =
            new RESTLink("deploy", "/cloud/virtualdatacenters/1/virtualappliances/1/action/deploy");
        link.setType(AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        link = new RESTLink("edit", "/cloud/virtualdatacenters/1/virtualappliances/1");
        link.setType(VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        VirtualApplianceDto vapp =
            newApiClient().getCloudApi().deploy(dto, true, 1, 300, TimeUnit.SECONDS);

        // Verify the returned status is the right one
        assertEquals(vapp.getState(), VirtualApplianceState.DEPLOYED);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/cloud/virtualdatacenters/1/virtualappliances/1/action/deploy?force=true");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getEditLink().getHref());
        assertAccept(second, VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testUndeployVirtualAppliance() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("acceptedRequest.json"));

        server.enqueue(response);

        VirtualApplianceDto notDeployed = new VirtualApplianceDto();
        notDeployed.setState(VirtualApplianceState.NOT_DEPLOYED);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(notDeployed)));

        server.play();

        VirtualApplianceDto dto = new VirtualApplianceDto();
        RESTLink link =
            new RESTLink("undeploy",
                "/cloud/virtualdatacenters/1/virtualappliances/1/action/undeploy");
        link.setType(AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        link = new RESTLink("edit", "/cloud/virtualdatacenters/1/virtualappliances/1");
        link.setType(VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        VirtualApplianceDto vm =
            newApiClient().getCloudApi().undeploy(dto, 1, 300, TimeUnit.SECONDS);

        // Verify the returned status is the right one
        assertEquals(vm.getState(), VirtualApplianceState.NOT_DEPLOYED);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/cloud/virtualdatacenters/1/virtualappliances/1/action/undeploy");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualMachineTaskDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        VirtualMachineTaskDto requestBody = readBody(request, VirtualMachineTaskDto.class);
        assertEquals(requestBody.getForceUndeploy(), false);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getEditLink().getHref());
        assertAccept(second, VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testUndeployVirtualApplianceWithForce() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("acceptedRequest.json"));

        server.enqueue(response);

        VirtualApplianceDto notDeployed = new VirtualApplianceDto();
        notDeployed.setState(VirtualApplianceState.NOT_DEPLOYED);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(notDeployed)));

        server.play();

        VirtualApplianceDto dto = new VirtualApplianceDto();
        RESTLink link =
            new RESTLink("undeploy",
                "/cloud/virtualdatacenters/1/virtualappliances/1/action/undeploy");
        link.setType(AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        link = new RESTLink("edit", "/cloud/virtualdatacenters/1/virtualappliances/1");
        link.setType(VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        VirtualApplianceDto vm =
            newApiClient().getCloudApi().undeploy(dto, true, 1, 300, TimeUnit.SECONDS);

        // Verify the returned status is the right one
        assertEquals(vm.getState(), VirtualApplianceState.NOT_DEPLOYED);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/cloud/virtualdatacenters/1/virtualappliances/1/action/undeploy");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualMachineTaskDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        VirtualMachineTaskDto requestBody = readBody(request, VirtualMachineTaskDto.class);
        assertEquals(requestBody.getForceUndeploy(), true);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getEditLink().getHref());
        assertAccept(second, VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testDeployVirtualMachine() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("acceptedRequest.json"));

        server.enqueue(response);

        VirtualMachineDto powerOn = new VirtualMachineDto();
        powerOn.setState(VirtualMachineState.ON);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(powerOn)));

        server.play();

        VirtualMachineDto dto = new VirtualMachineDto();
        RESTLink link =
            new RESTLink("deploy",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/deploy");
        link.setType(AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        link =
            new RESTLink("edit",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1");
        link.setType(VirtualMachineDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        VirtualMachineDto vm = newApiClient().getCloudApi().deploy(dto, 1, 300, TimeUnit.SECONDS);

        // Verify the returned status is the right one
        assertEquals(vm.getState(), VirtualMachineState.ON);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/deploy?force=false");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getEditLink().getHref());
        assertAccept(second, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testDeployVirtualMachineWithForce() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("acceptedRequest.json"));

        server.enqueue(response);

        VirtualMachineDto powerOn = new VirtualMachineDto();
        powerOn.setState(VirtualMachineState.ON);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(powerOn)));

        server.play();

        VirtualMachineDto dto = new VirtualMachineDto();
        RESTLink link =
            new RESTLink("deploy",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/deploy");
        link.setType(AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        link =
            new RESTLink("edit",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1");
        link.setType(VirtualMachineDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        VirtualMachineDto vm =
            newApiClient().getCloudApi().deploy(dto, true, 1, 300, TimeUnit.SECONDS);

        // Verify the returned status is the right one
        assertEquals(vm.getState(), VirtualMachineState.ON);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/deploy?force=true");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getEditLink().getHref());
        assertAccept(second, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testUndeployVirtualMachine() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("acceptedRequest.json"));

        server.enqueue(response);

        VirtualMachineDto notAllocated = new VirtualMachineDto();
        notAllocated.setState(VirtualMachineState.NOT_ALLOCATED);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(notAllocated)));

        server.play();

        VirtualMachineDto dto = new VirtualMachineDto();
        RESTLink link =
            new RESTLink("undeploy",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/undeploy");
        link.setType(AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        link =
            new RESTLink("edit",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1");
        link.setType(VirtualMachineDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        VirtualMachineDto vm = newApiClient().getCloudApi().undeploy(dto, 1, 300, TimeUnit.SECONDS);

        // Verify the returned status is the right one
        assertEquals(vm.getState(), VirtualMachineState.NOT_ALLOCATED);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/undeploy");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualMachineTaskDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        VirtualMachineTaskDto requestBody = readBody(request, VirtualMachineTaskDto.class);
        assertEquals(requestBody.getForceUndeploy(), false);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getEditLink().getHref());
        assertAccept(second, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testUndeployVirtualMachineWithForce() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("acceptedRequest.json"));

        server.enqueue(response);

        VirtualMachineDto notAllocated = new VirtualMachineDto();
        notAllocated.setState(VirtualMachineState.NOT_ALLOCATED);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(notAllocated)));

        server.play();

        VirtualMachineDto dto = new VirtualMachineDto();
        RESTLink link =
            new RESTLink("undeploy",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/undeploy");
        link.setType(AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        link =
            new RESTLink("edit",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1");
        link.setType(VirtualMachineDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        VirtualMachineDto vm =
            newApiClient().getCloudApi().undeploy(dto, true, 1, 300, TimeUnit.SECONDS);

        // Verify the returned status is the right one
        assertEquals(vm.getState(), VirtualMachineState.NOT_ALLOCATED);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/undeploy");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualMachineTaskDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        VirtualMachineTaskDto requestBody = readBody(request, VirtualMachineTaskDto.class);
        assertEquals(requestBody.getForceUndeploy(), true);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getEditLink().getHref());
        assertAccept(second, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testEditVirtualMachine() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("acceptedRequest.json"));

        server.enqueue(response);

        VirtualMachineDto notAllocated = new VirtualMachineDto();
        notAllocated.setState(VirtualMachineState.NOT_ALLOCATED);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(notAllocated)));

        server.play();

        VirtualMachineDto dto = new VirtualMachineDto();
        RESTLink link =
            new RESTLink("edit",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1");
        link.setType(VirtualMachineDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        dto.setState(VirtualMachineState.NOT_ALLOCATED);

        VirtualMachineDto vm =
            newApiClient().getCloudApi().editVirtualMachine(dto, 1, 300, TimeUnit.SECONDS);

        // Verify the returned status is the right one
        assertEquals(vm.getState(), VirtualMachineState.NOT_ALLOCATED);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "PUT",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testPowerState() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("acceptedRequest.json"));

        server.enqueue(response);

        VirtualMachineDto powerOff = new VirtualMachineDto();
        powerOff.setState(VirtualMachineState.OFF);

        server.enqueue(new MockResponse().addHeader("Content-type",
            VirtualMachineDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(powerOff)));

        server.play();

        VirtualMachineDto dto = new VirtualMachineDto();
        RESTLink link =
            new RESTLink("state",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/state");
        link.setType(VirtualMachineStateDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        link =
            new RESTLink("edit",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1");
        link.setType(VirtualMachineDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getCloudApi().powerState(dto, VirtualMachineState.OFF, 1, 300,
            TimeUnit.SECONDS);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "PUT",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/state");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualMachineStateDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        VirtualMachineStateDto requestBody = readBody(request, VirtualMachineStateDto.class);
        assertEquals(requestBody.getState(), VirtualMachineState.OFF);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", dto.getEditLink().getHref());
        assertAccept(second, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testGetPrivateNetwork() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VLANNetworkDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("privatenetwork.json"));

        server.enqueue(response);
        server.play();

        VirtualDatacenterDto vdc = new VirtualDatacenterDto();
        vdc.addLink(new RESTLink("privatenetworks", "/cloud/virtualdatacenters/1/privatenetworks"));

        newApiClient().getCloudApi().getPrivateNetwork(vdc, "1");

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters/1/privatenetworks/1");
        assertAccept(request, VLANNetworkDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testGetTask() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", TaskDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("task.json"));

        server.enqueue(response);
        server.play();

        VirtualMachineDto vm = new VirtualMachineDto();
        vm.addLink(new RESTLink("tasks",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/tasks"));

        newApiClient().getCloudApi().getTask(vm, "f9df77b3-2068-4a07-8336-38d4c8235e4d");

        RecordedRequest request = server.takeRequest();

        assertRequest(
            request,
            "GET",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/tasks/f9df77b3-2068-4a07-8336-38d4c8235e4d");
        assertAccept(request, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);
    }

    public void testGetVirtualAppliance() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("vapp.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getCloudApi().getVirtualAppliance("1", "1");
        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters/1/virtualappliances/1");
        assertAccept(request, VirtualApplianceDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testGetVirtualDatacenter() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("vdc.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getCloudApi().getVirtualDatacenter("1");
        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters/1");
        assertAccept(request, VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testGetVirtualMachine() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualMachineDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("vm.json"));

        server.enqueue(response);
        server.play();

        VirtualApplianceDto vapp = new VirtualApplianceDto();
        vapp.addLink(new RESTLink("virtualmachines",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines"));

        newApiClient().getCloudApi().getVirtualMachine(vapp, "1");
        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1");
        assertAccept(request, VirtualMachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testGetVolume() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VolumeManagementDto.SHORT_MEDIA_TYPE_JSON)//
            .setBody(payloadFromResource("volume.json"));

        server.enqueue(response);
        server.play();

        VirtualDatacenterDto vdc = new VirtualDatacenterDto();
        vdc.addLink(new RESTLink("volumes", "/cloud/virtualdatacenters/1/volumes"));

        newApiClient().getCloudApi().getVolume(vdc, "1");
        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters/1/volumes/1");
        assertAccept(request, VolumeManagementDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListExternalIps() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", ExternalIpsDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("externalips.json"));

        server.enqueue(response);
        server.play();

        VirtualDatacenterDto dto = new VirtualDatacenterDto();
        RESTLink link =
            new RESTLink("externalips", "/cloud/virtualdatacenters/1/action/externalips");
        link.setType(ExternalIpsDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getCloudApi().listExternalIps(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters/1/action/externalips");
        assertAccept(request, ExternalIpsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListExternalIpsWithOptions() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", ExternalIpsDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("externalips.json"));

        server.enqueue(response);
        server.play();

        VirtualDatacenterDto dto = new VirtualDatacenterDto();
        RESTLink link =
            new RESTLink("externalips", "/cloud/virtualdatacenters/1/action/externalips");
        link.setType(ExternalIpsDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getCloudApi().listExternalIps(dto,
            ExternalIpListOptions.builder().limit(0).all(true).build());

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET",
            "/cloud/virtualdatacenters/1/action/externalips?all=true&limit=0");
        assertAccept(request, ExternalIpsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListNetworkConfigurations() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VMNetworkConfigurationsDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("networkconfiguration.json"));

        server.enqueue(response);
        server.play();

        VirtualMachineDto dto = new VirtualMachineDto();
        RESTLink link =
            new RESTLink("configurations",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/network/configurations");
        link.setType(VMNetworkConfigurationsDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getCloudApi().listNetworkConfigurations(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/network/configurations");
        assertAccept(request, VMNetworkConfigurationsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListVirtualAppliances() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualAppliancesDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vapps.json"));

        server.enqueue(response);
        server.play();

        VirtualDatacenterDto dto = new VirtualDatacenterDto();
        RESTLink link =
            new RESTLink("virtualappliances", "/cloud/virtualdatacenters/1/virtualappliances");
        link.setType(VirtualAppliancesDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getCloudApi().listVirtualAppliances(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters/1/virtualappliances");
        assertAccept(request, VirtualAppliancesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListVirtualAppliancesWithOptions() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualAppliancesDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vapps.json"));

        server.enqueue(response);
        server.play();

        VirtualDatacenterDto dto = new VirtualDatacenterDto();
        RESTLink link =
            new RESTLink("virtualappliances", "/cloud/virtualdatacenters/1/virtualappliances");
        link.setType(VirtualAppliancesDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getCloudApi().listVirtualAppliances(dto,
            VirtualApplianceListOptions.builder().limit(0).expand("foo").build());

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET",
            "/cloud/virtualdatacenters/1/virtualappliances?expand=foo&limit=0");
        assertAccept(request, VirtualAppliancesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListVirtualDatacenters() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdcs.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getCloudApi().listVirtualDatacenters();

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters");
        assertAccept(request, VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListVirtualDatacentersWithOptions() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vdcs.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getCloudApi().listVirtualDatacenters(
            VirtualDatacenterListOptions.builder().has("foo bar*").limit(0).datacenterId(2)
                .enterpriseId(4).build());

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET",
            "/cloud/virtualdatacenters?datacenter=2&enterprise=4&has=foo%20bar%2A&limit=0");
        assertAccept(request, VirtualDatacentersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListVirtualMachines() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualMachinesDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vms.json"));

        server.enqueue(response);
        server.play();

        VirtualApplianceDto vapp = new VirtualApplianceDto();

        RESTLink link =
            new RESTLink("virtualmachines",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines");
        link.setType(VirtualMachinesDto.SHORT_MEDIA_TYPE_JSON);
        vapp.addLink(link);

        newApiClient().getCloudApi().listVirtualMachines(vapp);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines");
        assertAccept(request, VirtualMachinesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListVirtualMachinesWithOptions() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualMachinesDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("vms.json"));

        server.enqueue(response);
        server.play();

        VirtualApplianceDto vapp = new VirtualApplianceDto();

        RESTLink link =
            new RESTLink("virtualmachines",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines");
        link.setType(VirtualMachinesDto.SHORT_MEDIA_TYPE_JSON);
        vapp.addLink(link);

        newApiClient().getCloudApi().listVirtualMachines(vapp,
            VirtualMachineListOptions.builder().limit(0).key("foo").build());

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines?key=foo&limit=0");
        assertAccept(request, VirtualMachinesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListTiersfromVDC() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", TiersDto.SHORT_MEDIA_TYPE_JSON).setBody(
                payloadFromResource("tiersVDC.json"));

        server.enqueue(response);
        server.play();

        VirtualDatacenterDto dto = new VirtualDatacenterDto();
        RESTLink link = new RESTLink("tiers", "/cloud/virtualdatacenters/1/tiers");
        link.setType(TiersDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getCloudApi().listTiers(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters/1/tiers");
        assertAccept(request, TiersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }
}
