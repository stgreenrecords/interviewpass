/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.core.impl.models;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Model(adaptables = {Resource.class, SlingHttpServletRequest.class})
public class NavigationModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationModel.class);

    private static final String NAVIGATION_ROOT = "/content/home";
    private static final String NAVIGATION_SIGN_IN = "/content/home/signin";

    @SlingObject
    private ResourceResolver resourceResolver;

    public NavigationModel() {
        LOGGER.trace("Model Instance created");
    }

    public List<NavigationItemModel> getNavigationItems() {
        return Optional.ofNullable(resourceResolver)
                .map(resolver -> resolver.getResource(NAVIGATION_ROOT))
                .map(resource -> IteratorUtils.toList(resource.listChildren())).stream().flatMap(Collection::stream)
                .map(resource -> resource.adaptTo(NavigationItemModel.class))
                .filter(item -> StringUtils.isEmpty(Objects.requireNonNull(item).getHideFromNavigation()))
                .collect(Collectors.toList());

    }

    public String getRootPage(){
        return Optional.ofNullable(resourceResolver.getResource(NAVIGATION_ROOT))
                .map(Resource::getPath)
                .orElse(NAVIGATION_ROOT);
    }

    public String getSignIn(){
        return Optional.ofNullable(resourceResolver.getResource(NAVIGATION_SIGN_IN))
                .map(Resource::getPath)
                .orElse(NAVIGATION_SIGN_IN);
    }
}