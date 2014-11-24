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
package com.abiquo.apiclient.api;

import com.abiquo.server.core.appslibrary.VirtualMachineTemplateDto;
import com.abiquo.server.core.cloud.VirtualApplianceDto;
import com.abiquo.server.core.cloud.VirtualDatacenterDto;
import com.abiquo.server.core.cloud.VirtualMachineDto;
import com.abiquo.server.core.enterprise.DatacenterLimitsDto;
import com.abiquo.server.core.enterprise.EnterpriseDto;
import com.abiquo.server.core.enterprise.UserDto;
import com.abiquo.server.core.infrastructure.DatacenterDto;
import com.abiquo.server.core.infrastructure.RackDto;
import com.abiquo.server.core.infrastructure.network.NetworkServiceTypeDto;
import com.abiquo.server.core.infrastructure.network.VLANNetworkDto;
import com.abiquo.server.core.infrastructure.storage.StorageDeviceDto;
import com.abiquo.server.core.infrastructure.storage.StoragePoolDto;
import com.abiquo.server.core.infrastructure.storage.TierDto;
import com.google.common.base.Predicate;

public class ApiPredicates
{
    public static Predicate<DatacenterDto> datacenterName(final String name)
    {
        return new Predicate<DatacenterDto>()
        {
            @Override
            public boolean apply(final DatacenterDto input)
            {
                return input.getName().equals(name);
            }
        };
    }

    public static Predicate<RackDto> rackName(final String name)
    {
        return new Predicate<RackDto>()
        {
            @Override
            public boolean apply(final RackDto input)
            {
                return input.getName().equals(name);
            }
        };
    }

    public static Predicate<VirtualDatacenterDto> virtualDatacenterName(final String name)
    {
        return new Predicate<VirtualDatacenterDto>()
        {
            @Override
            public boolean apply(final VirtualDatacenterDto input)
            {
                return input.getName().equals(name);
            }
        };
    }

    public static Predicate<EnterpriseDto> enterpriseName(final String name)
    {
        return new Predicate<EnterpriseDto>()
        {
            @Override
            public boolean apply(final EnterpriseDto input)
            {
                return input.getName().equals(name);
            }
        };
    }

    public static Predicate<VirtualMachineTemplateDto> templateName(final String name)
    {
        return new Predicate<VirtualMachineTemplateDto>()
        {
            @Override
            public boolean apply(final VirtualMachineTemplateDto input)
            {
                return input.getName().equals(name);
            }
        };
    }

    public static Predicate<VirtualApplianceDto> virtualApplianceName(final String name)
    {
        return new Predicate<VirtualApplianceDto>()
        {
            @Override
            public boolean apply(final VirtualApplianceDto input)
            {
                return input.getName().equals(name);
            }
        };
    }

    public static Predicate<NetworkServiceTypeDto> defaultNetworkServiceType()
    {
        return new Predicate<NetworkServiceTypeDto>()
        {
            @Override
            public boolean apply(final NetworkServiceTypeDto netServiceType)
            {
                return netServiceType.isDefaultNST();
            }
        };
    }

    public static Predicate<StorageDeviceDto> storageDeviceName(final String name)
    {
        return new Predicate<StorageDeviceDto>()
        {
            @Override
            public boolean apply(final StorageDeviceDto input)
            {
                return input.getName().equals(name);
            }
        };
    }

    public static Predicate<StoragePoolDto> storagePoolName(final String name)
    {
        return new Predicate<StoragePoolDto>()
        {
            @Override
            public boolean apply(final StoragePoolDto input)
            {
                return input.getName().equals(name);
            }
        };
    }

    public static Predicate<TierDto> tierName(final String name)
    {
        return new Predicate<TierDto>()
        {
            @Override
            public boolean apply(final TierDto input)
            {
                return input.getName().equals(name);
            }
        };
    }

    public static Predicate<VirtualMachineDto> withTemplate(final String name)
    {
        return new Predicate<VirtualMachineDto>()
        {
            @Override
            public boolean apply(final VirtualMachineDto input)
            {
                return input.searchLink("virtualmachinetemplate").getTitle().equals(name);
            }
        };
    }

    public static Predicate<VLANNetworkDto> networkName(final String name)
    {
        return new Predicate<VLANNetworkDto>()
        {
            @Override
            public boolean apply(final VLANNetworkDto input)
            {
                return input.getName().equals(name);
            }
        };
    }

    public static Predicate<DatacenterLimitsDto> locationName(final String name)
    {
        return new Predicate<DatacenterLimitsDto>()
        {
            @Override
            public boolean apply(final DatacenterLimitsDto input)
            {
                return input.searchLink("location").getTitle().equals(name);
            }
        };
    }

    public static Predicate<UserDto> userName(final String name)
    {
        return new Predicate<UserDto>()
        {
            @Override
            public boolean apply(final UserDto input)
            {
                return input.getNick().equals(name);
            }
        };
    }
}
