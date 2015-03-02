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

import static com.abiquo.apiclient.domain.ApiPath.DATACENTERS_URL;
import static com.abiquo.apiclient.domain.ApiPath.HYPERVISORTYPES_URL;
import static com.abiquo.apiclient.domain.ApiPath.PUBLIC_CLOUD_REGIONS_URL;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.abiquo.apiclient.domain.options.DatacenterListOptions;
import com.abiquo.apiclient.domain.options.PublicCloudRegionListOptions;
import com.abiquo.model.enumerator.NetworkType;
import com.abiquo.model.enumerator.RemoteServiceType;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.enterprise.DatacenterLimitsDto;
import com.abiquo.server.core.enterprise.DatacentersLimitsDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.infrastructure.DatacentersDto;
import com.abiquo.server.core.infrastructure.MachineDto;
import com.abiquo.server.core.infrastructure.MachinesDto;
import com.abiquo.server.core.infrastructure.PublicCloudRegionDto;
import com.abiquo.server.core.infrastructure.PublicCloudRegionsDto;
import com.abiquo.server.core.infrastructure.RackDto;
import com.abiquo.server.core.infrastructure.RacksDto;
import com.abiquo.server.core.infrastructure.RemoteServiceDto;
import com.abiquo.server.core.infrastructure.RemoteServicesDto;
import com.abiquo.server.core.infrastructure.network.NetworkServiceTypeDto;
import com.abiquo.server.core.infrastructure.network.NetworkServiceTypesDto;
import com.abiquo.server.core.infrastructure.network.VLANNetworkDto;
import com.abiquo.server.core.infrastructure.network.VLANNetworksDto;
import com.abiquo.server.core.infrastructure.storage.StorageDeviceDto;
import com.abiquo.server.core.infrastructure.storage.StorageDevicesDto;
import com.abiquo.server.core.infrastructure.storage.StoragePoolDto;
import com.abiquo.server.core.infrastructure.storage.StoragePoolsDto;
import com.abiquo.server.core.infrastructure.storage.TierDto;
import com.abiquo.server.core.infrastructure.storage.TiersDto;
import com.abiquo.server.core.scheduler.MachineLoadRuleDto;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

@Test
public class InfrastructureApiTest extends BaseMockTest
{
    public void testListDatacenters() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", DatacentersDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("dcs.json"));

        server.enqueue(response);
        server.play();

        RESTLink link = new RESTLink("datacenters", baseUrl() + DATACENTERS_URL);
        link.setType(DatacentersDto.SHORT_MEDIA_TYPE_JSON);

        newApiClient().getInfrastructureApi().listDatacenters();

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", DATACENTERS_URL);
        assertAccept(request, DatacentersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListDatacentersWithOptions() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", DatacentersDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("dcs.json"));

        server.enqueue(response);
        server.play();

        RESTLink link = new RESTLink("datacenters", baseUrl() + DATACENTERS_URL);
        link.setType(DatacentersDto.SHORT_MEDIA_TYPE_JSON);

        newApiClient().getInfrastructureApi().listDatacenters(
            DatacenterListOptions.builder().limit(0).pricing(5).build());

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", DATACENTERS_URL + "?limit=0&pricing=5");
        assertAccept(request, DatacentersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testCreateDatacenter() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", DatacenterDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("dc.json"));

        server.enqueue(response);
        server.play();

        RemoteServicesDto listRs = new RemoteServicesDto();
        RemoteServiceDto rs = new RemoteServiceDto();
        rs.setType(RemoteServiceType.VIRTUAL_SYSTEM_MONITOR);
        listRs.add(rs);

        newApiClient().getInfrastructureApi().createDatacenter("Private Datacenter Berlin",
            "Berlin", listRs.getCollection());

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "POST", DATACENTERS_URL);
        assertAccept(request, DatacenterDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, DatacenterDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        DatacenterDto requestBody = readBody(request, DatacenterDto.class);
        assertEquals(requestBody.getName(), "Private Datacenter Berlin");
        assertEquals(requestBody.getLocation(), "Berlin");
        assertEquals(requestBody.getRemoteServices().getCollection().get(0).getType(),
            RemoteServiceType.VIRTUAL_SYSTEM_MONITOR);
    }

    public void testAddDatacenterToEnteprise() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", DatacenterLimitsDto.SHORT_MEDIA_TYPE_JSON);

        server.enqueue(response);
        server.play();

        EnterpriseDto enterprise = new EnterpriseDto();
        RESTLink limitsLink = new RESTLink("limits", "/admin/enterprises/1/action/limits");
        limitsLink.setType(DatacenterLimitsDto.SHORT_MEDIA_TYPE_JSON);
        enterprise.addLink(limitsLink);
        DatacenterDto datacenter = new DatacenterDto();
        RESTLink editLink = new RESTLink("edit", "/admin/datacenters/1");
        editLink.setType(DatacenterDto.SHORT_MEDIA_TYPE_JSON);
        datacenter.addLink(editLink);

        newApiClient().getInfrastructureApi().addDatacenterToEnterprise(enterprise, datacenter);

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST", "/admin/enterprises/1/action/limits");
        assertAccept(request, DatacenterLimitsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, DatacenterLimitsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        DatacenterLimitsDto requestBody = readBody(request, DatacenterLimitsDto.class);
        assertLinkExist(requestBody, datacenter.getEditLink().getHref(), "location",
            DatacenterDto.SHORT_MEDIA_TYPE_JSON);
    }

    public void testCreateRack() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", RackDto.SHORT_MEDIA_TYPE_JSON).setBody(
                payloadFromResource("rack.json"));

