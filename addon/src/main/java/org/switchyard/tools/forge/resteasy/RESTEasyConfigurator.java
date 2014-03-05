/*
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
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

import javax.inject.Inject;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;

import org.switchyard.component.resteasy.config.model.RESTEasyBindingModel;
import org.switchyard.component.resteasy.config.model.RESTEasyNamespace;
import org.switchyard.component.resteasy.config.model.v1.V1RESTEasyBindingModel;
import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Forge commands related to RESTEasy bindings.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
public class RESTEasyConfigurator {

    
    /**
     * Add a RESTEasy binding to a SwitchYard service.
     * @param serviceName name of the reference to bind
     * @param interfaces a set of interface classes to configure the RESTEasy binding
     * @param contextPath the additional context path where the REST endpoints will be hosted
     * @param out shell output
     */
    public void bindService(Project project,
    		final String serviceName,
            final String interfaces,
            final String contextPath) throws Exception {

        SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
        CompositeServiceModel service = switchYard.getCompositeService(serviceName);
        // Check to see if the service is public
        if (service == null) {
            throw new Exception("No public service named: " + serviceName);
        }

        RESTEasyBindingModel binding = new V1RESTEasyBindingModel(RESTEasyNamespace.DEFAULT.uri());
        binding.setInterfaces(interfaces);
        String projectName = project.getFacet(MetadataFacet.class).getProjectName();
        if ((contextPath != null) && contextPath.length() > 0) {
            binding.setContextPath(contextPath);
        } else {
            binding.setContextPath(projectName);
        }
        service.addBinding(binding);

        switchYard.saveConfig();
    }

    /**
     * Add a RESTEasy binding to a SwitchYard reference.
     * @param referenceName name of the reference to bind
     * @param interfaces a set of interface classes to configure the RESTEasy binding
     * @param address optional value of the root url where the REST endpoints are hosted
     * @param contextPath optional value of additional context path
     * @param out shell output
     */
    public void bindReference(
    		Project project,
            final String referenceName,
            final String interfaces,
            final String address,
            final String contextPath) throws Exception {
        
        SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
        CompositeReferenceModel reference = switchYard.getCompositeReference(referenceName);
        // Check to see if the service is public
        if (reference == null) {
            throw new Exception("No public reference named: " + referenceName);
        }

        RESTEasyBindingModel binding = new V1RESTEasyBindingModel(RESTEasyNamespace.DEFAULT.uri());
        binding.setInterfaces(interfaces);
        if (address != null) {
            binding.setAddress(address);
        }
        String projectName = project.getFacet(MetadataFacet.class).getProjectName();
        if (contextPath != null) {
            binding.setContextPath(contextPath);
        } else if (address == null) {
            binding.setContextPath(projectName);
        }
        reference.addBinding(binding);

        switchYard.saveConfig();
    }
}
