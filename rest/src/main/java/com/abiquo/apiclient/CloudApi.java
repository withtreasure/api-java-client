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
import static com.abiquo.apiclient.domain.Links.create;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;

import com.abiquo.apiclient.domain.options.ExternalIpListOptions;
import com.abiquo.apiclient.domain.options.VirtualApplianceListOptions;
import com.abiquo.apiclient.domain.options.VirtualDatacenterListOptions;
import com.abiquo.apiclient.domain.options.VirtualMachineListOptions;
import com.abiquo.model.enumerator.NetworkType;
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
import com.abiquo.server.core.infrastructure.PublicCloudRegionDto;
import com.abiquo.server.core.infrastructure.network.ExternalIpDto;
import com.abiquo.server.core.infrastructure.network.ExternalIpsDto;
import com.abiquo.server.core.infrastructure.network.VLANNetworkDto;
import com.abiquo.server.core.infrastructure.network.VMNetworkConfigurationDto;
import com.abiquo.server.core.infrastructure.network.VMNetworkConfigurationsDto;
import com.abiquo.server.core.infrastructure.storage.TierDto;
import com.abiquo.server.core.infrastructure.storage.TiersDto;
import com.abiquo.server.core.infrastructure.storage.VolumeManagementDto;
import com.abiquo.server.core.task.TaskDto;
import com.google.common.reflect.TypeToken;

public class CloudApi
{
    private final RestClient client;

    // Package private constructor to be used only by the ApiClient
    CloudApi(final RestClient client)
    {
        this.client = checkNotNull(client, "client cannot be null");
    }

    public VirtualDatacenterDto getVirtualDatacenter(final String id)
    {
        return client.get(VIRTUALDATACENTERS_URL + "/" + id, VirtualDatacenterDto.MEDIA_TYPE,
            VirtualDatacenterDto.class);
    }

    public Iterable<VirtualDatacenterDto> listVirtualDatacenters()
    {
        return client.list(VIRTUALDATACENTERS_URL, VirtualDatacentersDto.MEDIA_TYPE,
            VirtualDatacentersDto.class);
    }

    public Iterable<VirtualDatacenterDto> listVirtualDatacenters(
        final VirtualDatacenterListOptions options)
    {
        return client.list(VIRTUALDATACENTERS_URL, options.queryParams(),
            VirtualDatacentersDto.MEDIA_TYPE, VirtualDatacentersDto.class);
    }

    public Iterable<ExternalIpDto> listExternalIps(final VirtualDatacenterDto vdc)
    {
        return client.list(vdc.searchLink("externalips").getHref(), ExternalIpsDto.MEDIA_TYPE,
            ExternalIpsDto.class);
    }

    public Iterable<ExternalIpDto> listExternalIps(final VirtualDatacenterDto vdc,
        final ExternalIpListOptions options)
    {
        return client.list(vdc.searchLink("externalips").getHref(), options.queryParams(),
            ExternalIpsDto.MEDIA_TYPE, ExternalIpsDto.class);
    }

    public Iterable<VirtualApplianceDto> listVirtualAppliances(final VirtualDatacenterDto vdc)
    {
        return client.list(vdc.searchLink("virtualappliances").getHref(),
            VirtualAppliancesDto.MEDIA_TYPE, VirtualAppliancesDto.class);
    }

    public Iterable<VirtualApplianceDto> listVirtualAppliances(final VirtualDatacenterDto vdc,
        final VirtualApplianceListOptions options)
    {
        return client.list(vdc.searchLink("virtualappliances").getHref(), options.queryParams(),
            VirtualAppliancesDto.MEDIA_TYPE, VirtualAppliancesDto.class);
    }

    public VirtualApplianceDto getVirtualAppliance(final String idVdc, final String idVapp)
    {
        return client.get(
            String.format("%s/%s/virtualappliances/%s", VIRTUALDATACENTERS_URL, idVdc, idVapp),
            VirtualApplianceDto.MEDIA_TYPE, VirtualApplianceDto.class);
    }

    public Iterable<VirtualMachineDto> listVirtualMachines(final VirtualApplianceDto vapp)
    {
        return client.list(vapp.searchLink("virtualmachines").getHref(),
            VirtualMachinesDto.MEDIA_TYPE, VirtualMachinesDto.class);
    }

    public Iterable<VirtualMachineDto> listVirtualMachines(final VirtualApplianceDto vapp,
        final VirtualMachineListOptions options)
    {
        return client.list(vapp.searchLink("virtualmachines").getHref(), options.queryParams(),
            VirtualMachinesDto.MEDIA_TYPE, VirtualMachinesDto.class);
    }

    public VLANNetworkDto getPrivateNetwork(final VirtualDatacenterDto vdc, final String idNetwork)
    {
        return client.get(vdc.searchLink("privatenetworks").getHref() + "/" + idNetwork,
            VLANNetworkDto.MEDIA_TYPE, VLANNetworkDto.class);
    }

    public Iterable<VMNetworkConfigurationDto> listNetworkConfigurations(final VirtualMachineDto vm)
    {
        return client.list(vm.searchLink("configurations").getHref(),
            VMNetworkConfigurationsDto.MEDIA_TYPE, VMNetworkConfigurationsDto.class);
    }

