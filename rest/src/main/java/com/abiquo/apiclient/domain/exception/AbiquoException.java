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
package com.abiquo.apiclient.domain.exception;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.find;

import com.abiquo.model.transport.error.ErrorDto;
import com.abiquo.model.transport.error.ErrorsDto;
import com.google.common.base.Predicate;

public class AbiquoException extends HttpException
{
    private static final long serialVersionUID = 3417635717730170489L;

    private final ErrorsDto errors;

    public AbiquoException(final int code, final ErrorsDto errors)
    {
        super(code, errors.toString());
        this.errors = errors;
    }

    public ErrorsDto getErrors()
    {
        return errors;
    }

    public boolean hasError(final String code)
    {
        return any(errors.getCollection(), sameCode(code));
    }

    public ErrorDto getError(final String code)
    {
        return find(errors.getCollection(), sameCode(code));
    }

    public ErrorDto firstError()
    {
        return errors.getCollection().get(0);
    }

    private static Predicate<ErrorDto> sameCode(final String code)
    {
        return new Predicate<ErrorDto>()
        {
            @Override
            public boolean apply(final ErrorDto input)
            {
                return input.getCode().equals(code);
            }
        };
    }
}
