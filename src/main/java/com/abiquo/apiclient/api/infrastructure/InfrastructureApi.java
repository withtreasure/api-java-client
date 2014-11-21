package com.abiquo.apiclient.api.infrastructure;

import static com.abiquo.apiclient.api.ApiPath.DATECENTERS_URL;
import static com.abiquo.apiclient.api.ApiPath.LOADLEVELRULES_URL;

import java.util.List;

import com.abiquo.apiclient.rest.RestClient;
import com.abiquo.model.enumerator.NetworkType;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.server.core.cloud.VirtualDatacenterDto;
import com.abiquo.server.core.enterprise.DatacenterLimitsDto;
import com.abiquo.server.core.enterprise.DatacentersLimitsDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.infrastructure.DatacentersDto;
import com.abiquo.server.core.infrastructure.MachineDto;
import com.abiquo.server.core.infrastructure.MachinesDto;
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
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class InfrastructureApi
{

    private final RestClient client;

    public InfrastructureApi(final RestClient client)
    {
        this.client = client;

    }

    public DatacenterDto findDatacenter(final String name)
    {
        DatacentersDto datacenters =
            client.get(DATECENTERS_URL, DatacentersDto.MEDIA_TYPE, DatacentersDto.class);
        return datacenters.getCollection().stream()
            .filter(datacenter -> datacenter.getName().equals(name)).findFirst().get();
    }

    public RackDto findRack(final DatacenterDto datacenter, final String name)
    {
        RacksDto racks =
            client.get(datacenter.searchLink("racks").getHref(), RacksDto.MEDIA_TYPE,
                RacksDto.class);
        return racks.getCollection().stream().filter(rack -> rack.getName().equals(name))
            .findFirst().get();
    }

    public DatacentersLimitsDto listLimits(final EnterpriseDto enterprise)
    {
        return client.get(enterprise.searchLink("limits"), DatacentersLimitsDto.class);
    }

    public DatacenterLimitsDto findLimits(final EnterpriseDto enterprise, final String locationName)
    {
        return listLimits(enterprise).getCollection().stream()
            .filter(l -> locationName.equals(l.searchLink("location").getTitle())).findFirst()
            .get();
    }

    public DatacenterLimitsDto getEnterpriseLimitsForDatacenter(final EnterpriseDto enterprise,
        final DatacenterDto datacenter)
    {
        DatacentersLimitsDto limits =
            client.get(enterprise.searchLink("limits").getHref(), DatacentersLimitsDto.MEDIA_TYPE,
                DatacentersLimitsDto.class);

        return limits.getCollection().stream()
            .filter(l -> l.searchLink("location").getTitle().equals(datacenter.getName()))
            .findFirst().get();
    }

    public VLANNetworksDto listExternalNetworks(final DatacenterLimitsDto limits)
    {
        return client.get(limits.searchLink("externalnetworks").getHref(),
            VLANNetworksDto.MEDIA_TYPE, VLANNetworksDto.class);
    }

    public VLANNetworkDto findExternalNetwork(final DatacenterLimitsDto limits, final String name)
    {
        return listExternalNetworks(limits).getCollection().stream()
            .filter(net -> net.getName().equals(name)).findFirst().get();
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

        return client.post(DATECENTERS_URL, DatacenterDto.MEDIA_TYPE, DatacenterDto.MEDIA_TYPE,
            datacenter, DatacenterDto.class);
    }

    public RemoteServicesDto listRemoteServices(final DatacenterDto datacenter)
    {
        return client.get(datacenter.searchLink("remoteservices"), RemoteServicesDto.class);
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
        limits.addLink(new RESTLink("location", datacenter.getEditLink().getHref()));
        client.post(enterprise.searchLink("limits").getHref(), DatacenterLimitsDto.MEDIA_TYPE,
            DatacenterLimitsDto.MEDIA_TYPE, limits, DatacenterLimitsDto.class);
    }

    public MachinesDto discoverMachines(final DatacenterDto datacenter, final String type,
        final String ip, final String user, final String password)
    {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("hypervisor", type);
        queryParams.add("ip", ip);
        queryParams.add("user", user);
        queryParams.add("password", password);

        return client.get(datacenter.searchLink("discover").getHref(), queryParams,
            MachinesDto.MEDIA_TYPE, MachinesDto.class);
    }

    public MachineDto createMachine(final DatacenterDto datacenter, final RackDto rack,
        final MachineDto machine)
    {
        return client.post(rack.searchLink("machines").getHref(), MachineDto.MEDIA_TYPE,
            MachineDto.MEDIA_TYPE, machine, MachineDto.class);
    }

    public NetworkServiceTypeDto findDefaultNetworkServiceType(final DatacenterDto datacenter)
    {
        NetworkServiceTypesDto netServiceTypes =
            client.get(datacenter.searchLink("networkservicetypes").getHref(),
                NetworkServiceTypesDto.MEDIA_TYPE, NetworkServiceTypesDto.class);
        return netServiceTypes.getCollection().stream()
            .filter(netServiceType -> netServiceType.isDefaultNST()).findFirst().get();
    }

    public MachineLoadRuleDto datacenterLoadLevelRule(final DatacenterDto datacenter,
        final int cpuLoadPercentage, final int ramLoadPercentage)
    {
        return createLoadLevelRule(new RESTLink("datacenter", datacenter.getEditLink().getHref()),
            cpuLoadPercentage, ramLoadPercentage);
    }

    public MachineLoadRuleDto rackLoadLevelRule(final RackDto rack, final int cpuLoadPercentage,
        final int ramLoadPercentage)
    {
        return createLoadLevelRule(new RESTLink("rack", rack.getEditLink().getHref()),
            cpuLoadPercentage, ramLoadPercentage);
    }

    public MachineLoadRuleDto createMachineLoadLevelRule(final MachineDto machine,
        final int cpuLoadPercentage, final int ramLoadPercentage)
    {
        return createLoadLevelRule(new RESTLink("machine", machine.getEditLink().getHref()),
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

    public StorageDeviceDto findDevice(final DatacenterDto datacenter, final String name)
    {
        StorageDevicesDto devices =
            client.get(datacenter.searchLink("devices").getHref(), StorageDevicesDto.MEDIA_TYPE,
                StorageDevicesDto.class);
        return devices.getCollection().stream().filter(device -> device.getName().equals(name))
            .findFirst().get();
    }

    public StoragePoolDto findPool(final StorageDeviceDto device, final String name)
    {
        StoragePoolsDto pools =
            client.get(device.searchLink("pools").getHref(), StoragePoolsDto.MEDIA_TYPE,
                StoragePoolsDto.class);
        return pools.getCollection().stream().filter(pool -> pool.getName().equals(name))
            .findFirst().get();
    }

    public StoragePoolDto findRemotePool(final StorageDeviceDto device, final String name)
    {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("sync", true);

        StoragePoolsDto pools =
            client.get(device.searchLink("pools").getHref(), queryParams,
                StoragePoolsDto.MEDIA_TYPE, StoragePoolsDto.class);
        return pools.getCollection().stream().filter(pool -> pool.getName().equals(name))
            .findFirst().get();
    }

    public StorageDeviceDto createDevice(final DatacenterDto datacenter, final String name,
        final String technology, final String managementIp, final int managementPort,
        final String serviceIp, final int servicePort, final String username, final String password)
    {
        StorageDeviceDto device = new StorageDeviceDto();
        device.addLink(new RESTLink("datacenter", datacenter.getEditLink().getHref()));
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
        StoragePoolDto storagePool = findRemotePool(storageDevice, pool);
        TierDto tier = findTier(datacenter, tierName);

        storagePool.setEnabled(true);
        storagePool.addLink(new RESTLink("tier", tier.getEditLink().getHref()));

        return client.post(storageDevice.searchLink("pools").getHref(), StoragePoolDto.MEDIA_TYPE,
            StoragePoolDto.MEDIA_TYPE, storagePool, StoragePoolDto.class);
    }

    public VLANNetworkDto createExternalNetwork(final DatacenterDto datacenter,
        final EnterpriseDto enterprise, final String name, final String address,
        final String gateway, final int mask, final int tag)
    {
        NetworkServiceTypeDto defaultNetType = findDefaultNetworkServiceType(datacenter);

        VLANNetworkDto vlan = new VLANNetworkDto();
        vlan.addLink(new RESTLink("enterprise", enterprise.getEditLink().getHref()));
        vlan.addLink(new RESTLink("networkservicetype", defaultNetType.getEditLink().getHref()));
        vlan.setAddress(address);
        vlan.setName(name);
        vlan.setType(NetworkType.EXTERNAL);
        vlan.setMask(mask);
        vlan.setTag(tag);
        vlan.setGateway(gateway);

        return client.post(datacenter.searchLink("network").getHref(), VLANNetworkDto.MEDIA_TYPE,
            VLANNetworkDto.MEDIA_TYPE, vlan, VLANNetworkDto.class);
    }

    public TierDto findTier(final VirtualDatacenterDto vdc, final String name)
    {
        TiersDto tiers =
            client.get(vdc.searchLink("tiers").getHref(), TiersDto.MEDIA_TYPE, TiersDto.class);
        return tiers.getCollection().stream().filter(tier -> tier.getName().equals(name))
            .findFirst().get();
    }

    public TierDto findTier(final DatacenterDto datacenter, final String name)
    {
        TiersDto tiers =
            client.get(datacenter.searchLink("tiers").getHref(), TiersDto.MEDIA_TYPE,
                TiersDto.class);
        return tiers.getCollection().stream().filter(tier -> tier.getName().equals(name))
            .findFirst().get();
    }

}
