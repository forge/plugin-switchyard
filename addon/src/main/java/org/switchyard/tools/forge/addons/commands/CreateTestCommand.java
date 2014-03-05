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

import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.plugin.SwitchYardConfigurator;
import org.switchyard.tools.forge.rules.RulesFacet;

/**
 * Command to create a unit test for a service.
 */
public class CreateTestCommand extends AbstractSwitchYardCommand {
    
    @Inject
    @WithAttributes(label = "Service Name", required = true)
    private UIInput<String> serviceName;
	
    @Inject
    @WithAttributes(label = "Package Name", type = InputType.JAVA_PACKAGE_PICKER)
    private UIInput<String> packageName;
	
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private SwitchYardConfigurator switchYardConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
    	Project project = getSelectedProject(builder.getUIContext());

    	String pkgName = project.getFacet(MetadataFacet.class).getTopLevelPackage();
        packageName.setDefaultValue(pkgName);
    	builder.add(serviceName).add(packageName);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: Create Service Test")
                .description("Create Service Test")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext shellContext) throws Exception {
        Project project = getSelectedProject(shellContext);
        SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
        
        JavaResource result = switchYardConfigurator.createServiceTest(project, serviceName.getValue(),
        		packageName.getValue());
        
        shellContext.getUIContext().getProvider().getOutput().out().println(result);
        return Results.success();
        
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
