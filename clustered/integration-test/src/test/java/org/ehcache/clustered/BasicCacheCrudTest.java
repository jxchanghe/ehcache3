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

package org.ehcache.clustered;

import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.terracotta.testing.rules.BasicExternalCluster;
import org.terracotta.testing.rules.Cluster;

import java.io.File;
import java.net.URI;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class BasicCacheCrudTest {

  private static final String RESOURCE_CONFIG =
      "<service xmlns:ohr='http://www.terracotta.org/config/offheap-resource' id=\"resources\">"
      + "<ohr:offheap-resources>"
      + "<ohr:resource name=\"primary-server-resource\" unit=\"MB\">64</ohr:resource>"
      + "</ohr:offheap-resources>" +
      "</service>\n";

  @ClassRule
  public static Cluster CLUSTER =
      new BasicExternalCluster(new File("build/cluster"), 1, Collections.<File>emptyList(), "", RESOURCE_CONFIG);

  @BeforeClass
  public static void waitForActive() throws Exception {
    CLUSTER.getClusterControl().waitForActive();
  }

  @Test
  public void basicCacheGetPut() throws Exception {
    final CacheManagerBuilder<PersistentCacheManager> clusteredCacheManagerBuilder
        = CacheManagerBuilder.newCacheManagerBuilder()
        .with(ClusteringServiceConfigurationBuilder.cluster(CLUSTER.getConnectionURI().resolve("/myCacheManager?auto-create"))
            .defaultServerResource("primary-server-resource"));
    final PersistentCacheManager cacheManager = clusteredCacheManagerBuilder.build(false);
    cacheManager.init();

    try {
      CacheConfiguration<Long, String> config = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
          ResourcePoolsBuilder.newResourcePoolsBuilder()
              .with(ClusteredResourcePoolBuilder.fixed("primary-server-resource", 1, MemoryUnit.MB))).build();

      Cache<Long, String> cache = cacheManager.createCache("clustered-cache", config);
      cache.put(1L, "The one");
      cache.put(2L, "The two");
      cache.put(1L, "Another one");
      cache.put(3L, "The three");
      assertThat(cache.get(1L), equalTo("Another one"));
      assertThat(cache.get(2L), equalTo("The two"));
      assertThat(cache.get(3L), equalTo("The three"));
    } finally {
      cacheManager.close();
    }
  }
}
