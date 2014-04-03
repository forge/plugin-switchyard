/*
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.switchyard.tools.forge.resteasy;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;

import org.switchyard.tools.forge.plugin.AbstractSwitchyardFacet;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Forge facet for RESTEasy binding functionality.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
@FacetConstraint({ DependencyFacet.class, PackagingFacet.class, SwitchYardFacet.class })
public class RESTEasyFacet extends AbstractSwitchyardFacet {

    private static final String REST_MAVEN_ID =
        "org.switchyard.components:switchyard-component-resteasy";

    @Override
    public boolean install() {
        installDependencies();
        return true;
    }

    /**
     * Create a new SOAP Facet.
     */
    public RESTEasyFacet() {
        super(REST_MAVEN_ID);
    }
}
