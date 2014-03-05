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
import java.util.List;
import java.util.ArrayList;

import javax.inject.Inject;

import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.policy.Policy;
import org.switchyard.policy.PolicyFactory;
import org.switchyard.tools.forge.rules.RulesFacet;
import org.switchyard.tools.forge.plugin.SwitchYardConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.plugin.PolicyPlacementLocations;

/**
 * Command to add a required policy on component service/reference.
 */
public class AddPolicyCommand extends AbstractSwitchYardCommand {
	
    @Inject
    @WithAttributes(label = "Component Name", required = true)
    private UIInput<String> componentName;
    
    @Inject
    @WithAttributes(label = "Policy")
    private UIInput<String> policy;
	
    @Inject
    @WithAttributes(label = "Where", required = true)
    private UISelectOne<PolicyPlacementLocations> where;

    @Inject
    @WithAttributes(label = "Component Reference Model")
    private UISelectOne<ComponentReferenceModel> componentReferenceModel;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private SwitchYardConfigurator switchYardConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
    		
        where.setItemLabelConverter(new Converter<PolicyPlacementLocations, String>()
        {
           @Override
           public String convert(PolicyPlacementLocations source)
           {
              return source.getLocation();
           }
        });
    	
     	builder.add(componentName);
    	builder.add(policy);
    	
    	List choices = new ArrayList<PolicyPlacementLocations>();
    	for (PolicyPlacementLocations ppl : PolicyPlacementLocations.values()) {
    		choices.add(ppl);
    	}
    	
    	builder.add(where);
    	builder.add(componentReferenceModel);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: Add Policy")
                .description("Add a required policy on component service/reference")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext shellContext) throws Exception {
        Project project = getSelectedProject(shellContext);
        SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);

        String whereString = where.getValue().toString();

        Policy validPolicy = null;
        for (Policy p : PolicyFactory.getAvailableInteractionPolicies()) {
        	if (p.getName().equals(policy.getValue())) {
        		validPolicy = p;
        		break;
        	}
        }
        
        if (policy == null) {
        	StringBuilder builder = new StringBuilder();
            for (Policy p : PolicyFactory.getAvailableInteractionPolicies()) {
            	builder.append(p.getName() + " ");
            }
        	throw new Exception("Cannot find policy [" + policy.getValue() + "]. "
        			+ "Valid policies are [" + builder.toString() + "]"); 
        }
        
        switchYardConfigurator.addPolicy(project, componentName.getValue(),
        		validPolicy, whereString, componentReferenceModel.getValue());	
        
        shellContext.getUIContext().getProvider().getOutput().out().println("Policy "
        		+ policy.getValue() + " added.");
        return Results.success();
        
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }
}
