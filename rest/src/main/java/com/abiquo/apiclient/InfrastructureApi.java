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
import static com.abiquo.apiclient.domain.ApiPath.LOADLEVELRULES_URL;
import static com.abiquo.apiclient.domain.ApiPath.PUBLIC_CLOUD_REGIONS_URL;
import static com.abiquo.apiclient.domain.Links.create;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.find;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.abiquo.apiclient.domain.options.DatacenterListOptions;
import com.abiquo.apiclient.domain.options.PublicCloudRegionListOptions;
import com.abiquo.model.enumerator.NetworkType;
import com.abiquo.model.rest.RESTLink;
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
import com.google.common.base.Predicate;

public class InfrastructureApi
{
    private final RestClient client;

    // Package private constructor to be used only by the ApiClient
    InfrastructureApi(final RestClient client)
    {
        this.client = checkNotNull(client, "client cannot be null");
    }

    public Iterable<DatacenterDto> listDatacenters()
    {
        return client.list(DATACENTERS_URL, DatacentersDto.MEDIA_TYPE, DatacentersDto.class);
    }

    public Iterable<DatacenterDto> listDatacenters(final DatacenterListOptions options)
    {
        return client.list(DATACENTERS_URL, options.queryParams(), DatacentersDto.MEDIA_TYPE,
            DatacentersDto.class);
    }

    public Iterable<RackDto> listRacks(final DatacenterDto datacenter)
    {
        return client.list(datacenter.searchLink("racks").getHref(), RacksDto.MEDIA_TYPE,
            RacksDto.class);
    }

    public Iterable<DatacenterLimitsDto> listLimits(final EnterpriseDto enterprise)
    {
        return client.list(enterprise.searchLink("limits"), DatacentersLimitsDto.class);
    }

