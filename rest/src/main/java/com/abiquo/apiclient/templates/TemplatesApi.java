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
import com.abiquo.server.core.appslibrary.DatacenterRepositoryDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplatePersistentDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplateRequestDto;
import com.abiquo.server.core.appslibrary.VirtualMachineTemplatesDto;
import com.abiquo.server.core.cloud.VirtualDatacenterDto;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.cloud.VirtualMachineInstanceDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.infrastructure.storage.TierDto;
import com.abiquo.server.core.task.TaskDto;
import com.abiquo.server.core.task.TasksDto;
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
        final String snapshotName, final int pollInterval, final int maxWait,
        final TimeUnit timeUnit)
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

        TaskDto task = client.waitForTask(acceptedRequest, pollInterval, maxWait, timeUnit);
        if (FINISHED_SUCCESSFULLY != task.getState())
        {
            throw new RuntimeException("Virtual machine instance operation failed");
        }

        return client.get(task.searchLink("result").getHref(),
            VirtualMachineTemplateDto.MEDIA_TYPE, VirtualMachineTemplateDto.class);
    }

    public VirtualMachineTemplateDto promoteInstance(final VirtualMachineTemplateDto template,
        final String promotedName, final int pollInterval, final int maxWait,
        final TimeUnit timeUnit)
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

        TaskDto task = client.waitForTask(acceptedRequest, pollInterval, maxWait, timeUnit);
        if (FINISHED_SUCCESSFULLY != task.getState())
        {
            throw new RuntimeException("Promote instance operation failed");
        }

        return client.get(task.searchLink("result").getHref(),
            VirtualMachineTemplateDto.MEDIA_TYPE, VirtualMachineTemplateDto.class);
    }

    public void refreshAppslibrary(final EnterpriseDto enterprise, final DatacenterDto datacenter,
        final int pollInterval, final int maxWait, final TimeUnit timeUnit)
    {
        AcceptedRequestDto<String> acceptedRequest =
            client.put(
                enterprise.searchLink("datacenterrepositories").getHref() + "/"
                    + datacenter.getId() + "/actions/refresh", AcceptedRequestDto.MEDIA_TYPE,
                new TypeToken<AcceptedRequestDto<String>>()
                {
                    private static final long serialVersionUID = -6348281615419377868L;
                });

        TaskDto task = client.waitForTask(acceptedRequest, pollInterval, maxWait, timeUnit);
        if (FINISHED_SUCCESSFULLY != task.getState())
        {
            throw new RuntimeException("Refresh repository operation failed");
        }
    }

    public DatacenterRepositoryDto getDatacenterRepository(final EnterpriseDto enterprise,
        final DatacenterDto datacenter)
    {
        return client
            .get(
                enterprise.searchLink("datacenterrepositories").getHref() + "/"
                    + datacenter.getId(), DatacenterRepositoryDto.MEDIA_TYPE,
                DatacenterRepositoryDto.class);
    }

    public VirtualMachineTemplateDto createPersistent(final VirtualDatacenterDto vdc,
        final VirtualMachineTemplateDto vmt, final String persistentTemplateName,
        final TierDto tier, final int pollInterval, final int maxWait, final TimeUnit unit)
    {

        VirtualMachineTemplatePersistentDto persistentTemplateDto =
            new VirtualMachineTemplatePersistentDto();
        persistentTemplateDto.setPersistentTemplateName(persistentTemplateName);
        persistentTemplateDto.setPersistentVolumeName(persistentTemplateDto
            .getPersistentTemplateName());
        persistentTemplateDto.addLink(create("tier", tier.searchLink("self").getHref(), tier
            .searchLink("self").getType()));
        persistentTemplateDto.addLink(create("virtualdatacenter", vdc.getEditLink().getHref(), vdc
            .getEditLink().getType()));
        persistentTemplateDto.addLink(create("virtualmachinetemplate", vmt.getEditLink().getHref(),
            vmt.getEditLink().getType()));

        AcceptedRequestDto<String> acceptedRequest =
            client.post(vmt.searchLink("datacenterrepository").getHref()
                + "/virtualmachinetemplates", AcceptedRequestDto.MEDIA_TYPE,
                VirtualMachineTemplatePersistentDto.MEDIA_TYPE, persistentTemplateDto,
                new TypeToken<AcceptedRequestDto<String>>()
                {
                    private static final long serialVersionUID = -6348281615419377868L;
                });
        TaskDto task = client.waitForTask(acceptedRequest, pollInterval, maxWait, unit);
        if (FINISHED_SUCCESSFULLY != task.getState())
        {
            throw new RuntimeException("Persistent operation failed");
        }

        return client.get(task.searchLink("result").getHref(),
            VirtualMachineTemplateDto.MEDIA_TYPE, VirtualMachineTemplateDto.class);
    }

    public Iterable<TaskDto> getVirtualMachineTemplateTasks(final VirtualMachineTemplateDto vmt)
    {
        return client.list(vmt.searchLink("tasks").getHref(), TasksDto.MEDIA_TYPE, TasksDto.class);
    }
}
