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

import java.util.Arrays;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.switchyard.tools.forge.plugin.SwitchYardConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.rules.RulesFacet;

/**
 * Command class that adds the trace message property or enables/disables it.
 */
public class TraceMessagesCommand extends AbstractSwitchYardCommand {
	
    /**
     * Flag whether to enable / disable the trace message property.
     */
    @Inject
    @WithAttributes(label = "Enable")
    private UIInput<Boolean> enable;
    
    /**
     * Facet factory. 
     */
    @Inject
    private FacetFactory facetFactory;
    
    /**
     * SwitchYard configurator to call traceMessages. 
     */
    @Inject 
    private SwitchYardConfigurator switchYardConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
    	builder.add(enable);    	
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: Trace Messages")
                .description("Set the message trace flag")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }
    
    @Override
    public Result execute(UIExecutionContext shellContext) throws Exception {
        Project project = getSelectedProject(shellContext);
        SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);

        String result = switchYardConfigurator.traceMessages(project, enable.getValue());
        	
        shellContext.getUIContext().getProvider().getOutput().out().println(result); 
        return Results.success();
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
