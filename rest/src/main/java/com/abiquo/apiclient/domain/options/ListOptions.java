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
import java.util.TreeMap;

public class ListOptions extends BaseOptions
{
    protected ListOptions(final Map<String, Object> queryParams)
    {
        super(queryParams);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder extends BaseOptionsBuilder<Builder>
    {
        @Override
        protected Builder self()
        {
            return this;
        }

        public ListOptions build()
        {
            return new ListOptions(buildParameters());
        }
    }

    public static abstract class BaseOptionsBuilder<T extends BaseOptionsBuilder<T>>
    {
        private String has;

        private String orderBy;

        private Integer limit;

        private Integer start;

        private Boolean asc;

        protected abstract T self();

        public T has(final String has)
        {
            this.has = has;
            return self();
        }

        public T orderBy(final String orderBy)
        {
            this.orderBy = orderBy;
            return self();
        }

        public T limit(final int limit)
        {
            this.limit = limit;
            return self();
        }

        public T start(final int start)
        {
            this.start = start;
            return self();
        }

        public T asc(final boolean asc)
        {
            this.asc = asc;
            return self();
        }

        protected Map<String, Object> buildParameters()
        {
            Map<String, Object> parameters = new TreeMap<>();
            putIfPresent("has", has, parameters);
            putIfPresent("by", orderBy, parameters);
            putIfPresent("limit", limit, parameters);
            putIfPresent("startwith", start, parameters);
            putIfPresent("asc", asc, parameters);
            return parameters;
        }

    }
}
