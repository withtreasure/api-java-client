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

import static org.testng.Assert.assertFalse;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

@Test
public class ApiPathTest
{
    public void testAllPathsEndWithSlash() throws IllegalArgumentException, IllegalAccessException
    {
        Field[] fields = ApiPath.class.getDeclaredFields();
        for (Field field : fields)
        {
            if (isPublicConstant(field) && field.getType().equals(String.class)
                && field.getName().endsWith("_URL"))
            {
                String value = (String) field.get(null);
                assertFalse(value.endsWith("/"));
            }
        }
    }

    private static boolean isPublicConstant(final Field field)
    {
        int modifiers = field.getModifiers();
        return modifiers == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
    }
}
