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

public class VirtualDatacenterListOptions extends BaseOptions
{
    protected VirtualDatacenterListOptions(final Map<String, Object> queryParams)
    {
        super(queryParams);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder extends BaseOptionsBuilder<Builder>
    {
        private Integer datacenterId;

        private Integer enterpriseId;

        public Builder datacenterId(final int datacenterId)
        {
            this.datacenterId = datacenterId;
            return self();
        }

        public Builder enterpriseId(final int enterpriseId)
        {
            this.enterpriseId = enterpriseId;
            return self();
        }

        @Override
        protected Map<String, Object> buildParameters()
        {
            Map<String, Object> params = super.buildParameters();
            putIfPresent("datacenter", datacenterId, params);
            putIfPresent("enterprise", enterpriseId, params);
            return params;
        }

        public VirtualDatacenterListOptions build()
        {
            return new VirtualDatacenterListOptions(buildParameters());
        }

        @Override
        protected Builder self()
        {
            return this;
        }
    }

}