    public VirtualMachineDto getVirtualMachine(final VirtualApplianceDto vapp, final String idVm)
    {
        return client.get(vapp.searchLink("virtualmachines").getHref() + "/" + idVm,
            VirtualMachineDto.MEDIA_TYPE, VirtualMachineDto.class);
    }

    public VirtualDatacenterDto createVirtualDatacenter(final SingleResourceTransportDto location,
        final EnterpriseDto enterprise, final String name, final String type,
        final String vlanAddress, final String vlanGateway, final String vlanName)
    {
        checkArgument(location instanceof DatacenterDto || location instanceof PublicCloudRegionDto);
        String mt =
            location instanceof DatacenterDto ? DatacenterDto.SHORT_MEDIA_TYPE_JSON
                : PublicCloudRegionDto.SHORT_MEDIA_TYPE_JSON;

        VirtualDatacenterDto vdc = new VirtualDatacenterDto();
        vdc.setName(name);
        vdc.setHypervisorType(type);

        vdc.addLink(create("enterprise", enterprise.getEditLink().getHref(),
            EnterpriseDto.SHORT_MEDIA_TYPE_JSON));
        vdc.addLink(create("location", location.searchLink("self").getHref(), mt));

        VLANNetworkDto vlan = new VLANNetworkDto();
        vlan.setAddress(vlanAddress);
        vlan.setGateway(vlanGateway);
        vlan.setMask(24);
        vlan.setName(vlanName);
        vlan.setType(NetworkType.INTERNAL);
        vdc.setVlan(vlan);

        return client.post(VIRTUALDATACENTERS_URL, VirtualDatacenterDto.MEDIA_TYPE,
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

    public VirtualMachineDto createVirtualMachine(final VirtualMachineTemplateDto template,
        final VirtualApplianceDto vapp)
    {
        VirtualMachineDto vm = new VirtualMachineDto();
        vm.setVdrpEnabled(Boolean.TRUE);
        vm.addLink(create("virtualmachinetemplate", template.getEditLink().getHref(),
            VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON));

        return client
            .post(vapp.searchLink("virtualmachines").getHref(), VirtualMachineDto.MEDIA_TYPE,
                VirtualMachineDto.MEDIA_TYPE, vm, VirtualMachineDto.class);
    }

    public VirtualMachineDto deploy(final VirtualMachineDto vm, final int pollInterval,
        final int maxWait, final TimeUnit timeUnit)
    {
        return deploy(vm, false, pollInterval, maxWait, timeUnit);
    }

    public VirtualMachineDto deploy(final VirtualMachineDto vm, final boolean forceDeploy,
        final int pollInterval, final int maxWait, final TimeUnit timeUnit)
    {
        client.post(vm.searchLink("deploy").getHref() + "?force=" + forceDeploy,
            AcceptedRequestDto.MEDIA_TYPE, new TypeToken<AcceptedRequestDto<String>>()
            {
                private static final long serialVersionUID = -6348281615419377868L;
            });

        VirtualMachineDto refreshed = client.waitUntilUnlocked(vm, pollInterval, maxWait, timeUnit);
        if (!refreshed.getState().isDeployed())
        {
            throw new RuntimeException("Deploy virtual machine operation failed");
        }

        return refreshed;
    }

    public VirtualApplianceDto deploy(final VirtualApplianceDto vapp, final int pollInterval,
        final int maxWait, final TimeUnit timeUnit)
    {
        return deploy(vapp, false, pollInterval, maxWait, timeUnit);
    }

    public VirtualApplianceDto deploy(final VirtualApplianceDto vapp, final boolean forceDeploy,
        final int pollInterval, final int maxWait, final TimeUnit timeUnit)
    {
        client.post(vapp.searchLink("deploy").getHref() + "?force=" + forceDeploy,
            AcceptedRequestDto.MEDIA_TYPE, new TypeToken<AcceptedRequestDto<String>>()
            {
                private static final long serialVersionUID = -6348281615419377868L;
            });

        VirtualApplianceDto refreshed =
            client.waitUntilUnlocked(vapp, pollInterval, maxWait, timeUnit);
        if (VirtualApplianceState.DEPLOYED != refreshed.getState())
        {
            throw new RuntimeException("Deploy virtual appliance operation failed");
        }

        return refreshed;
    }

    public VirtualMachineDto undeploy(final VirtualMachineDto vm, final boolean forceUndeploy,
        final int pollInterval, final int maxWait, final TimeUnit timeUnit)
    {
        VirtualMachineTaskDto virtualMachineTask = new VirtualMachineTaskDto();
        virtualMachineTask.setForceUndeploy(forceUndeploy);

        client.post(vm.searchLink("undeploy").getHref(), AcceptedRequestDto.MEDIA_TYPE,
            VirtualMachineTaskDto.MEDIA_TYPE, virtualMachineTask,
            new TypeToken<AcceptedRequestDto<String>>()
            {
                private static final long serialVersionUID = -6348281615419377868L;
            });

        VirtualMachineDto refreshed = client.waitUntilUnlocked(vm, pollInterval, maxWait, timeUnit);
        if (refreshed.getState().isDeployed())
        {
            throw new RuntimeException("Undeploy virtual machine operation failed");
        }

        return refreshed;
    }

    public VirtualMachineDto undeploy(final VirtualMachineDto vm, final int pollInterval,
        final int maxWait, final TimeUnit timeUnit)
    {
        return undeploy(vm, false, pollInterval, maxWait, timeUnit);
    }

    public VirtualApplianceDto undeploy(final VirtualApplianceDto vapp,
        final boolean forceUndeploy, final int pollInterval, final int maxWait,
        final TimeUnit timeUnit)
    {
        VirtualMachineTaskDto virtualMachineTask = new VirtualMachineTaskDto();
        virtualMachineTask.setForceUndeploy(forceUndeploy);

        client.post(vapp.searchLink("undeploy").getHref(), AcceptedRequestDto.MEDIA_TYPE,
            VirtualMachineTaskDto.MEDIA_TYPE, virtualMachineTask,
            new TypeToken<AcceptedRequestDto<String>>()
            {
                private static final long serialVersionUID = -6348281615419377868L;
            });

        VirtualApplianceDto refreshed =
            client.waitUntilUnlocked(vapp, pollInterval, maxWait, timeUnit);
        if (VirtualApplianceState.NOT_DEPLOYED != refreshed.getState())
        {
            throw new RuntimeException("Undeploy virtual appliance operation failed");
        }

        return refreshed;
    }

    public VirtualApplianceDto undeploy(final VirtualApplianceDto vapp, final int pollInterval,
        final int maxWait, final TimeUnit timeUnit)
    {
        return undeploy(vapp, false, pollInterval, maxWait, timeUnit);
    }

    public VirtualMachineDto powerState(final VirtualMachineDto vm,
        final VirtualMachineState state, final int pollInterval, final int maxWait,
        final TimeUnit timeUnit)
    {
        VirtualMachineStateDto vmState = new VirtualMachineStateDto();
        vmState.setState(state);

        client.put(vm.searchLink("state").getHref(), AcceptedRequestDto.MEDIA_TYPE,
            VirtualMachineStateDto.MEDIA_TYPE, vmState, new TypeToken<AcceptedRequestDto<String>>()
            {
                private static final long serialVersionUID = -6348281615419377868L;
            });

        VirtualMachineDto refreshed = client.waitUntilUnlocked(vm, pollInterval, maxWait, timeUnit);
        if (state != refreshed.getState())
        {
            throw new RuntimeException("Virtual machine power state '" + state.name()
                + "' operation failed");
        }

        return refreshed;
    }

    public VirtualMachineDto editVirtualMachine(final VirtualMachineDto vm, final int pollInterval,
        final int maxWait, final TimeUnit timeUnit)
    {
        VirtualMachineDto refreshed = null;

        if (vm.getState().isDeployed())
        {
            client.put(vm.getEditLink().getHref(), AcceptedRequestDto.MEDIA_TYPE,
                VirtualMachineDto.MEDIA_TYPE, vm, new TypeToken<AcceptedRequestDto<String>>()
                {
                    private static final long serialVersionUID = -6348281615419377868L;
                });

            refreshed = client.waitUntilUnlocked(vm, pollInterval, maxWait, timeUnit);
            if (VirtualMachineState.OFF != refreshed.getState())
            {
                throw new RuntimeException("Virtual machine reconfigure operation failed");
            }
        }
        else
        {
            client.put(vm.getEditLink().getHref(), AcceptedRequestDto.MEDIA_TYPE,
                VirtualMachineDto.MEDIA_TYPE, vm);
            refreshed = client.refresh(vm);
        }

        return refreshed;
    }

    public VolumeManagementDto getVolume(final VirtualDatacenterDto vdc, final String idVolume)
    {
        return client.get(vdc.searchLink("volumes").getHref() + "/" + idVolume,
            VolumeManagementDto.MEDIA_TYPE, VolumeManagementDto.class);
    }

    public VolumeManagementDto createVolume(final VirtualDatacenterDto vdc, final String name,
        final long sizeInMb, final TierDto tier)
    {
        VolumeManagementDto dto = new VolumeManagementDto();
        dto.setName(name);
        dto.setSizeInMB(sizeInMb);
        dto.addLink(create("tier", tier.searchLink("self").getHref(), TierDto.SHORT_MEDIA_TYPE_JSON));

        return client.post(vdc.searchLink("volumes").getHref(), VolumeManagementDto.MEDIA_TYPE,
            VolumeManagementDto.MEDIA_TYPE, dto, VolumeManagementDto.class);
    }

    public TaskDto getTask(final VirtualMachineDto vm, final String idTask)
    {
        return client.get(vm.searchLink("tasks").getHref() + "/" + idTask, TaskDto.MEDIA_TYPE,
            TaskDto.class);
    }

    public Iterable<TierDto> listTiers(final VirtualDatacenterDto vdc)
    {
        return client.list(vdc.searchLink("tiers").getHref(), TiersDto.MEDIA_TYPE, TiersDto.class);
    }

}