        server.enqueue(response);
        server.play();

        DatacenterDto datacenter = new DatacenterDto();
        RESTLink link = new RESTLink("racks", "/admin/datacenters/1/racks");
        link.setType(RacksDto.SHORT_MEDIA_TYPE_JSON);
        datacenter.addLink(link);

        newApiClient().getInfrastructureApi().createRack(datacenter, "KVM");

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST", "/admin/datacenters/1/racks");
        assertAccept(request, RackDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);
        assertContentType(request, RackDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        RackDto requestBody = readBody(request, RackDto.class);
        assertEquals(requestBody.getName(), "KVM");
    }

    public void testCreateMachine() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", MachineDto.SHORT_MEDIA_TYPE_JSON).setBody(
                payloadFromResource("machine.json"));

        server.enqueue(response);
        server.play();

        RackDto rack = new RackDto();
        RESTLink link = new RESTLink("machines", "/admin/datacenters/1/racks/1/machines");
        link.setType(MachinesDto.SHORT_MEDIA_TYPE_JSON);
        rack.addLink(link);

        MachineDto machine = new MachineDto();
        machine.setIp("10.60.11.210");
        machine.setType("KVM");

        newApiClient().getInfrastructureApi().createMachine(rack, machine);

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST", "/admin/datacenters/1/racks/1/machines");
        assertAccept(request, MachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, MachineDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        MachineDto requestBody = readBody(request, MachineDto.class);
        assertEquals(requestBody.getIp(), "10.60.11.210");
        assertEquals(requestBody.getType(), "KVM");
    }

    public void testDiscoverMachines() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", MachinesDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("machines.json"));

        server.enqueue(response);
        server.play();

        DatacenterDto datacenter = new DatacenterDto();
        RESTLink link = new RESTLink("discover", "/admin/datacenters/1/action/discover");
        link.setType(MachinesDto.SHORT_MEDIA_TYPE_JSON);
        datacenter.addLink(link);

        newApiClient().getInfrastructureApi().discoverMachines(datacenter, "KVM", "10.60.12.4",
            "foo", "pwd");

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "GET",
            "/admin/datacenters/1/action/discover?hypervisor=KVM&ip=10.60.12.4&password=pwd&user=foo");
        assertAccept(request, MachinesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testCreateDevice() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", StorageDeviceDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("device.json"));

        server.enqueue(response);
        server.play();

        DatacenterDto dto = new DatacenterDto();
        RESTLink link = new RESTLink("devices", "/admin/datacenters/1/storage/devices");
        link.setType(StorageDevicesDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        link = new RESTLink("edit", "/admin/datacenters/1");
        link.setType(DatacenterDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getInfrastructureApi().createDevice(dto, "JC-Storage Device", "LVM",
            "10.10.10.10", 8180, "10.10.10.12", 8181, "foo", "bar");

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST", "/admin/datacenters/1/storage/devices");
        assertAccept(request, StorageDeviceDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, StorageDeviceDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        StorageDeviceDto requestBody = readBody(request, StorageDeviceDto.class);
        assertEquals(requestBody.getName(), "JC-Storage Device");
        assertEquals(requestBody.getStorageTechnology(), "LVM");
        assertEquals(requestBody.getManagementIp(), "10.10.10.10");
        assertEquals(requestBody.getManagementPort(), 8180);
        assertEquals(requestBody.getServiceIp(), "10.10.10.12");
        assertEquals(requestBody.getServicePort(), 8181);
        assertEquals(requestBody.getUsername(), "foo");
        assertEquals(requestBody.getPassword(), "bar");
        assertLinkExist(requestBody, dto.getEditLink().getHref(), "datacenter",
            DatacenterDto.SHORT_MEDIA_TYPE_JSON);
    }

    public void testListExternalNetworks() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", VLANNetworksDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("externalnetworks.json"));

        server.enqueue(response);
        server.play();

        DatacenterLimitsDto limits = new DatacenterLimitsDto();
        RESTLink link =
            new RESTLink("externalnetworks", "/admin/enterprises/1/limits/1/externalnetworks");
        link.setType(VLANNetworksDto.SHORT_MEDIA_TYPE_JSON);
        limits.addLink(link);

        newApiClient().getInfrastructureApi().listExternalNetworks(limits);

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "GET", "/admin/enterprises/1/limits/1/externalnetworks");
        assertAccept(request, VLANNetworksDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testCreateExternalNetwork() throws Exception
    {
        server.enqueue(new MockResponse().addHeader("Content-type",
            VLANNetworkDto.SHORT_MEDIA_TYPE_JSON).setBody(
            payloadFromResource("externalnetwork.json")));

        server.play();

        EnterpriseDto enterprise = new EnterpriseDto();
        RESTLink editLink = new RESTLink("edit", "/admin/enterprises/1");
        editLink.setType(EnterpriseDto.SHORT_MEDIA_TYPE_JSON);
        enterprise.addLink(editLink);
        DatacenterDto datacenter = new DatacenterDto();
        RESTLink networkservicetypes =
            new RESTLink("networkservicetypes", "/admin/datacenters/1/networkservicetypes");
        networkservicetypes.setType(NetworkServiceTypesDto.SHORT_MEDIA_TYPE_JSON);
        datacenter.addLink(networkservicetypes);
        RESTLink network = new RESTLink("network", "/admin/datacenters/1/network");
        network.setType(VLANNetworksDto.SHORT_MEDIA_TYPE_JSON);
        datacenter.addLink(network);

        NetworkServiceTypeDto nst = new NetworkServiceTypeDto();
        RESTLink nstlink = new RESTLink("edit", "/admin/datacenters/1/networkservicetypes/1");
        nstlink.setType(NetworkServiceTypeDto.SHORT_MEDIA_TYPE_JSON);
        nst.addLink(nstlink);

        newApiClient().getInfrastructureApi().createExternalNetwork(datacenter, nst, enterprise,
            "ext1", "10.10.10.30", "10.10.10.1", 24, 1);

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST", "/admin/datacenters/1/network");
        assertAccept(request, VLANNetworkDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VLANNetworkDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        VLANNetworkDto requestBody = readBody(request, VLANNetworkDto.class);
        assertEquals(requestBody.getName(), "ext1");
        assertEquals(requestBody.getAddress(), "10.10.10.30");
        assertEquals(requestBody.getGateway(), "10.10.10.1");
        assertEquals(requestBody.getMask(), new Integer(24));
        assertEquals(requestBody.getTag(), new Integer(1));
        assertEquals(requestBody.getType(), NetworkType.EXTERNAL);
        assertLinkExist(requestBody, enterprise.getEditLink().getHref(), "enterprise",
            EnterpriseDto.SHORT_MEDIA_TYPE_JSON);
        assertLinkExist(requestBody, requestBody.searchLink("networkservicetype").getHref(),
            "networkservicetype", NetworkServiceTypeDto.SHORT_MEDIA_TYPE_JSON);
    }

    public void testCreatePool() throws Exception
    {
        server.enqueue(new MockResponse().setHeader("Content-Type",
            StoragePoolsDto.SHORT_MEDIA_TYPE_JSON).setBody(payloadFromResource("pools.json")));

        server.enqueue(new MockResponse().addHeader("Content-type", TiersDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(payloadFromResource("tiersDC.json")));

        server.enqueue(new MockResponse().addHeader("Content-type",
            StoragePoolDto.SHORT_MEDIA_TYPE_JSON).setBody(payloadFromResource("pool.json")));

        server.play();

        DatacenterDto datacenter = new DatacenterDto();
        RESTLink link = new RESTLink("tiers", "/admin/datacenters/1/storage/tiers");
        link.setType(TiersDto.SHORT_MEDIA_TYPE_JSON);
        datacenter.addLink(link);

        StorageDeviceDto storage = new StorageDeviceDto();
        RESTLink pools = new RESTLink("pools", "/admin/datacenters/1/storage/devices/1/pools");
        pools.setType(StoragePoolsDto.SHORT_MEDIA_TYPE_JSON);
        storage.addLink(pools);

        newApiClient().getInfrastructureApi().createPool(datacenter, storage, "zpool", "Nexenta");

        assertEquals(server.getRequestCount(), 3);

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "GET", "/admin/datacenters/1/storage/devices/1/pools?sync=true");
        assertAccept(request, StoragePoolsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET", "/admin/datacenters/1/storage/tiers");
        assertAccept(second, TiersDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);

        RecordedRequest third = server.takeRequest();
        assertRequest(third, "POST", "/admin/datacenters/1/storage/devices/1/pools");
        assertAccept(third, StoragePoolDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(third, StoragePoolDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        StoragePoolDto requestBody = readBody(third, StoragePoolDto.class);
        assertEquals(requestBody.getName(), "zpool");
        assertEquals(requestBody.getEnabled(), true);
        assertLinkExist(requestBody, requestBody.searchLink("tier").getHref(), "tier",
            TierDto.SHORT_MEDIA_TYPE_JSON);
    }

    public void testListRemoteServices() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", RemoteServicesDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("remoteservices.json"));

        server.enqueue(response);
        server.play();

        DatacenterDto dto = new DatacenterDto();
        RESTLink link = new RESTLink("remoteservices", "/admin/datacenters/1/remoteservices");
        link.setType(RemoteServicesDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getInfrastructureApi().listRemoteServices(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/admin/datacenters/1/remoteservices");
        assertAccept(request, RemoteServicesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListRacks() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", RacksDto.SHORT_MEDIA_TYPE_JSON).setBody(
                payloadFromResource("racks.json"));

        server.enqueue(response);
        server.play();

        DatacenterDto dto = new DatacenterDto();
        RESTLink link = new RESTLink("racks", "/admin/datacenters/1/racks");
        link.setType(RacksDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getInfrastructureApi().listRacks(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/admin/datacenters/1/racks");
        assertAccept(request, RacksDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListLimits() throws Exception
    {
        MockResponse response =
            new MockResponse()
                .setHeader("Content-Type", DatacentersLimitsDto.SHORT_MEDIA_TYPE_JSON).setBody(
                    payloadFromResource("limits.json"));

        server.enqueue(response);
        server.play();

        EnterpriseDto dto = new EnterpriseDto();
        RESTLink link = new RESTLink("limits", "/admin/datacenters/enterprises/1/limits");
        link.setType(DatacentersLimitsDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getInfrastructureApi().listLimits(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/admin/datacenters/enterprises/1/limits");
        assertAccept(request, DatacentersLimitsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testGetEnterpriseLimitsForDatacenter() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", DatacenterLimitsDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("limits.json"));

        server.enqueue(response);
        server.play();

        DatacenterDto datacenter = new DatacenterDto();
        datacenter.setId(4);

        EnterpriseDto enterprise = new EnterpriseDto();
        RESTLink link = new RESTLink("limits", "/admin/enterprises/1/limits");
        link.setType(DatacentersLimitsDto.SHORT_MEDIA_TYPE_JSON);
        enterprise.addLink(link);

        newApiClient().getInfrastructureApi().getEnterpriseLimitsForDatacenter(enterprise,
            datacenter);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/admin/enterprises/1/limits?datacenter=4");
        assertAccept(request, DatacentersLimitsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListPools() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", StoragePoolsDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("pools.json"));

        server.enqueue(response);
        server.play();

        StorageDeviceDto dto = new StorageDeviceDto();
        RESTLink link = new RESTLink("pools", "/admin/datacenters/1/storage/devices/4/pools");
        link.setType(StoragePoolsDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getInfrastructureApi().listPools(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/admin/datacenters/1/storage/devices/4/pools");
        assertAccept(request, StoragePoolsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListRemotePools() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", StoragePoolsDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("pools.json"));

        server.enqueue(response);
        server.play();

        StorageDeviceDto dto = new StorageDeviceDto();
        RESTLink link = new RESTLink("pools", "/admin/datacenters/1/storage/devices/4/pools");
        link.setType(StoragePoolsDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getInfrastructureApi().listRemotePools(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/admin/datacenters/1/storage/devices/4/pools?sync=true");
        assertAccept(request, StoragePoolsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListDevices() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", StorageDevicesDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("devices.json"));

        server.enqueue(response);
        server.play();

        DatacenterDto dto = new DatacenterDto();
        RESTLink link = new RESTLink("devices", "/admin/datacenters/1/storage/devices");
        link.setType(StorageDevicesDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getInfrastructureApi().listDevices(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/admin/datacenters/1/storage/devices");
        assertAccept(request, StorageDevicesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListTiersfromDC() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", TiersDto.SHORT_MEDIA_TYPE_JSON).setBody(
                payloadFromResource("tiersDC.json"));

        server.enqueue(response);
        server.play();

        DatacenterDto dto = new DatacenterDto();
        RESTLink link = new RESTLink("tiers", "/admin/datacenters/1/storage/tiers");
        link.setType(TiersDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getInfrastructureApi().listTiers(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/admin/datacenters/1/storage/tiers");
        assertAccept(request, TiersDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListNetworkServiceTypes() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type",
                NetworkServiceTypesDto.SHORT_MEDIA_TYPE_JSON).setBody(
                payloadFromResource("networkservicetypes.json"));

        server.enqueue(response);
        server.play();

        DatacenterDto dto = new DatacenterDto();
        RESTLink link =
            new RESTLink("networkservicetypes", "/admin/datacenters/1/networkservicetypes");
        link.setType(NetworkServiceTypesDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getInfrastructureApi().listNetworkServiceTypes(dto);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/admin/datacenters/1/networkservicetypes");
        assertAccept(request, NetworkServiceTypesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testCreateDatacenterLoadLevelRule() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", MachineLoadRuleDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("loadlevel.json"));
        server.enqueue(response);
        server.play();

        DatacenterDto datacenter = new DatacenterDto();
        RESTLink link = new RESTLink("edit", "/admin/datacenters/1");
        link.setType(DatacenterDto.SHORT_MEDIA_TYPE_JSON);
        datacenter.addLink(link);

        newApiClient().getInfrastructureApi().createDatacenterLoadLevelRule(datacenter, 25, 75);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "POST", "/admin/rules/machineLoadLevel");
        assertAccept(request, MachineLoadRuleDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, MachineLoadRuleDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        MachineLoadRuleDto requestBody = readBody(request, MachineLoadRuleDto.class);
        assertEquals(requestBody.getCpuLoadPercentage(), new Integer(25));
        assertEquals(requestBody.getRamLoadPercentage(), new Integer(75));
        assertLinkExist(requestBody, requestBody.searchLink("datacenter").getHref(), "datacenter",
            DatacenterDto.SHORT_MEDIA_TYPE_JSON);
    }

    public void testCreateRackLoadLevelRule() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", MachineLoadRuleDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("loadlevel.json"));
        server.enqueue(response);
        server.play();

        RackDto rack = new RackDto();
        RESTLink link = new RESTLink("edit", "/admin/datacenters/1/racks/5");
        link.setType(RackDto.SHORT_MEDIA_TYPE_JSON);
        rack.addLink(link);

        newApiClient().getInfrastructureApi().createRackLoadLevelRule(rack, 25, 75);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "POST", "/admin/rules/machineLoadLevel");
        assertAccept(request, MachineLoadRuleDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, MachineLoadRuleDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        MachineLoadRuleDto requestBody = readBody(request, MachineLoadRuleDto.class);
        assertEquals(requestBody.getCpuLoadPercentage(), new Integer(25));
        assertEquals(requestBody.getRamLoadPercentage(), new Integer(75));
        assertLinkExist(requestBody, requestBody.searchLink("rack").getHref(), "rack",
            RackDto.SHORT_MEDIA_TYPE_JSON);
    }

    public void testCreateMachineLoadLevelRule() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type", MachineLoadRuleDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("loadlevel.json"));
        server.enqueue(response);
        server.play();

        MachineDto machine = new MachineDto();
        RESTLink link = new RESTLink("edit", "/admin/datacenters/1/racks/1/machines/12");
        link.setType(MachineDto.SHORT_MEDIA_TYPE_JSON);
        machine.addLink(link);

        newApiClient().getInfrastructureApi().createMachineLoadLevelRule(machine, 25, 75);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "POST", "/admin/rules/machineLoadLevel");
        assertAccept(request, MachineLoadRuleDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, MachineLoadRuleDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        MachineLoadRuleDto requestBody = readBody(request, MachineLoadRuleDto.class);
        assertEquals(requestBody.getCpuLoadPercentage(), new Integer(25));
        assertEquals(requestBody.getRamLoadPercentage(), new Integer(75));
        assertLinkExist(requestBody, requestBody.searchLink("machine").getHref(), "machine",
            MachineDto.SHORT_MEDIA_TYPE_JSON);
    }

    public void testCreatePublicCloudRegion() throws Exception
    {
        String region = new String("eu-west-1");
        String hypervisorType = new String("Amazon");

        MockResponse response =
            new MockResponse().setHeader("Content-Type", DatacenterDto.SHORT_MEDIA_TYPE_JSON)
                .setBody(payloadFromResource("publiccloudregion.json"));

        server.enqueue(response);
        server.play();

        RemoteServicesDto listRs = new RemoteServicesDto();
        RemoteServiceDto rs = new RemoteServiceDto();
        rs.setType(RemoteServiceType.VIRTUAL_SYSTEM_MONITOR);
        listRs.add(rs);

        newApiClient().getInfrastructureApi().createPublicCloudRegion("amazon test", region,
            hypervisorType, listRs.getCollection());

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "POST", PUBLIC_CLOUD_REGIONS_URL);
        assertAccept(request, PublicCloudRegionDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, PublicCloudRegionDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        PublicCloudRegionDto requestBody = readBody(request, PublicCloudRegionDto.class);
        assertEquals(requestBody.getName(), "amazon test");
        assertEquals(requestBody.searchLink("region").getHref(),
            String.format("%s/%s/regions/%s", HYPERVISORTYPES_URL, hypervisorType, region));

        assertEquals(requestBody.getRemoteServices().getCollection().get(0).getType(),
            RemoteServiceType.VIRTUAL_SYSTEM_MONITOR);
    }

    public void testListPublicCloudRegions() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type",
                PublicCloudRegionsDto.SHORT_MEDIA_TYPE_JSON).setBody(
                payloadFromResource("pcrs.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getInfrastructureApi().listPublicCloudRegions();

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", PUBLIC_CLOUD_REGIONS_URL);
        assertAccept(request, PublicCloudRegionsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListPublicCloudRegionsWithOptions() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type",
                PublicCloudRegionsDto.SHORT_MEDIA_TYPE_JSON).setBody(
                payloadFromResource("pcrs.json"));

        server.enqueue(response);
        server.play();

        newApiClient().getInfrastructureApi().listPublicCloudRegions(
            PublicCloudRegionListOptions.builder().limit(1).scope(0).build());

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", PUBLIC_CLOUD_REGIONS_URL + "?idScope=0&limit=1");
        assertAccept(request, PublicCloudRegionsDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

}
