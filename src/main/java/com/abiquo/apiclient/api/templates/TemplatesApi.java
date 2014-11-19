package com.abiquo.apiclient.api.templates;

import com.abiquo.apiclient.api.ApiClient;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplatesDto;
import com.abiquo.server.core.cloud.VirtualDatacenterDto;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TemplatesApi extends ApiClient
{

    public TemplatesApi(final String baseURL, final String username, final String password)
    {
        super(baseURL, username, password);
    }

    public static VirtualMachineTemplateDto findAvailableTemplate(final VirtualDatacenterDto vdc,
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
}
