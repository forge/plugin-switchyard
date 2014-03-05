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
package org.switchyard.tools.forge.addons.commands;


import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.switchyard.tools.forge.camel.CamelBindingConfigurator;
import org.switchyard.tools.forge.camel.CamelFacet;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Command for binding a service using a Camel endpoint URI.
 */
public class CamelBindServiceCommand extends AbstractSwitchYardCommand  {

    @Inject
    @WithAttributes(label = "Service Name", required = true)
    private UIInput<String> serviceName;
    
    @Inject
    @WithAttributes(label = "configURI", required = true)
    private UIInput<String> configURI;

    @Inject
    @WithAttributes(label = "operationName", required = true)
    private UIInput<String> operationName;

    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private CamelBindingConfigurator camelBindingConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder.getUIContext());
        if (project.hasFacet(SwitchYardFacet.class))
        {
           SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
        }
        builder.add(serviceName);
       	builder.add(configURI);
        builder.add(operationName);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: Camel Bind Service")
                .description("Bind a Service with Camel")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        CamelFacet facet = facetFactory.install(project, CamelFacet.class);
        
        camelBindingConfigurator.bindService(project, serviceName.getValue(), 
        		configURI.getValue(), operationName.getValue());
        if (facet != null)
        {
           return Results.success("Camel Service Binding has been installed.");
        }
        return Results.fail("Could not install Camel Service Binding Service.");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }    

}
