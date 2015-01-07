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
package com.abiquo.apiclient.templates;

import static com.abiquo.apiclient.domain.Links.create;
import static com.abiquo.server.core.task.TaskState.FINISHED_SUCCESSFULLY;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;

import com.abiquo.apiclient.RestClient;
import com.abiquo.apiclient.domain.options.TemplateListOptions;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateRequestDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplatesDto;
import com.abiquo.server.core.cloud.VirtualDatacenterDto;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.cloud.VirtualMachineInstanceDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.task.TaskDto;
import com.google.common.reflect.TypeToken;

public class TemplatesApi
{
    private final RestClient client;

    public TemplatesApi(final RestClient client)
    {
        this.client = checkNotNull(client, "client cannot be null");
    }

    public Iterable<VirtualMachineTemplateDto> listTemplates(final VirtualDatacenterDto vdc)
    {
        return client.list(vdc.searchLink("templates").getHref(),
            VirtualMachineTemplatesDto.MEDIA_TYPE, VirtualMachineTemplatesDto.class);
    }

    public Iterable<VirtualMachineTemplateDto> listTemplates(final VirtualDatacenterDto vdc,
        final TemplateListOptions options)
    {
        return client.list(vdc.searchLink("templates").getHref(), options.queryParams(),
            VirtualMachineTemplatesDto.MEDIA_TYPE, VirtualMachineTemplatesDto.class);
    }

    public VirtualMachineTemplateDto instanceVirtualMachine(final VirtualMachineDto vm,
        final String snapshotName)
    {
        VirtualMachineInstanceDto instance = new VirtualMachineInstanceDto();
        instance.setInstanceName(snapshotName);
        AcceptedRequestDto<String> acceptedRequest =
            client.post(vm.searchLink("instance").getHref(), AcceptedRequestDto.MEDIA_TYPE,
                VirtualMachineInstanceDto.MEDIA_TYPE, instance,
                new TypeToken<AcceptedRequestDto<String>>()
                {
                    private static final long serialVersionUID = -6348281615419377868L;
                });

        // Wait a maximum of 5 minutes and poll every 5 seconds
        TaskDto task = client.waitForTask(acceptedRequest, 5, 300, TimeUnit.SECONDS);
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
        promote.addLink(create("virtualmachinetemplate", template.getEditLink().getHref(),
            VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON));
        promote.setPromotedName(promotedName);
        AcceptedRequestDto<String> acceptedRequest =
            client.post(template.searchLink("datacenterrepository").getHref()
                + "/virtualmachinetemplates", AcceptedRequestDto.MEDIA_TYPE,
                VirtualMachineTemplateRequestDto.MEDIA_TYPE, promote,
                new TypeToken<AcceptedRequestDto<String>>()
                {
                    private static final long serialVersionUID = -6348281615419377868L;
                });

        // Wait a maximum of 5 minutes and poll every 5 seconds
        TaskDto task = client.waitForTask(acceptedRequest, 5, 300, TimeUnit.SECONDS);
        if (FINISHED_SUCCESSFULLY != task.getState())
        {
            throw new RuntimeException("Promote instance operation failed");
        }

        return client.get(task.searchLink("result").getHref(),
            VirtualMachineTemplateDto.MEDIA_TYPE, VirtualMachineTemplateDto.class);
    }

    public void refreshAppslibrary(final EnterpriseDto enterprise, final DatacenterDto datacenter)
    {
        AcceptedRequestDto<String> acceptedRequest =
            client.put(
                enterprise.searchLink("datacenterrepositories").getHref() + "/"
                    + datacenter.getId() + "/actions/refresh", AcceptedRequestDto.MEDIA_TYPE,
                new TypeToken<AcceptedRequestDto<String>>()
                {
                    private static final long serialVersionUID = -6348281615419377868L;
                });

        // Wait a maximum of 5 minutes and poll every 5 seconds
        TaskDto task = client.waitForTask(acceptedRequest, 5, 300, TimeUnit.SECONDS);
        if (FINISHED_SUCCESSFULLY != task.getState())
        {
            throw new RuntimeException("Refresh repository operation failed");
        }
    }

}
