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
package com.abiquo.apiclient.json;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.reflect.TypeToken;

public class Json
{
    private final ObjectMapper mapper;

    public Json()
    {
        mapper = new ObjectMapper();
        mapper.setVisibilityChecker(mapper.getVisibilityChecker().withFieldVisibility(
            Visibility.ANY));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public <T> T read(final String str, final Class<T> clazz) throws IOException
    {
        return mapper.readValue(str, mapper.constructType(clazz));
    }

    public <T> T read(final String str, final TypeToken<T> type) throws IOException
    {
        return mapper.readValue(str, mapper.constructType(type.getType()));
    }

    public String write(final Object object) throws IOException
    {
        return mapper.writeValueAsString(object);
    }
}
