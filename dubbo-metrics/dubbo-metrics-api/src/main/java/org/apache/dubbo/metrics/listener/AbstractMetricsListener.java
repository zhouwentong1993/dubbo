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

package org.apache.dubbo.metrics.listener;

import org.apache.dubbo.common.utils.ReflectionUtils;
import org.apache.dubbo.metrics.event.MetricsEvent;

import java.util.List;

public abstract class AbstractMetricsListener<E extends MetricsEvent> implements MetricsListener<E> {

    /**
     * Whether to support the general determination of event points depends on the event type
     */
    public boolean isSupport(MetricsEvent event) {
        List<Class<?>> eventTypes = ReflectionUtils.getClassGenerics(getClass(), AbstractMetricsListener.class);
        return event.isAvailable() && eventTypes.stream().allMatch(clazz -> clazz.isInstance(event));
    }

    @Override
    public abstract void onEvent(E event);
}