    public DatacenterLimitsDto getEnterpriseLimitsForDatacenter(final EnterpriseDto enterprise,
        final DatacenterDto datacenter)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("datacenter", datacenter.getId());
        return client
            .get(enterprise.searchLink("limits").getHref(), params,
                DatacentersLimitsDto.MEDIA_TYPE, DatacentersLimitsDto.class).getCollection().get(0);
    }

    public Iterable<VLANNetworkDto> listExternalNetworks(final DatacenterLimitsDto limits)
    {
        return client.list(limits.searchLink("externalnetworks").getHref(),
            VLANNetworksDto.MEDIA_TYPE, VLANNetworksDto.class);
    }

    public DatacenterDto createDatacenter(final String name, final String location,
        final List<RemoteServiceDto> remoteServices)
    {
        RemoteServicesDto remoteServicesDto = new RemoteServicesDto();
        remoteServicesDto.addAll(remoteServices);

        DatacenterDto datacenter = new DatacenterDto();
        datacenter.setName(name);
        datacenter.setLocation(location);
        datacenter.setRemoteServices(remoteServicesDto);

        return client.post(DATACENTERS_URL, DatacenterDto.MEDIA_TYPE, DatacenterDto.MEDIA_TYPE,
            datacenter, DatacenterDto.class);
    }

    public PublicCloudRegionDto createPublicCloudRegion(final String name, final String region,
        final String type, final List<RemoteServiceDto> remoteServices)
    {
        RemoteServicesDto remoteServicesDto = new RemoteServicesDto();
        remoteServicesDto.addAll(remoteServices);

        PublicCloudRegionDto publicCloudRegion = new PublicCloudRegionDto();
        publicCloudRegion.setName(name);
        publicCloudRegion.setRemoteServices(remoteServicesDto);
        publicCloudRegion.addLink(new RESTLink("region", String.format("%s/%s/regions/%s",
            HYPERVISORTYPES_URL, type, region)));

        return client.post(PUBLIC_CLOUD_REGIONS_URL, PublicCloudRegionDto.MEDIA_TYPE,
            PublicCloudRegionDto.MEDIA_TYPE, publicCloudRegion, PublicCloudRegionDto.class);
    }

    public Iterable<RemoteServiceDto> listRemoteServices(final DatacenterDto datacenter)
    {
        return client.list(datacenter.searchLink("remoteservices"), RemoteServicesDto.class);
    }

    public RackDto createRack(final DatacenterDto datacenter, final String name)
    {
        RackDto rack = new RackDto();
        rack.setName(name);
        return client.post(datacenter.searchLink("racks").getHref(), RackDto.MEDIA_TYPE,
            RackDto.MEDIA_TYPE, rack, RackDto.class);
    }

    public void addDatacenterToEnterprise(final EnterpriseDto enterprise,
        final DatacenterDto datacenter)
    {
        DatacenterLimitsDto limits = new DatacenterLimitsDto();
        limits.addLink(create("location", datacenter.getEditLink().getHref(), datacenter
            .getEditLink().getType()));
        client.post(enterprise.searchLink("limits").getHref(), DatacenterLimitsDto.MEDIA_TYPE,
            DatacenterLimitsDto.MEDIA_TYPE, limits, DatacenterLimitsDto.class);
    }

    public MachinesDto discoverMachines(final DatacenterDto datacenter, final String type,
        final String ip, final String user, final String password)
    {
        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("hypervisor", type);
        queryParams.put("ip", ip);
        queryParams.put("user", user);
        queryParams.put("password", password);

        return client.get(datacenter.searchLink("discover").getHref(), queryParams,
            MachinesDto.MEDIA_TYPE, MachinesDto.class);
    }

    public MachineDto createMachine(final RackDto rack, final MachineDto machine)
    {
        return client.post(rack.searchLink("machines").getHref(), MachineDto.MEDIA_TYPE,
            MachineDto.MEDIA_TYPE, machine, MachineDto.class);
    }

    public Iterable<NetworkServiceTypeDto> listNetworkServiceTypes(final DatacenterDto datacenter)
    {
        return client.list(datacenter.searchLink("networkservicetypes").getHref(),
            NetworkServiceTypesDto.MEDIA_TYPE, NetworkServiceTypesDto.class);
    }

    public MachineLoadRuleDto createDatacenterLoadLevelRule(final DatacenterDto datacenter,
        final int cpuLoadPercentage, final int ramLoadPercentage)
    {
        return createLoadLevelRule(
            create("datacenter", datacenter.getEditLink().getHref(), datacenter.getEditLink()
                .getType()), cpuLoadPercentage, ramLoadPercentage);
    }

    public MachineLoadRuleDto createRackLoadLevelRule(final RackDto rack,
        final int cpuLoadPercentage, final int ramLoadPercentage)
    {
        return createLoadLevelRule(
            create("rack", rack.getEditLink().getHref(), rack.getEditLink().getType()),
            cpuLoadPercentage, ramLoadPercentage);
    }

    public MachineLoadRuleDto createMachineLoadLevelRule(final MachineDto machine,
        final int cpuLoadPercentage, final int ramLoadPercentage)
    {
        return createLoadLevelRule(
            create("machine", machine.getEditLink().getHref(), machine.getEditLink().getType()),
            cpuLoadPercentage, ramLoadPercentage);
    }

    private MachineLoadRuleDto createLoadLevelRule(final RESTLink targetLink,
        final int cpuLoadPercentage, final int ramLoadPercentage)
    {
        MachineLoadRuleDto rule = new MachineLoadRuleDto();
        rule.setCpuLoadPercentage(cpuLoadPercentage);
        rule.setRamLoadPercentage(ramLoadPercentage);
        rule.addLink(targetLink);

        return client.post(LOADLEVELRULES_URL, MachineLoadRuleDto.MEDIA_TYPE,
            MachineLoadRuleDto.MEDIA_TYPE, rule, MachineLoadRuleDto.class);
    }

    public Iterable<StorageDeviceDto> listDevices(final DatacenterDto datacenter)
    {
        return client.list(datacenter.searchLink("devices").getHref(),
            StorageDevicesDto.MEDIA_TYPE, StorageDevicesDto.class);
    }

    public Iterable<StoragePoolDto> listPools(final StorageDeviceDto device)
    {
        return client.list(device.searchLink("pools").getHref(), StoragePoolsDto.MEDIA_TYPE,
            StoragePoolsDto.class);
    }

    public Iterable<StoragePoolDto> listRemotePools(final StorageDeviceDto device)
    {
        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("sync", true);

        return client.list(device.searchLink("pools").getHref(), queryParams,
            StoragePoolsDto.MEDIA_TYPE, StoragePoolsDto.class);
    }

    public StorageDeviceDto createDevice(final DatacenterDto datacenter, final String name,
        final String technology, final String managementIp, final int managementPort,
        final String serviceIp, final int servicePort, final String username, final String password)
    {
        StorageDeviceDto device = new StorageDeviceDto();
        device.addLink(create("datacenter", datacenter.getEditLink().getHref(), datacenter
            .getEditLink().getType()));
        device.setName(name);
        device.setStorageTechnology(technology);
        device.setManagementIp(managementIp);
        device.setManagementPort(managementPort);
        device.setServiceIp(serviceIp);
        device.setServicePort(servicePort);
        device.setUsername(username);
        device.setPassword(password);

        return client.post(datacenter.searchLink("devices").getHref(), StorageDeviceDto.MEDIA_TYPE,
            StorageDeviceDto.MEDIA_TYPE, device, StorageDeviceDto.class);
    }

    public StoragePoolDto createPool(final DatacenterDto datacenter,
        final StorageDeviceDto storageDevice, final String pool, final String tierName)
    {
        StoragePoolDto storagePool =
            find(listRemotePools(storageDevice), new Predicate<StoragePoolDto>()
            {
                @Override
                public boolean apply(final StoragePoolDto input)
                {
                    return input.getName().equals(pool);
                }
            });

        TierDto tier = find(listTiers(datacenter), new Predicate<TierDto>()
        {
            @Override
            public boolean apply(final TierDto input)
            {
                return input.getName().equals(tierName);
            }
        });

        storagePool.setEnabled(true);
        storagePool.addLink(create("tier", tier.getEditLink().getHref(), tier.getEditLink()
            .getType()));

        return client.post(storageDevice.searchLink("pools").getHref(), StoragePoolDto.MEDIA_TYPE,
            StoragePoolDto.MEDIA_TYPE, storagePool, StoragePoolDto.class);
    }

    public VLANNetworkDto createExternalNetwork(final DatacenterDto datacenter,
        final NetworkServiceTypeDto nst, final EnterpriseDto enterprise, final String name,
        final String address, final String gateway, final int mask, final int tag)
    {
        VLANNetworkDto vlan = new VLANNetworkDto();
        vlan.addLink(create("enterprise", enterprise.getEditLink().getHref(), enterprise
            .getEditLink().getType()));
        vlan.addLink(create("networkservicetype", nst.getEditLink().getHref(), nst.getEditLink()
            .getType()));
        vlan.setAddress(address);
        vlan.setName(name);
        vlan.setType(NetworkType.EXTERNAL);
        vlan.setMask(mask);
        vlan.setTag(tag);
        vlan.setGateway(gateway);

        return client.post(datacenter.searchLink("network").getHref(), VLANNetworkDto.MEDIA_TYPE,
            VLANNetworkDto.MEDIA_TYPE, vlan, VLANNetworkDto.class);
    }

    public Iterable<TierDto> listTiers(final DatacenterDto datacenter)
    {
        return client.list(datacenter.searchLink("tiers").getHref(), TiersDto.MEDIA_TYPE,
            TiersDto.class);
    }

    public Iterable<PublicCloudRegionDto> listPublicCloudRegions(
        final PublicCloudRegionListOptions options)
    {
        return client.list(PUBLIC_CLOUD_REGIONS_URL, options.queryParams(),
            PublicCloudRegionsDto.MEDIA_TYPE, PublicCloudRegionsDto.class);
    }

    public Iterable<PublicCloudRegionDto> listPublicCloudRegions()
    {
        return client.list(PUBLIC_CLOUD_REGIONS_URL, PublicCloudRegionsDto.MEDIA_TYPE,
            PublicCloudRegionsDto.class);
    }

}
