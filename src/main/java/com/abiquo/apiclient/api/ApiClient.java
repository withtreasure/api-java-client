package com.abiquo.apiclient.api;

import static com.abiquo.server.core.cloud.VirtualMachineState.LOCKED;
import static com.abiquo.server.core.task.TaskState.FINISHED_SUCCESSFULLY;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.abiquo.apiclient.rest.RestClient;
import com.abiquo.model.enumerator.NetworkType;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateRequestDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplatesDto;
import com.abiquo.server.core.cloud.VirtualApplianceDto;
import com.abiquo.server.core.cloud.VirtualAppliancesDto;
import com.abiquo.server.core.cloud.VirtualDatacenterDto;
import com.abiquo.server.core.cloud.VirtualDatacentersDto;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.cloud.VirtualMachineInstanceDto;
import com.abiquo.server.core.cloud.VirtualMachineState;
import com.abiquo.server.core.cloud.VirtualMachineStateDto;
import com.abiquo.server.core.cloud.VirtualMachineTaskDto;
import com.abiquo.server.core.cloud.VirtualMachinesDto;
import com.abiquo.server.core.config.LicenseDto;
import com.abiquo.server.core.enterprise.DatacenterLimitsDto;
import com.abiquo.server.core.enterprise.DatacentersLimitsDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.enterprise.EnterprisesDto;
import com.abiquo.server.core.enterprise.UserDto;
import com.abiquo.server.core.enterprise.UsersDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.infrastructure.DatacentersDto;
import com.abiquo.server.core.infrastructure.MachineDto;
import com.abiquo.server.core.infrastructure.MachinesDto;
import com.abiquo.server.core.infrastructure.PublicCloudRegionDto;
import com.abiquo.server.core.infrastructure.RackDto;
import com.abiquo.server.core.infrastructure.RacksDto;
import com.abiquo.server.core.infrastructure.RemoteServiceDto;
import com.abiquo.server.core.infrastructure.RemoteServicesDto;
import com.abiquo.server.core.infrastructure.network.ExternalIpsDto;
import com.abiquo.server.core.infrastructure.network.NetworkServiceTypeDto;
import com.abiquo.server.core.infrastructure.network.NetworkServiceTypesDto;
import com.abiquo.server.core.infrastructure.network.VLANNetworkDto;
import com.abiquo.server.core.infrastructure.network.VLANNetworksDto;
import com.abiquo.server.core.infrastructure.network.VMNetworkConfigurationsDto;
import com.abiquo.server.core.infrastructure.storage.StorageDeviceDto;
import com.abiquo.server.core.infrastructure.storage.StorageDevicesDto;
import com.abiquo.server.core.infrastructure.storage.StoragePoolDto;
import com.abiquo.server.core.infrastructure.storage.StoragePoolsDto;
import com.abiquo.server.core.infrastructure.storage.TierDto;
import com.abiquo.server.core.infrastructure.storage.TiersDto;
import com.abiquo.server.core.infrastructure.storage.VolumeManagementDto;
import com.abiquo.server.core.scheduler.MachineLoadRuleDto;
import com.abiquo.server.core.task.TaskDto;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class ApiClient
{
    private final RestClient client;

    private final String baseURL;

    public ApiClient(final String baseURL, final String username, final String password)
    {
        client = new RestClient(username, password);
        this.baseURL = baseURL;
    }

    public String absolute(final String path)
    {
        return baseURL + (path.startsWith("/") ? path : "/" + path);
    }

    public <T extends SingleResourceTransportDto> T get(final RESTLink link, final Class<T> clazz)
    {
        return client.get(link.getHref(), link.getType(), clazz);
    }

    public <T extends SingleResourceTransportDto> T refresh(final T dto)
    {
        RESTLink link = dto.getEditLink();
        if (link == null)
        {
            link = dto.searchLink("self");
        }

        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) dto.getClass();

        checkNotNull(link, "The given object does not have an edit/self link");
        return client.get(link.getHref(), link.getType(), clazz);
    }

    public LicenseDto addLicense(final String licenseKey)
    {
        LicenseDto license = new LicenseDto();
        license.setCode(licenseKey);
        return client.post(absolute("/config/licenses"), LicenseDto.MEDIA_TYPE,
            LicenseDto.MEDIA_TYPE, license, LicenseDto.class);
    }

    public EnterpriseDto findEnterprise(final String name)
    {
        EnterprisesDto enterprises =
            client.get(absolute("/admin/enterprises"), EnterprisesDto.MEDIA_TYPE,
                EnterprisesDto.class);
        return enterprises.getCollection().stream()
            .filter(enterprise -> enterprise.getName().equals(name)).findFirst().get();
    }

    public DatacenterDto findDatacenter(final String name)
    {
        DatacentersDto datacenters =
            client.get(absolute("/admin/datacenters"), DatacentersDto.MEDIA_TYPE,
                DatacentersDto.class);
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

    public DatacenterDto findDatacenterLocation(final String name)
    {
        DatacentersDto locations =
            client.get(absolute("/cloud/locations"), DatacentersDto.MEDIA_TYPE,
                DatacentersDto.class);
        return locations.getCollection().stream()
            .filter(location -> location.getName().equals(name)).findFirst().get();
    }

    public VirtualDatacenterDto findVirtualDatacenter(final String name)
    {
        VirtualDatacentersDto vdcs =
            client.get(absolute("/cloud/virtualdatacenters"), VirtualDatacentersDto.MEDIA_TYPE,
                VirtualDatacentersDto.class);
        return vdcs.getCollection().stream().filter(vdc -> vdc.getName().equals(name)).findFirst()
            .get();
    }

    public VirtualMachineTemplateDto findAvailableTemplate(final VirtualDatacenterDto vdc,
        final String name)
    {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("limit", 0);

        VirtualMachineTemplatesDto templates =
            client.get(vdc.searchLink("templates").getHref(), queryParams,
                VirtualMachineTemplatesDto.MEDIA_TYPE, VirtualMachineTemplatesDto.class);
        return templates.getCollection().stream()
            .filter(template -> template.getName().equals(name)).findFirst().get();
    }

    public DatacentersLimitsDto listLimits(final EnterpriseDto enterprise)
    {
        return get(enterprise.searchLink("limits"), DatacentersLimitsDto.class);
    }

    public DatacenterLimitsDto findLimits(final EnterpriseDto enterprise, final String locationName)
    {
        return listLimits(enterprise).getCollection().stream()
            .filter(l -> locationName.equals(l.searchLink("location").getTitle())).findFirst()
            .get();
    }

    public ExternalIpsDto listExternalIps(final VirtualDatacenterDto vdc)
    {
        return get(vdc.searchLink("externalips"), ExternalIpsDto.class);
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

    public VirtualAppliancesDto listVirtualAppliances(final VirtualDatacenterDto vdc)
    {
        return client.get(vdc.searchLink("virtualappliances").getHref(),
            VirtualAppliancesDto.MEDIA_TYPE, VirtualAppliancesDto.class);
    }

    public VirtualApplianceDto findVirtualAppliance(final VirtualDatacenterDto vdc,
        final String name)
    {
        VirtualAppliancesDto vapps = listVirtualAppliances(vdc);
        return vapps.getCollection().stream().filter(vapp -> vapp.getName().equals(name))
            .findFirst().get();
    }

    public VirtualMachinesDto listVirtualMachines(final VirtualApplianceDto vapp)
    {
        return client.get(vapp.searchLink("virtualmachines").getHref(),
            VirtualMachinesDto.MEDIA_TYPE, VirtualMachinesDto.class);
    }

    public VMNetworkConfigurationsDto listNetworkConfigurations(final VirtualMachineDto vm)
    {
        return client.get(vm.searchLink("configurations").getHref(),
            VMNetworkConfigurationsDto.MEDIA_TYPE, VMNetworkConfigurationsDto.class);
    }

    public VirtualMachineDto findVirtualMachine(final VirtualApplianceDto vapp,
        final String templateName)
    {
        VirtualMachinesDto vms = listVirtualMachines(vapp);
        return vms.getCollection().stream()
            .filter(vm -> vm.searchLink("virtualmachinetemplate").getTitle().equals(templateName))
            .findFirst().get();
    }

    public VirtualDatacenterDto createVirtualDatacenter(final SingleResourceTransportDto location,
        final EnterpriseDto enterprise, final String name, final String type)
    {
        checkArgument(location instanceof DatacenterDto || location instanceof PublicCloudRegionDto);

        VirtualDatacenterDto vdc = new VirtualDatacenterDto();
        vdc.setName(name);
        vdc.setHypervisorType(type);

        vdc.addLink(new RESTLink("enterprise", enterprise.getEditLink().getHref()));
        vdc.addLink(new RESTLink("location", location.searchLink("self").getHref()));

        VLANNetworkDto vlan = new VLANNetworkDto();
        vlan.setAddress("192.168.0.0");
        vlan.setGateway("192.168.0.5");
        vlan.setMask(24);
        vlan.setName("default_private_network");
        vlan.setType(NetworkType.INTERNAL);
        vdc.setVlan(vlan);

        return client.post(absolute("/cloud/virtualdatacenters"), VirtualDatacenterDto.MEDIA_TYPE,
            VirtualDatacenterDto.MEDIA_TYPE, vdc, VirtualDatacenterDto.class);
    }

    public VirtualApplianceDto createVirtualAppliance(final VirtualDatacenterDto vdc,
        final String name)
    {
        VirtualApplianceDto vapp = new VirtualApplianceDto();
        vapp.setName(name);

        return client.post(vdc.searchLink("virtualappliances").getHref(),
            VirtualApplianceDto.MEDIA_TYPE, VirtualApplianceDto.MEDIA_TYPE, vapp,
            VirtualApplianceDto.class);
    }

    public VirtualMachineDto createVirtualMachine(final VirtualDatacenterDto vdc,
        final VirtualMachineTemplateDto template, final VirtualApplianceDto vapp)
    {
        VirtualMachineDto vm = new VirtualMachineDto();
        vm.setVdrpEnabled(Boolean.TRUE);
        vm.addLink(new RESTLink("virtualmachinetemplate", template.getEditLink().getHref()));

        return client
            .post(vapp.searchLink("virtualmachines").getHref(), VirtualMachineDto.MEDIA_TYPE,
                VirtualMachineDto.MEDIA_TYPE, vm, VirtualMachineDto.class);
    }

    public VirtualMachineTemplateDto instanceVirtualMachine(final VirtualMachineDto vm,
        final String snapshotName)
    {
        VirtualMachineInstanceDto instance = new VirtualMachineInstanceDto();
        instance.setInstanceName(snapshotName);
        AcceptedRequestDto<String> acceptedRequest =
            client.post(vm.searchLink("instance").getHref(), AcceptedRequestDto.MEDIA_TYPE,
                VirtualMachineInstanceDto.MEDIA_TYPE, instance,
                new GenericType<AcceptedRequestDto<String>>()
                {
                });

        // Wait a maximum of 5 minutes and poll every 5 seconds
        TaskDto task = waitForTask(acceptedRequest, 5, 300, TimeUnit.SECONDS);
        if (FINISHED_SUCCESSFULLY != task.getState())
        {
            throw new RuntimeException("Virtual machine instance operation failed");
        }

        return client.get(task.searchLink("result").getHref(),
            VirtualMachineTemplateDto.MEDIA_TYPE, VirtualMachineTemplateDto.class);
    }

    public VirtualMachineTemplateDto promoteInstance(final VirtualMachineTemplateDto template,
        final String promotedName)
    {
        VirtualMachineTemplateRequestDto promote = new VirtualMachineTemplateRequestDto();
        promote.addLink(new RESTLink("virtualmachinetemplate", template.getEditLink().getHref()));
        promote.setPromotedName(promotedName);
        AcceptedRequestDto<String> acceptedRequest =
            client.post(template.searchLink("datacenterrepository").getHref()
                + "/virtualmachinetemplates", AcceptedRequestDto.MEDIA_TYPE,
                VirtualMachineTemplateRequestDto.MEDIA_TYPE, promote,
                new GenericType<AcceptedRequestDto<String>>()
                {
                });

        // Wait a maximum of 5 minutes and poll every 5 seconds
        TaskDto task = waitForTask(acceptedRequest, 5, 300, TimeUnit.SECONDS);
        if (FINISHED_SUCCESSFULLY != task.getState())
        {
            throw new RuntimeException("Promote instance operation failed");
        }

        return client.get(task.searchLink("result").getHref(),
            VirtualMachineTemplateDto.MEDIA_TYPE, VirtualMachineTemplateDto.class);
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

        return client.post(absolute("/admin/datacenters"), DatacenterDto.MEDIA_TYPE,
            DatacenterDto.MEDIA_TYPE, datacenter, DatacenterDto.class);
    }

    public RemoteServicesDto listRemoteServices(final DatacenterDto datacenter)
    {
        return get(datacenter.searchLink("remoteservices"), RemoteServicesDto.class);
    }

    public void delete(final SingleResourceTransportDto dto)
    {
        client.delete(dto.getEditLink().getHref());
    }

    public RackDto createRack(final DatacenterDto datacenter, final String name)
    {
        RackDto rack = new RackDto();
        rack.setName(name);
        return client.post(datacenter.searchLink("racks").getHref(), RackDto.MEDIA_TYPE,
            RackDto.MEDIA_TYPE, rack, RackDto.class);
    }

    public EnterpriseDto createEnterprise(final String name)
    {
        EnterpriseDto enterprise = new EnterpriseDto();
        enterprise.setName(name);
        return client.post(absolute("/admin/enterprises"), EnterpriseDto.MEDIA_TYPE,
            EnterpriseDto.MEDIA_TYPE, enterprise, EnterpriseDto.class);
    }

    public void addDatacenterToEnterprise(final EnterpriseDto enterprise,
        final DatacenterDto datacenter)
    {
        DatacenterLimitsDto limits = new DatacenterLimitsDto();
        limits.addLink(new RESTLink("location", datacenter.getEditLink().getHref()));
        client.post(enterprise.searchLink("limits").getHref(), DatacenterLimitsDto.MEDIA_TYPE,
            DatacenterLimitsDto.MEDIA_TYPE, limits, DatacenterLimitsDto.class);
    }

    public UserDto findUser(final String name)
    {
        UsersDto users =
            client.get(absolute("/admin/enterprises/_/users"), UsersDto.MEDIA_TYPE, UsersDto.class);
        return users.getCollection().stream().filter(user -> user.getNick().equals(name))
            .findFirst().get();
    }

    public UserDto impersonateEnterprise(final String username, final EnterpriseDto enterprise)
    {
        UserDto user = findUser(username);
        user.modifyLink("enterprise", enterprise.getEditLink().getHref());
        return edit(user);
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

    public void refreshAppslibrary(final EnterpriseDto enterprise, final DatacenterDto datacenter)
    {
        AcceptedRequestDto<String> acceptedRequest =
            client.put(
                enterprise.searchLink("datacenterrepositories").getHref() + "/"
                    + datacenter.getId() + "/actions/refresh", AcceptedRequestDto.MEDIA_TYPE,
                new GenericType<AcceptedRequestDto<String>>()
                {
                });

        // Wait a maximum of 5 minutes and poll every 5 seconds
        TaskDto task = waitForTask(acceptedRequest, 5, 300, TimeUnit.SECONDS);
        if (FINISHED_SUCCESSFULLY != task.getState())
        {
            throw new RuntimeException("Refresh repository operation failed");
        }
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

        return client.post(absolute("/admin/rules/machineLoadLevel"),
            MachineLoadRuleDto.MEDIA_TYPE, MachineLoadRuleDto.MEDIA_TYPE, rule,
            MachineLoadRuleDto.class);
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

    public VirtualMachineDto deploy(final VirtualMachineDto vm)
    {
        client.post(vm.searchLink("deploy").getHref(), AcceptedRequestDto.MEDIA_TYPE,
            new GenericType<AcceptedRequestDto<String>>()
            {
            });

        // Wait a maximum of 5 minutes and poll every 5 seconds
        VirtualMachineDto refreshed = waitUntilUnlocked(vm, 5, 300, TimeUnit.SECONDS);
        if (!refreshed.getState().isDeployed())
        {
            throw new RuntimeException("Deploy virtual machine operation failed");
        }

        return refreshed;
    }

    public VirtualMachineDto undeploy(final VirtualMachineDto vm)
    {
        VirtualMachineTaskDto virtualMachineTask = new VirtualMachineTaskDto();
        virtualMachineTask.setForceUndeploy(false);

        client.post(vm.searchLink("undeploy").getHref(), AcceptedRequestDto.MEDIA_TYPE,
            VirtualMachineTaskDto.MEDIA_TYPE, virtualMachineTask,
            new GenericType<AcceptedRequestDto<String>>()
            {
            });

        // Wait a maximum of 5 minutes and poll every 5 seconds
        VirtualMachineDto refreshed = waitUntilUnlocked(vm, 5, 300, TimeUnit.SECONDS);
        if (refreshed.getState().isDeployed())
        {
            throw new RuntimeException("Undeploy virtual machine operation failed");
        }

        return refreshed;
    }

    public VirtualMachineDto powerState(final VirtualMachineDto vm, final VirtualMachineState state)
    {
        VirtualMachineStateDto vmState = new VirtualMachineStateDto();
        vmState.setState(state);

        client.put(vm.searchLink("state").getHref(), AcceptedRequestDto.MEDIA_TYPE,
            VirtualMachineStateDto.MEDIA_TYPE, vmState,
            new GenericType<AcceptedRequestDto<String>>()
            {
            });

        // Wait a maximum of 5 minutes and poll every 5 seconds
        VirtualMachineDto refreshed = waitUntilUnlocked(vm, 5, 300, TimeUnit.SECONDS);
        if (state != refreshed.getState())
        {
            throw new RuntimeException("Virtual machine power state '" + state.name()
                + "' operation failed");
        }

        return refreshed;
    }

    public <T extends SingleResourceTransportDto> T edit(final T dto)
    {
        RESTLink link =
            checkNotNull(dto.getEditLink(), "The given object does not have an edit link");

        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) dto.getClass();

        return client.put(link.getHref(), link.getType(), link.getType(), dto, clazz);
    }

    public VirtualMachineDto editVirtualMachine(final VirtualMachineDto vm)
    {
        VirtualMachineDto refreshed = null;

        if (vm.getState().isDeployed())
        {
            client.put(vm.getEditLink().getHref(), AcceptedRequestDto.MEDIA_TYPE,
                VirtualMachineDto.MEDIA_TYPE, vm, new GenericType<AcceptedRequestDto<String>>()
                {
                });

            // Wait a maximum of 5 minutes and poll every 5 seconds
            refreshed = waitUntilUnlocked(vm, 5, 300, TimeUnit.SECONDS);
            if (VirtualMachineState.OFF != refreshed.getState())
            {
                throw new RuntimeException("Virtual machine reconfigure operation failed");
            }
        }
        else
        {
            client.put(vm.getEditLink().getHref(), AcceptedRequestDto.MEDIA_TYPE,
                VirtualMachineDto.MEDIA_TYPE, vm);
            refreshed = refresh(vm);
        }

        return refreshed;
    }

    public VolumeManagementDto createVolume(final VirtualDatacenterDto vdc, final String name,
        final long sizeInMb, final TierDto tier)
    {
        VolumeManagementDto dto = new VolumeManagementDto();
        dto.setName(name);
        dto.setSizeInMB(sizeInMb);
        dto.addLink(new RESTLink("tier", tier.searchLink("self").getHref()));

        return client.post(vdc.searchLink("volumes").getHref(), VolumeManagementDto.MEDIA_TYPE,
            VolumeManagementDto.MEDIA_TYPE, dto, VolumeManagementDto.class);
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

    public TaskDto waitForTask(final AcceptedRequestDto< ? > acceptedRequest,
        final int pollInterval, final int maxWait, final TimeUnit timeUnit)
    {
        RESTLink status = acceptedRequest.getStatusLink();

        Stopwatch watch = Stopwatch.createStarted();
        while (watch.elapsed(timeUnit) < maxWait)
        {
            TaskDto task = client.get(status.getHref(), TaskDto.MEDIA_TYPE, TaskDto.class);

            switch (task.getState())
            {
                case FINISHED_SUCCESSFULLY:
                case FINISHED_UNSUCCESSFULLY:
                case ABORTED:
                case ACK_ERROR:
                case CANCELLED:
                    return task;
                case PENDING:
                case QUEUEING:
                case STARTED:
                    // Do nothing and keep waiting
                    break;
            }

            Uninterruptibles.sleepUninterruptibly(pollInterval, timeUnit);
        }

        throw new RuntimeException("Task did not complete in the configured timeout");
    }

    public VirtualMachineDto waitUntilUnlocked(final VirtualMachineDto vm, final int pollInterval,
        final int maxWait, final TimeUnit timeUnit)
    {
        Stopwatch watch = Stopwatch.createStarted();
        while (watch.elapsed(timeUnit) < maxWait)
        {
            VirtualMachineDto refreshed = refresh(vm);
            if (!LOCKED.equals(refreshed.getState()))
            {
                return refreshed;
            }

            Uninterruptibles.sleepUninterruptibly(pollInterval, timeUnit);
        }

        throw new RuntimeException("Virtual machine did not reach the desired state in the configured timeout");
    }
}
