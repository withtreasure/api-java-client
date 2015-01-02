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

public class EnterpriseListOptions extends BaseOptions
{
    protected EnterpriseListOptions(final Map<String, Object> queryParams)
    {
        super(queryParams);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder extends BaseOptionsBuilder<Builder>
    {
        private Integer idPricingTemplate;

        private Integer idScope;

        private Boolean included;

        public Builder idPricingTemplate(final int idPricingTemplate)
        {
            this.idPricingTemplate = idPricingTemplate;
            return self();
        }

        public Builder idScope(final int idScope)
        {
            this.idScope = idScope;
            return self();
        }

        public Builder included(final boolean included)
        {
            this.included = included;
            return self();
        }

        @Override
        protected Map<String, Object> buildParameters()
        {
            Map<String, Object> params = super.buildParameters();
            putIfPresent("idPricingTemplate", idPricingTemplate, params);
            putIfPresent("idScope", idScope, params);
            putIfPresent("included", included, params);
            return params;
        }

        public EnterpriseListOptions build()
        {
            return new EnterpriseListOptions(buildParameters());
        }

        @Override
        protected Builder self()
        {
            return this;
        }
    }

}
