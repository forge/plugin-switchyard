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
import org.switchyard.config.model.composite.BindingModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.policy.Policy;
import org.switchyard.policy.PolicyFactory;
import org.switchyard.tools.forge.rules.RulesFacet;
import org.switchyard.tools.forge.plugin.OperationSelectorTypes;
import org.switchyard.tools.forge.plugin.SwitchYardConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.plugin.PolicyPlacementLocations;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Command to add an operation selector on a binding.
 */
public class AddOperationSelectorCommand extends AbstractSwitchYardCommand {
	
    @Inject
    @WithAttributes(label = "Service Name", required = true)
    private UIInput<String> serviceName;
    
    @Inject
    @WithAttributes(label = "Operation Selector Type", required = true)
    private UISelectOne<OperationSelectorTypes> type;
    
    @Inject
    @WithAttributes(label = "Binding Name")
    private UIInput<String> bindingName;
	
    @Inject
    @WithAttributes(label = "Operation name", required = true)
    private UIInput<String> operationName;

    @Inject
    @WithAttributes(label = "XPath expression")
    private UIInput<String> xpathExpression;

    @Inject
    @WithAttributes(label = "Regular expression")
    private UIInput<String> regularExpression;
    
    @Inject
    @WithAttributes(label = "Class name")
    private UIInput<String> className;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private SwitchYardConfigurator switchYardConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
    		
        type.setItemLabelConverter(new Converter<OperationSelectorTypes, String>()
        {
           @Override
           public String convert(OperationSelectorTypes source)
           {
              return source.getType();
           }
        });
    	
     	builder.add(serviceName);
    	List choices = new ArrayList<OperationSelectorTypes>();
    	for (OperationSelectorTypes ppl : OperationSelectorTypes.values()) {
    		choices.add(ppl);
    	}
     	
    	builder.add(type);
    	builder.add(bindingName);

    	builder.add(operationName);
    	builder.add(xpathExpression);
    	
    	builder.add(regularExpression);
    	builder.add(className);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: Add Operation Selector")
                .description("Add a operation selector to a service binding.")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext shellContext) throws Exception {
        Project project = getSelectedProject(shellContext);
        SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
    
        CompositeServiceModel service = null;
        for (CompositeServiceModel s : facet.getSwitchYardConfig().getComposite().getServices()) {
            if (s.getName().equals(serviceName.getValue())) {
                service = s;
            }
        }
        if (service == null) {
            throw new Exception("Service " + serviceName.getValue() + " could not be found");
        }

        List<BindingModel> bindingList = service.getBindings(); 
        if (bindingList.size() == 0) {
            throw new Exception("There is no binding which supports OperationSelector");
        }
        List<String> bindingDescList = new ArrayList<String>();
    	BindingModel binding = null;
        for (BindingModel b : bindingList) {
        	if (b.getName() != null) {
        		if (b.getName().equals(bindingName.getValue())) {
        			binding = b; 
        		}
        	}
        }

        if (binding == null) {
        	StringBuilder builder = new StringBuilder();
            for (BindingModel b : bindingList) {
            	builder.append(b.getName() + " ");
            }

        	throw new Exception("No binding " + bindingName.getValue() + " found.  "
        			+ "Available bindings : " + builder.toString());
        }
            	
        switch (type.getValue()) {
	        case STATIC:
	        	switchYardConfigurator.addStaticOperationSelector(project, 
	        			serviceName.getValue(), binding, operationName.getValue()); 
	        	break;
	        case XPATH:
	        	switchYardConfigurator.addXPathOperationSelector(project, 
	        			serviceName.getValue(), binding, xpathExpression.getValue());
	        	break;
	        case REGEX:
	        	switchYardConfigurator.addStaticOperationSelector(project, 
	        			serviceName.getValue(), binding, regularExpression.getValue());
	        	break;
	        case JAVA:
	        	switchYardConfigurator.addStaticOperationSelector(project, 
	        			serviceName.getValue(), binding, className.getValue());
	        	break;
	        default :
	        	throw new Exception("Operation Type not recognized.");
        }
               
        shellContext.getUIContext().getProvider().getOutput().out().println("Operation Selector added to "
        		+ serviceName.getValue() + ".");
        
        return Results.success();
        
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }
}
