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

import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.SingleResourceTransportDto;

public final class Links
{
    public static RESTLink create(final String rel, final String href, final String type)
    {
        RESTLink link = new RESTLink(rel, href);
        link.setType(type);
        return link;
    }

    public static RESTLink withRel(final String newRel, final RESTLink source)
    {
        RESTLink link = create(newRel, source.getHref(), source.getType());
        link.setTitle(source.getTitle());
        return link;
    }

    public static RESTLink editOrSelf(final SingleResourceTransportDto dto)
    {
        RESTLink link = dto.getEditLink();
        if (link == null)
        {
            link = dto.searchLink("self");
        }
        return link;
    }

    private Links()
    {
        throw new AssertionError("Constant class. Clients shouldn't instantiate it directly.");
    }
}
