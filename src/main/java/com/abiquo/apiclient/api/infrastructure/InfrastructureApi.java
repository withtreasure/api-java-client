package com.abiquo.apiclient.api.infrastructure;

import java.util.List;

import com.abiquo.apiclient.api.ApiClient;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.server.core.enterprise.DatacenterLimitsDto;
import com.abiquo.server.core.enterprise.DatacentersLimitsDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.infrastructure.DatacentersDto;
import com.abiquo.server.core.infrastructure.RackDto;
import com.abiquo.server.core.infrastructure.RacksDto;
import com.abiquo.server.core.infrastructure.RemoteServiceDto;
import com.abiquo.server.core.infrastructure.RemoteServicesDto;
import com.abiquo.server.core.infrastructure.network.VLANNetworkDto;
import com.abiquo.server.core.infrastructure.network.VLANNetworksDto;

public class InfrastructureApi extends ApiClient
{

    public InfrastructureApi(final String baseURL, final String username, final String password)
    {
        super(baseURL, username, password);
    }

    @Override
    public DatacenterDto findDatacenter(final String name)
    {
        DatacentersDto datacenters =
            client.get(absolute("/admin/datacenters"), DatacentersDto.MEDIA_TYPE,
                DatacentersDto.class);
        return datacenters.getCollection().stream()
            .filter(datacenter -> datacenter.getName().equals(name)).findFirst().get();
    }

    @Override
    public RackDto findRack(final DatacenterDto datacenter, final String name)
    {
        RacksDto racks =
            client.get(datacenter.searchLink("racks").getHref(), RacksDto.MEDIA_TYPE,
                RacksDto.class);
        return racks.getCollection().stream().filter(rack -> rack.getName().equals(name))
            .findFirst().get();
    }

    @Override
    public DatacenterDto findDatacenterLocation(final String name)
    {
        DatacentersDto locations =
            client.get(absolute("/cloud/locations"), DatacentersDto.MEDIA_TYPE,
                DatacentersDto.class);
        return locations.getCollection().stream()
            .filter(location -> location.getName().equals(name)).findFirst().get();
    }

    @Override
    public DatacentersLimitsDto listLimits(final EnterpriseDto enterprise)
    {
        return get(enterprise.searchLink("limits"), DatacentersLimitsDto.class);
    }

    @Override
    public DatacenterLimitsDto findLimits(final EnterpriseDto enterprise, final String locationName)
    {
        return listLimits(enterprise).getCollection().stream()
            .filter(l -> locationName.equals(l.searchLink("location").getTitle())).findFirst()
            .get();
    }

    @Override
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

    @Override
    public VLANNetworksDto listExternalNetworks(final DatacenterLimitsDto limits)
    {
        return client.get(limits.searchLink("externalnetworks").getHref(),
            VLANNetworksDto.MEDIA_TYPE, VLANNetworksDto.class);
    }

    @Override
    public VLANNetworkDto findExternalNetwork(final DatacenterLimitsDto limits, final String name)
    {
        return listExternalNetworks(limits).getCollection().stream()
            .filter(net -> net.getName().equals(name)).findFirst().get();
    }

    @Override
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

    @Override
    public RemoteServicesDto listRemoteServices(final DatacenterDto datacenter)
    {
        return get(datacenter.searchLink("remoteservices"), RemoteServicesDto.class);
    }

    @Override
    public RackDto createRack(final DatacenterDto datacenter, final String name)
    {
        RackDto rack = new RackDto();
        rack.setName(name);
        return client.post(datacenter.searchLink("racks").getHref(), RackDto.MEDIA_TYPE,
            RackDto.MEDIA_TYPE, rack, RackDto.class);
    }

    @Override
    public void addDatacenterToEnterprise(final EnterpriseDto enterprise,
        final DatacenterDto datacenter)
    {
        DatacenterLimitsDto limits = new DatacenterLimitsDto();
        limits.addLink(new RESTLink("location", datacenter.getEditLink().getHref()));
        client.post(enterprise.searchLink("limits").getHref(), DatacenterLimitsDto.MEDIA_TYPE,
            DatacenterLimitsDto.MEDIA_TYPE, limits, DatacenterLimitsDto.class);
    }

}
