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
import java.io.InputStream;
import java.io.Reader;

import com.google.common.reflect.TypeToken;

public interface Json
{
    /**
     * Read a json object from an {@link InputStream}.
     */
    public <T> T read(Reader reader, Class<T> type) throws IOException;

    /**
     * Read a json object from an {@link InputStream}.
     */
    public <T> T read(Reader reader, TypeToken<T> type) throws IOException;

    /**
     * Write the given object to json.
     */
    public String write(Object object) throws IOException;
}
