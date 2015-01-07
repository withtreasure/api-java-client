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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;

import com.abiquo.apiclient.ApiClient;
import com.abiquo.apiclient.RestClient;
import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.model.transport.WrapperDto;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

/**
 * An {@link Iterator} that is capable of advancing between the pages of a paginated collection.
 * <p>
 * To create this iterator use the {@link #flatten(ApiClient, WrapperDto)} method.
 * 
 * @author Ignasi Barrera
 */
public class PageIterator<T extends WrapperDto< ? extends SingleResourceTransportDto>> extends
    AbstractIterator<T>
{
    private final RestClient api;

    private T currentPage;

    private boolean unread;

    /* For internal use only. Use the factory methods. */
    private PageIterator(final RestClient api, final T initialPage)
    {
        this.api = checkNotNull(api, "api cannot be null");
        this.currentPage = checkNotNull(initialPage, "initialPage cannot be null");
        // First iteration has to return the initial page without fetching a new one
        this.unread = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T computeNext()
    {
        if (unread)
        {
            try
            {
                return currentPage;
            }
            finally
            {
                // Set this in a finally block to only set it after the value has been returned
                unread = false;
            }
        }
        else
        {
            RESTLink next = currentPage.searchLink("next");
            if (next == null)
            {
                return endOfData();
            }
            else
            {
                currentPage =
                    api.get(next.getHref(), currentPage.getMediaType(),
                        (Class<T>) currentPage.getClass());
                return currentPage;
            }
        }
    }

    /**
     * Creates an iterator capable of advancing over the elements of a paginated collection, and
     * lazily fetch new pages as they are needed.
     * 
     * @param api The rest client used to fetch new pages when needed.
     * @param dto The collection to iterate.
     * @return An iterator capable of advancing between pages.
     */
    public static <T extends SingleResourceTransportDto, W extends WrapperDto<T>> Iterable<T> flatten(
        final RestClient api, final W dto)
    {
        return new AdvancingIterable<T, W>(api, dto);
    }

    /**
     * An {@link Iterable} that is capable of advancing between pages.
     * 
     * @author Ignasi Barrera
     */
    public static class AdvancingIterable<T extends SingleResourceTransportDto, W extends WrapperDto<T>>
        implements Iterable<T>
    {
        private final RestClient api;

        private final W initialPage;

        // For internal use only.
        private AdvancingIterable(final RestClient api, final W initialPage)
        {
            this.api = checkNotNull(api, "api cannot be null");
            this.initialPage = checkNotNull(initialPage, "initialPage cannot be null");
        }

        public int size()
        {
            return initialPage.getTotalSize();
        }

        @Override
        public Iterator<T> iterator()
        {
            final PageIterator<W> pageIterator = new PageIterator<W>(api, initialPage);
            return Iterators.concat(new AbstractIterator<Iterator<T>>()
            {
                @Override
                protected Iterator<T> computeNext()
                {
                    return pageIterator.hasNext() ? pageIterator.next().getCollection().iterator()
                        : endOfData();
                }
            });
        }

    }
}
