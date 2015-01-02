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
package com.abiquo.apiclient.domain.options;

import java.util.Map;

import com.abiquo.apiclient.domain.options.ListOptions.BaseOptionsBuilder;
import com.abiquo.model.enumerator.StatefulInclusion;

public class TemplateListOptions extends BaseOptions
{
    protected TemplateListOptions(final Map<String, Object> queryParams)
    {
        super(queryParams);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder extends BaseOptionsBuilder<Builder>
    {
        private String category;

        private String templateId;

        private StatefulInclusion persistentInclusion;

        private Boolean imported;

        private String osType;

        private Boolean is64bit;

        private String description;

        private String name;

        private String path;

        private String ovfId;

        private String creationUser;

        private Boolean shared;

        public Builder category(final String category)
        {
            this.category = category;
            return self();
        }

        public Builder templateId(final String templateId)
        {
            this.templateId = templateId;
            return self();
        }

        public Builder persistentInclusion(final StatefulInclusion persistentInclusion)
        {
            this.persistentInclusion = persistentInclusion;
            return self();
        }

        public Builder imported(final boolean imported)
        {
            this.imported = imported;
            return self();
        }

        public Builder osType(final String osType)
        {
            this.osType = osType;
            return self();
        }

        public Builder is64bit(final boolean is64bit)
        {
            this.is64bit = is64bit;
            return self();
        }

        public Builder description(final String description)
        {
            this.description = description;
            return self();
        }

        public Builder name(final String name)
        {
            this.name = name;
            return self();
        }

        public Builder path(final String path)
        {
            this.path = path;
            return self();
        }

        public Builder ovfId(final String ovfId)
        {
            this.ovfId = ovfId;
            return self();
        }

        public Builder creationUser(final String creationUser)
        {
            this.creationUser = creationUser;
            return self();
        }

        public Builder shared(final boolean shared)
        {
            this.shared = shared;
            return self();
        }

        @Override
        protected Map<String, Object> buildParameters()
        {
            Map<String, Object> params = super.buildParameters();
            putIfPresent("categoryName", category, params);
            putIfPresent("idTemplate", templateId, params);
            putIfPresent("stateful", persistentInclusion, params);
            putIfPresent("imported", imported, params);
            putIfPresent("ostype", osType, params);
            putIfPresent("64bits", is64bit, params);
            putIfPresent("description", description, params);
            putIfPresent("name", name, params);
            putIfPresent("path", path, params);
            putIfPresent("ovfId", ovfId, params);
            putIfPresent("creationUser", creationUser, params);
            putIfPresent("shared", shared, params);
            return params;
        }

        public TemplateListOptions build()
        {
            return new TemplateListOptions(buildParameters());
        }

        @Override
        protected Builder self()
        {
            return this;
        }
    }

}
