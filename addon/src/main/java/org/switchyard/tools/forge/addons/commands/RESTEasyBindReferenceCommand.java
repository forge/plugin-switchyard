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

import java.util.Arrays;

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
 * Command for binding RESTEasy references.
 */
public class RESTEasyBindReferenceCommand extends AbstractSwitchYardCommand {

    @Inject
    @WithAttributes(label = "Reference Name", required = true)
    private UIInput<String> referenceName;
    
    @Inject
    @WithAttributes(label = "Interfaces", required = true)
    private UIInput<String> interfaces;

    @Inject
    @WithAttributes(label = "Address", required = true)
    private UIInput<String> address;
    
    @Inject
    @WithAttributes(label = "Context Path", required = true)
    private UIInput<String> contextPath;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private RESTEasyConfigurator restEasyConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {

        builder.add(referenceName);
        builder.add(interfaces);
        builder.add(address);
        builder.add(contextPath);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard : RESTEasy Bind Reference")
                .description("Bind a service with a RESTEasy reference")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        RESTEasyFacet facet = facetFactory.install(project, RESTEasyFacet.class);
        
        restEasyConfigurator.bindReference(project, referenceName.getValue(), 
        		interfaces.getValue(), address.getValue(), contextPath.getValue());
        if (facet != null)
        {
           return Results.success("RESTEasy Reference has been installed.");
        }
        return Results.fail("Could not install RESTEasy Reference.");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
