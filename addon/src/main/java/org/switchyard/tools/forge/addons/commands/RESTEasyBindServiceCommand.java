package org.switchyard.tools.forge.addons.commands;

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
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.resteasy.RESTEasyFacet;
import org.switchyard.tools.forge.resteasy.RESTEasyConfigurator;

/**
 * Commands for binding RESTEasy Services.
 */
public class RESTEasyBindServiceCommand extends AbstractSwitchYardCommand {

    @Inject
    @WithAttributes(label = "Service Name", required = true)
    private UIInput<String> serviceName;
    
    @Inject
    @WithAttributes(label = "Interfaces", required = true)
    private UIInput<String> interfaces;

    @Inject
    @WithAttributes(label = "Context Path", required = true)
    private UIInput<String> contextPath;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private RESTEasyConfigurator restEasyConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        builder.add(serviceName);
        builder.add(interfaces);
        builder.add(contextPath);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard : Rest Bind Service")
                .description("Bind a Service with RESTEasy")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        RESTEasyFacet facet = facetFactory.install(project, RESTEasyFacet.class);
        
        restEasyConfigurator.bindService(project, serviceName.getValue(), 
        		interfaces.getValue(), contextPath.getValue());
        
        if (facet != null)
        {
           return Results.success("RESTEasy Service has been installed.");
        }
        return Results.fail("Could not install RESTEasy Service.");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
