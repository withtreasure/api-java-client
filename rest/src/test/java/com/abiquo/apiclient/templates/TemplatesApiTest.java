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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.abiquo.apiclient.BaseMockTest;
import com.abiquo.apiclient.domain.options.TemplateListOptions;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.AcceptedRequestDto;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.appslibrary.DatacenterRepositoriesDto;
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
import com.abiquo.server.core.task.TaskState;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

@Test
public class TemplatesApiTest extends BaseMockTest
{
    public void testListTemplates() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualMachineTemplatesDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("templates.json"));

        server.enqueue(response);
        server.play();

        VirtualDatacenterDto vdc = new VirtualDatacenterDto();
        RESTLink link = new RESTLink("templates", "/cloud/virtualdatacenters/1/action/templates");
        link.setType(VirtualMachineTemplatesDto.SHORT_MEDIA_TYPE_JSON);
        vdc.addLink(link);

        newApiClient().getTemplatesApi().listTemplates(vdc);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET", "/cloud/virtualdatacenters/1/action/templates");
        assertAccept(request, VirtualMachineTemplatesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testListTemplatesWithOptions() throws Exception
    {
        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualMachineTemplatesDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("templates.json"));

        server.enqueue(response);
        server.play();

        VirtualDatacenterDto vdc = new VirtualDatacenterDto();
        RESTLink link = new RESTLink("templates", "/cloud/virtualdatacenters/1/action/templates");
        link.setType(VirtualMachineTemplatesDto.SHORT_MEDIA_TYPE_JSON);
        vdc.addLink(link);

        TemplateListOptions options =
            TemplateListOptions.builder().limit(0).category("foo").build();
        newApiClient().getTemplatesApi().listTemplates(vdc, options);

        RecordedRequest request = server.takeRequest();

        assertRequest(request, "GET",
            "/cloud/virtualdatacenters/1/action/templates?categoryName=foo&limit=0");
        assertAccept(request, VirtualMachineTemplatesDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testInstanceVirtualMachine() throws Exception
    {
        AcceptedRequestDto<String> acceptDto = new AcceptedRequestDto<String>();
        RESTLink link =
            new RESTLink("status",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/tasks/1");
        link.setType(TaskDto.SHORT_MEDIA_TYPE_JSON);
        acceptDto.addLink(link);

        server.enqueue(new MockResponse().addHeader("Content-type",
            AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(acceptDto)));

        TaskDto completed = new TaskDto();
        completed.setState(TaskState.FINISHED_SUCCESSFULLY);
        link =
            new RESTLink("result",
                "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1");
        link.setType(VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);
        completed.addLink(link);

        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(json.write(completed)));

        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("template.json"));

        server.enqueue(response);

        server.play();

        VirtualMachineDto dto = new VirtualMachineDto();
        link =
            new RESTLink("instance",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/instance");
        link.setType(VirtualMachineInstanceDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getTemplatesApi().instanceVirtualMachine(dto, "GRML-Small-Functional");

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 3);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/action/instance");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualMachineInstanceDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        VirtualMachineInstanceDto requestBody = readBody(request, VirtualMachineInstanceDto.class);
        assertEquals(requestBody.getInstanceName(), "GRML-Small-Functional");

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/tasks/1");
        assertAccept(second, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);

        // Verify the third request
        RecordedRequest third = server.takeRequest();
        assertRequest(third, "GET",
            "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1");
        assertAccept(third, VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testPromoteInstance() throws Exception
    {
        AcceptedRequestDto<String> acceptDto = new AcceptedRequestDto<String>();
        RESTLink link =
            new RESTLink("status",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/tasks/1");
        link.setType(TaskDto.SHORT_MEDIA_TYPE_JSON);
        acceptDto.addLink(link);

        server.enqueue(new MockResponse().addHeader("Content-type",
            AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(acceptDto)));

        TaskDto completed = new TaskDto();
        completed.setState(TaskState.FINISHED_SUCCESSFULLY);
        link =
            new RESTLink("result",
                "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1");
        link.setType(VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);
        completed.addLink(link);

        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(json.write(completed)));

        MockResponse response = new MockResponse() //
            .setHeader("Content-Type", VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON) //
            .setBody(payloadFromResource("template.json"));

        server.enqueue(response);

        server.play();

        VirtualMachineTemplateDto dto = new VirtualMachineTemplateDto();
        link =
            new RESTLink("edit",
                "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1");
        link.setType(VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);
        link =
            new RESTLink("datacenterrepository", "/admin/enterprises/1/datacenterrepositories/1");
        link.setType(DatacenterRepositoryDto.SHORT_MEDIA_TYPE_JSON);
        dto.addLink(link);

        newApiClient().getTemplatesApi().promoteInstance(dto, "promotedNameTest");

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 3);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualMachineTemplateRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        VirtualMachineTemplateRequestDto requestBody =
            readBody(request, VirtualMachineTemplateRequestDto.class);
        assertLinkExist(requestBody, dto.getEditLink().getHref(), "virtualmachinetemplate",
            VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);
        assertEquals(requestBody.getPromotedName(), "promotedNameTest");

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/tasks/1");
        assertAccept(second, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);

        // Verify the third request
        RecordedRequest third = server.takeRequest();
        assertRequest(third, "GET",
            "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1");
        assertAccept(third, VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
    }

    public void testRefreshLibrary() throws Exception
    {
        AcceptedRequestDto<String> acceptDto = new AcceptedRequestDto<String>();
        RESTLink link =
            new RESTLink("status",
                "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/tasks/1");
        link.setType(TaskDto.SHORT_MEDIA_TYPE_JSON);
        acceptDto.addLink(link);

        server.enqueue(new MockResponse().addHeader("Content-type",
            AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(acceptDto)));

        TaskDto completed = new TaskDto();
        completed.setState(TaskState.FINISHED_SUCCESSFULLY);
        link =
            new RESTLink("result",
                "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1");
        link.setType(VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);
        completed.addLink(link);

        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(json.write(completed)));

        server.play();

        DatacenterDto dc = new DatacenterDto();
        dc.setId(1);
        EnterpriseDto enterprise = new EnterpriseDto();
        link =
            new RESTLink("datacenterrepositories", "/admin/enterprises/1/datacenterrepositories");
        link.setType(DatacenterRepositoriesDto.SHORT_MEDIA_TYPE_JSON);
        enterprise.addLink(link);

        newApiClient().getTemplatesApi().refreshAppslibrary(enterprise, dc);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 2);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "PUT",
            "/admin/enterprises/1/datacenterrepositories/1/actions/refresh");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET",
            "/cloud/virtualdatacenters/1/virtualappliances/1/virtualmachines/1/tasks/1");
        assertAccept(second, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);
    }

    public void testCreatePersistent() throws Exception
    {

        AcceptedRequestDto<String> acceptDto = new AcceptedRequestDto<String>();
        RESTLink link =
            new RESTLink("status",
                "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1/tasks/1");
        link.setType(TaskDto.SHORT_MEDIA_TYPE_JSON);
        acceptDto.addLink(link);

        server.enqueue(new MockResponse().addHeader("Content-type",
            AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(acceptDto)));

        TaskDto inProgress = new TaskDto();
        inProgress.setState(TaskState.PENDING);
        TaskDto completed = new TaskDto();
        completed.setState(TaskState.FINISHED_SUCCESSFULLY);
        link =
            new RESTLink("result",
                "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1");
        link.setType(VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);
        completed.addLink(link);

        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(json.write(inProgress)));
        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(json.write(completed)));

        MockResponse response =
            new MockResponse().setHeader("Content-Type",
                VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON).setBody(
                payloadFromResource("persistentTemplate.json"));

        server.enqueue(response);
        server.play();

        TierDto tier = new TierDto();
        link = new RESTLink("self", "/cloud/virtualdatacenters/1/tiers/1");
        link.setType(TierDto.SHORT_MEDIA_TYPE_JSON);
        tier.addLink(link);

        VirtualDatacenterDto vdc = new VirtualDatacenterDto();
        link = new RESTLink("edit", "/cloud/virtualdatacenters/1");
        link.setType(VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON);
        vdc.addLink(link);

        VirtualMachineTemplateDto vmt = new VirtualMachineTemplateDto();
        link =
            new RESTLink("edit",
                "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1");
        link.setType(VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);
        vmt.addLink(link);
        link =
            new RESTLink("datacenterrepository", "/admin/enterprises/1/datacenterrepositories/1");
        link.setType(DatacenterRepositoriesDto.SHORT_MEDIA_TYPE_JSON);
        vmt.addLink(link);

        newApiClient().getTemplatesApi().createPersistent(vdc, vmt, "persistentName", tier, 1, 300,
            TimeUnit.SECONDS);

        // Make sure the polling has retried once
        assertEquals(server.getRequestCount(), 4);

        // Verify the first request
        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualMachineTemplatePersistentDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        VirtualMachineTemplatePersistentDto requestBody =
            readBody(request, VirtualMachineTemplatePersistentDto.class);
        assertEquals(requestBody.getPersistentTemplateName(), "persistentName");
        assertEquals(requestBody.getPersistentVolumeName(), "persistentName");
        assertLinkExist(requestBody, requestBody.searchLink("tier").getHref(), "tier",
            TierDto.SHORT_MEDIA_TYPE_JSON);
        assertLinkExist(requestBody, requestBody.searchLink("virtualdatacenter").getHref(),
            "virtualdatacenter", VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON);
        assertLinkExist(requestBody, requestBody.searchLink("virtualmachinetemplate").getHref(),
            "virtualmachinetemplate", VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);

        // Verify the second request
        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET",
            "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1/tasks/1");
        assertAccept(second, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);

        // Verify the third request
        RecordedRequest third = server.takeRequest();
        assertRequest(third, "GET",
            "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1/tasks/1");
        assertAccept(third, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);

        // Verify the fourth request
        RecordedRequest fourth = server.takeRequest();
        assertRequest(fourth, "GET",
            "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1");
        assertAccept(fourth, VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

    }

    public void testCreatePersistentFails() throws Exception
    {

        AcceptedRequestDto<String> acceptDto = new AcceptedRequestDto<String>();
        RESTLink link =
            new RESTLink("status",
                "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1/tasks/1");
        link.setType(TaskDto.SHORT_MEDIA_TYPE_JSON);
        acceptDto.addLink(link);

        server.enqueue(new MockResponse().addHeader("Content-type",
            AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON).setBody(json.write(acceptDto)));

        TaskDto unsuccesful = new TaskDto();
        unsuccesful.setState(TaskState.FINISHED_UNSUCCESSFULLY);
        link =
            new RESTLink("result",
                "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1");
        link.setType(VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);
        unsuccesful.addLink(link);

        server.enqueue(new MockResponse().addHeader("Content-type", TaskDto.SHORT_MEDIA_TYPE_JSON)
            .setBody(json.write(unsuccesful)));

        server.play();

        TierDto tier = new TierDto();
        link = new RESTLink("self", "/cloud/virtualdatacenters/1/tiers/1");
        link.setType(TierDto.SHORT_MEDIA_TYPE_JSON);
        tier.addLink(link);

        VirtualDatacenterDto vdc = new VirtualDatacenterDto();
        link = new RESTLink("edit", "/cloud/virtualdatacenters/1");
        link.setType(VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON);
        vdc.addLink(link);

        VirtualMachineTemplateDto vmt = new VirtualMachineTemplateDto();
        link =
            new RESTLink("edit",
                "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1");
        link.setType(VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);
        vmt.addLink(link);
        link =
            new RESTLink("datacenterrepository", "/admin/enterprises/1/datacenterrepositories/1");
        link.setType(DatacenterRepositoriesDto.SHORT_MEDIA_TYPE_JSON);
        vmt.addLink(link);

        try
        {
            newApiClient().getTemplatesApi().createPersistent(vdc, vmt, "persistentName", tier, 1,
                300, TimeUnit.SECONDS);
            fail("Persistent operation failed");
        }
        catch (RuntimeException ex)
        {
            assertEquals("Persistent operation failed", ex.getMessage());
        }

        assertEquals(server.getRequestCount(), 2);

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "POST",
            "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates");
        assertAccept(request, AcceptedRequestDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);
        assertContentType(request, VirtualMachineTemplatePersistentDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

        VirtualMachineTemplatePersistentDto requestBody =
            readBody(request, VirtualMachineTemplatePersistentDto.class);
        assertEquals(requestBody.getPersistentTemplateName(), "persistentName");
        assertEquals(requestBody.getPersistentVolumeName(), "persistentName");
        assertLinkExist(requestBody, requestBody.searchLink("tier").getHref(), "tier",
            TierDto.SHORT_MEDIA_TYPE_JSON);
        assertLinkExist(requestBody, requestBody.searchLink("virtualdatacenter").getHref(),
            "virtualdatacenter", VirtualDatacenterDto.SHORT_MEDIA_TYPE_JSON);
        assertLinkExist(requestBody, requestBody.searchLink("virtualmachinetemplate").getHref(),
            "virtualmachinetemplate", VirtualMachineTemplateDto.SHORT_MEDIA_TYPE_JSON);

        RecordedRequest second = server.takeRequest();
        assertRequest(second, "GET",
            "/admin/enterprises/1/datacenterrepositories/1/virtualmachinetemplates/1/tasks/1");
        assertAccept(second, TaskDto.SHORT_MEDIA_TYPE_JSON, SingleResourceTransportDto.API_VERSION);

    }

    public void testGetDatacenterRepository() throws Exception
    {
        MockResponse response =
            new MockResponse().setHeader("Content-Type",
                DatacenterRepositoryDto.SHORT_MEDIA_TYPE_JSON).setBody(
                payloadFromResource("repository.json"));

        server.enqueue(response);
        server.play();

        DatacenterDto datacenter = new DatacenterDto();
        datacenter.setId(1);
        EnterpriseDto enterprise = new EnterpriseDto();
        RESTLink link =
            new RESTLink("datacenterrepositories", "/admin/enterprises/1/datacenterrepositories");
        link.setType(DatacenterRepositoriesDto.SHORT_MEDIA_TYPE_JSON);
        enterprise.addLink(link);

        newApiClient().getTemplatesApi().getDatacenterRepository(enterprise, datacenter);

        RecordedRequest request = server.takeRequest();
        assertRequest(request, "GET", "/admin/enterprises/1/datacenterrepositories/1");
        assertAccept(request, DatacenterRepositoryDto.SHORT_MEDIA_TYPE_JSON,
            SingleResourceTransportDto.API_VERSION);

    }
}
