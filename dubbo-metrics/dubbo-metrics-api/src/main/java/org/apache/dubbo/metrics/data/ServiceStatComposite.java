/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dubbo.metrics.data;

import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.metrics.model.MetricsCategory;
import org.apache.dubbo.metrics.model.ServiceKeyMetric;
import org.apache.dubbo.metrics.model.key.MetricsKeyWrapper;
import org.apache.dubbo.metrics.model.sample.GaugeMetricSample;
import org.apache.dubbo.metrics.model.sample.MetricSample;
import org.apache.dubbo.metrics.report.MetricsExport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ServiceStatComposite implements MetricsExport {

    private final Map<MetricsKeyWrapper, Map<ServiceKeyMetric, AtomicLong>> serviceWrapperNumStats = new ConcurrentHashMap<>();

    public void initWrapper(List<MetricsKeyWrapper> metricsKeyWrappers) {
        if (CollectionUtils.isEmpty(metricsKeyWrappers)) {
            return;
        }
        metricsKeyWrappers.forEach(appKey -> serviceWrapperNumStats.put(appKey, new ConcurrentHashMap<>()));
    }

    public void incrementServiceKey(MetricsKeyWrapper wrapper, String applicationName, String serviceKey, int size) {
        if (!serviceWrapperNumStats.containsKey(wrapper)) {
            return;
        }
        serviceWrapperNumStats.get(wrapper).computeIfAbsent(new ServiceKeyMetric(applicationName, serviceKey), k -> new AtomicLong(0L)).getAndAdd(size);
    }

    public void setServiceKey(MetricsKeyWrapper wrapper, String applicationName, String serviceKey, int num) {
        if (!serviceWrapperNumStats.containsKey(wrapper)) {
            return;
        }
        serviceWrapperNumStats.get(wrapper).computeIfAbsent(new ServiceKeyMetric(applicationName, serviceKey), k -> new AtomicLong(0L)).set(num);
    }

    public List<MetricSample> export(MetricsCategory category) {
        List<MetricSample> list = new ArrayList<>();
        for (MetricsKeyWrapper wrapper : serviceWrapperNumStats.keySet()) {
            Map<ServiceKeyMetric, AtomicLong> stringAtomicLongMap = serviceWrapperNumStats.get(wrapper);
            for (ServiceKeyMetric serviceKeyMetric : stringAtomicLongMap.keySet()) {
                list.add(new GaugeMetricSample<>(wrapper, serviceKeyMetric.getTags(), category, stringAtomicLongMap, value -> value.get(serviceKeyMetric).get()));
            }
        }
        return list;
    }

}
