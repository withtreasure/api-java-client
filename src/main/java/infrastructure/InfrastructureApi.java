package infrastructure;

import com.abiquo.apiclient.api.ApiClient;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.infrastructure.DatacentersDto;
import com.abiquo.server.core.infrastructure.RackDto;
import com.abiquo.server.core.infrastructure.RacksDto;

public class InfrastructureApi extends ApiClient
{

    public InfrastructureApi(final String baseURL, final String username, final String password)
    {
        super(baseURL, username, password);
    }

    public static DatacenterDto findDatacenter(final String name)
    {
        DatacentersDto datacenters =
            client.get(absolute("/admin/datacenters"), DatacentersDto.MEDIA_TYPE,
                DatacentersDto.class);
        return datacenters.getCollection().stream()
            .filter(datacenter -> datacenter.getName().equals(name)).findFirst().get();
    }

    public static RackDto findRack(final DatacenterDto datacenter, final String name)
    {
        RacksDto racks =
            client.get(datacenter.searchLink("racks").getHref(), RacksDto.MEDIA_TYPE,
                RacksDto.class);
        return racks.getCollection().stream().filter(rack -> rack.getName().equals(name))
            .findFirst().get();
    }

    public static DatacenterDto findDatacenterLocation(final String name)
    {
        DatacentersDto locations =
            client.get(absolute("/cloud/locations"), DatacentersDto.MEDIA_TYPE,
                DatacentersDto.class);
        return locations.getCollection().stream()
            .filter(location -> location.getName().equals(name)).findFirst().get();
    }
}
