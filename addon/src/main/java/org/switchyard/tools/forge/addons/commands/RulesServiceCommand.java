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
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.rules.RulesFacet;
import org.switchyard.tools.forge.rules.RulesServiceConfigurator;

/**
 * Creates a new Rules service.
 */
public class RulesServiceCommand extends AbstractSwitchYardCommand  {

    @Inject
    @WithAttributes(label = "Service Name", required = true)
    private UIInput<String> serviceName;
    
    @Inject
    @WithAttributes(label = "Package Name", type = InputType.JAVA_PACKAGE_PICKER)
    private UIInput<String> packageName;
    
    @Inject
    @WithAttributes(label = "Interface Class", type = InputType.JAVA_CLASS_PICKER)
    private UIInput<String> interfaceClass;
    

    @Inject
    @WithAttributes(label = "Rule File Path", required = true, type = InputType.FILE_PICKER)
    private UIInput<String> ruleFilePath;
    
    @Inject
    @WithAttributes(label = "Agent", required = true)
    private UIInput<Boolean> isAgent;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private RulesServiceConfigurator rulesServiceConfigurator;
    
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder.getUIContext());
        if (project.hasFacet(SwitchYardFacet.class))
        {
           SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
        }
        builder.add(serviceName);
        builder.add(packageName);
        builder.add(interfaceClass);
        builder.add(ruleFilePath);
        builder.add(isAgent);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: Rules Service Create")
                .description("Create a new Rules Service")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        RulesFacet facet = facetFactory.install(project, RulesFacet.class);
        
        rulesServiceConfigurator.newRules(project, serviceName.getValue(), 
        		packageName.getValue(), interfaceClass.getValue(), ruleFilePath.getValue(), 
        		isAgent.getValue());
        if (facet != null)
        {
           return Results.success("Rules Service has been installed.");
        }
        return Results.fail("Could not install Rules Service.");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
