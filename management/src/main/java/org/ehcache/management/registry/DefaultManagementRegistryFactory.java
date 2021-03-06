/*
 * Copyright Terracotta, Inc.
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
package org.ehcache.management.registry;

import org.ehcache.management.ManagementRegistryService;
import org.ehcache.management.ManagementRegistryServiceConfiguration;
import org.ehcache.spi.service.ServiceCreationConfiguration;
import org.ehcache.core.spi.service.ServiceFactory;

/**
 * @author Ludovic Orban
 */
public class DefaultManagementRegistryFactory implements ServiceFactory<ManagementRegistryService> {

  @Override
  public ManagementRegistryService create(ServiceCreationConfiguration<ManagementRegistryService> configuration) {
    if(configuration == null) {
      configuration = new DefaultManagementRegistryConfiguration();
    }
    return new DefaultManagementRegistryService((ManagementRegistryServiceConfiguration) configuration);
  }

  @Override
  public Class<ManagementRegistryService> getServiceType() {
    return ManagementRegistryService.class;
  }

}
