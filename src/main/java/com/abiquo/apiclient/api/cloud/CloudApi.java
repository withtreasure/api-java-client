/**
 * Copyright (C) 2008 - Abiquo Holdings S.L. All rights reserved.
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */

package com.abiquo.apiclient.api.cloud;

import static com.abiquo.apiclient.api.ApiPath.LOCATIONS_URL;
import static com.abiquo.apiclient.api.ApiPath.VIRTUALDATACENTERS_URL;
import static com.abiquo.apiclient.api.ApiPredicates.datacenterName;
import static com.abiquo.apiclient.api.ApiPredicates.virtualApplianceName;
import static com.abiquo.apiclient.api.ApiPredicates.virtualDatacenterName;
import static com.abiquo.apiclient.api.ApiPredicates.withTemplate;
import static com.abiquo.server.core.cloud.VirtualMachineState.LOCKED;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.find;

import java.util.concurrent.TimeUnit;

import com.abiquo.apiclient.rest.RestClient;
import com.abiquo.model.enumerator.NetworkType;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;
import com.abiquo.server.core.cloud.VirtualApplianceDto;
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
import com.abiquo.server.core.infrastructure.DatacentersDto;
import com.abiquo.server.core.infrastructure.PublicCloudRegionDto;
import com.abiquo.server.core.infrastructure.network.ExternalIpsDto;
import com.abiquo.server.core.infrastructure.network.VLANNetworkDto;
import com.abiquo.server.core.infrastructure.network.VMNetworkConfigurationsDto;
import com.abiquo.server.core.infrastructure.storage.TierDto;
import com.abiquo.server.core.infrastructure.storage.VolumeManagementDto;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.GenericType;

public class CloudApi
{

    private final RestClient client;

    public CloudApi(final RestClient client)
    {
        this.client = client;

    }

    public DatacenterDto findDatacenterLocation(final String name)
    {
        DatacentersDto locations =
            client.get(LOCATIONS_URL, DatacentersDto.MEDIA_TYPE, DatacentersDto.class);
        return find(locations.getCollection(), datacenterName(name));
    }

    public VirtualDatacenterDto findVirtualDatacenter(final String name)
    {
        VirtualDatacentersDto vdcs =
            client.get(VIRTUALDATACENTERS_URL, VirtualDatacentersDto.MEDIA_TYPE,
                VirtualDatacentersDto.class);
        return find(vdcs.getCollection(), virtualDatacenterName(name));
    }

    public ExternalIpsDto listExternalIps(final VirtualDatacenterDto vdc)
    {
        return client.get(vdc.searchLink("externalips"), ExternalIpsDto.class);
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
        return find(vapps.getCollection(), virtualApplianceName(name));
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
        return find(vms.getCollection(), withTemplate(templateName));
    }

    public VirtualDatacenterDto createVirtualDatacenter(final SingleResourceTransportDto location,
        final EnterpriseDto enterprise, final String name, final String type,
        final String vlanAddress, final String vlanGateway, final String vlanName)
    {
        checkArgument(location instanceof DatacenterDto || location instanceof PublicCloudRegionDto);

        VirtualDatacenterDto vdc = new VirtualDatacenterDto();
        vdc.setName(name);
        vdc.setHypervisorType(type);

        vdc.addLink(new RESTLink("enterprise", enterprise.getEditLink().getHref()));
        vdc.addLink(new RESTLink("location", location.searchLink("self").getHref()));

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
            refreshed = client.refresh(vm);
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

    private VirtualMachineDto waitUntilUnlocked(final VirtualMachineDto vm, final int pollInterval,
        final int maxWait, final TimeUnit timeUnit)
    {
        Stopwatch watch = Stopwatch.createStarted();
        while (watch.elapsed(timeUnit) < maxWait)
        {
            VirtualMachineDto refreshed = client.refresh(vm);
            if (!LOCKED.equals(refreshed.getState()))
            {
                return refreshed;
            }

            Uninterruptibles.sleepUninterruptibly(pollInterval, timeUnit);
        }

        throw new RuntimeException("Virtual machine did not reach the desired state in the configured timeout");
    }
}
