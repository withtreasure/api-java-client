package com.abiquo.apiclient.api.cloud;

import static com.google.common.base.Preconditions.checkArgument;

import com.abiquo.apiclient.api.ApiClient;
import com.abiquo.model.enumerator.NetworkType;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;
import com.abiquo.server.core.cloud.VirtualApplianceDto;
import com.abiquo.server.core.cloud.VirtualAppliancesDto;
import com.abiquo.server.core.cloud.VirtualDatacenterDto;
import com.abiquo.server.core.cloud.VirtualDatacentersDto;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.cloud.VirtualMachinesDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.infrastructure.PublicCloudRegionDto;
import com.abiquo.server.core.infrastructure.network.ExternalIpsDto;
import com.abiquo.server.core.infrastructure.network.VLANNetworkDto;
import com.abiquo.server.core.infrastructure.network.VMNetworkConfigurationsDto;

public class CloudApi extends ApiClient
{

    public CloudApi(final String baseURL, final String username, final String password)
    {
        super(baseURL, username, password);
    }

    @Override
    public VirtualDatacenterDto findVirtualDatacenter(final String name)
    {
        VirtualDatacentersDto vdcs =
            client.get(absolute("/cloud/virtualdatacenters"), VirtualDatacentersDto.MEDIA_TYPE,
                VirtualDatacentersDto.class);
        return vdcs.getCollection().stream().filter(vdc -> vdc.getName().equals(name)).findFirst()
            .get();
    }

    @Override
    public ExternalIpsDto listExternalIps(final VirtualDatacenterDto vdc)
    {
        return get(vdc.searchLink("externalips"), ExternalIpsDto.class);
    }

    @Override
    public VirtualAppliancesDto listVirtualAppliances(final VirtualDatacenterDto vdc)
    {
        return client.get(vdc.searchLink("virtualappliances").getHref(),
            VirtualAppliancesDto.MEDIA_TYPE, VirtualAppliancesDto.class);
    }

    @Override
    public VirtualApplianceDto findVirtualAppliance(final VirtualDatacenterDto vdc,
        final String name)
    {
        VirtualAppliancesDto vapps = listVirtualAppliances(vdc);
        return vapps.getCollection().stream().filter(vapp -> vapp.getName().equals(name))
            .findFirst().get();
    }

    @Override
    public VirtualMachinesDto listVirtualMachines(final VirtualApplianceDto vapp)
    {
        return client.get(vapp.searchLink("virtualmachines").getHref(),
            VirtualMachinesDto.MEDIA_TYPE, VirtualMachinesDto.class);
    }

    @Override
    public VMNetworkConfigurationsDto listNetworkConfigurations(final VirtualMachineDto vm)
    {
        return client.get(vm.searchLink("configurations").getHref(),
            VMNetworkConfigurationsDto.MEDIA_TYPE, VMNetworkConfigurationsDto.class);
    }

    @Override
    public VirtualMachineDto findVirtualMachine(final VirtualApplianceDto vapp,
        final String templateName)
    {
        VirtualMachinesDto vms = listVirtualMachines(vapp);
        return vms.getCollection().stream()
            .filter(vm -> vm.searchLink("virtualmachinetemplate").getTitle().equals(templateName))
            .findFirst().get();
    }

    @Override
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

    @Override
    public VirtualApplianceDto createVirtualAppliance(final VirtualDatacenterDto vdc,
        final String name)
    {
        VirtualApplianceDto vapp = new VirtualApplianceDto();
        vapp.setName(name);

        return client.post(vdc.searchLink("virtualappliances").getHref(),
            VirtualApplianceDto.MEDIA_TYPE, VirtualApplianceDto.MEDIA_TYPE, vapp,
            VirtualApplianceDto.class);
    }

    @Override
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
}
