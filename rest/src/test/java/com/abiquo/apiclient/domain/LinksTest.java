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
package com.abiquo.apiclient.domain;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.server.core.cloud.VirtualMachineDto;

@Test
public class LinksTest
{
    public void testCreate()
    {
        RESTLink link = Links.create("edit", "http://localhost", VirtualMachineDto.MEDIA_TYPE);

        assertEquals(link.getRel(), "edit");
        assertEquals(link.getHref(), "http://localhost");
        assertEquals(link.getType(), VirtualMachineDto.MEDIA_TYPE);
        assertNull(link.getTitle());
    }

    public void testEditOrSelf()
    {
        VirtualMachineDto dto = new VirtualMachineDto();
        dto.addLink(new RESTLink("self", "http://localhost/foo"));

        RESTLink link = Links.editOrSelf(dto);
        assertEquals(link.getRel(), "self");
        assertEquals(link.getHref(), "http://localhost/foo");

        dto.addLink(new RESTLink("edit", "http://localhost/bar"));
        link = Links.editOrSelf(dto);
        assertEquals(link.getRel(), "edit");
        assertEquals(link.getHref(), "http://localhost/bar");
    }

    public void testWithRel()
    {
        RESTLink link = new RESTLink("edit", "http://localhost");
        link.setType(VirtualMachineDto.MEDIA_TYPE);
        link.setTitle("foo");

        RESTLink copy = Links.withRel("changed", link);
        assertEquals(copy.getRel(), "changed");
        assertEquals(copy.getHref(), "http://localhost");
        assertEquals(copy.getType(), VirtualMachineDto.MEDIA_TYPE);
        assertEquals(copy.getTitle(), "foo");
    }
}
