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
import org.switchyard.tools.forge.plugin.TransformerTypes;
import org.switchyard.tools.forge.plugin.SwitchYardConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.plugin.PolicyPlacementLocations;

/**
 * Command to add a transformer.
 */
public class AddTransformerCommand extends AbstractSwitchYardCommand {

    @Inject
    @WithAttributes(label = "Transformer type", required = true)
    private UISelectOne<TransformerTypes> transformerType;

    @Inject
    @WithAttributes(label = "Transform from (QName)", required = true)
    private UIInput<String> from;
    
    @Inject
    @WithAttributes(label = "Transform to (QName)", required = true)
    private UIInput<String> to;
	

    @Inject
    @WithAttributes(label = "Smooks transformation type")
    private UIInput<String> smooksTransformationType;

    @Inject
    @WithAttributes(label = "Smooks Location")
    private UIInput<String> smooksLocation;
    
    @Inject
    @WithAttributes(label = "Java Transformer Class Name")
    private UIInput<String> javaTransformerClassName;
    
    @Inject
    @WithAttributes(label = "XSLT file Location")
    private UIInput<String> xsltFileLocation;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private SwitchYardConfigurator switchYardConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {

    	transformerType.setItemLabelConverter(new Converter<TransformerTypes, String>()
        {
           @Override
           public String convert(TransformerTypes source)
           {
              return source.getType();
           }
        });
    	
    	List choices = new ArrayList<TransformerTypes>();
    	for (TransformerTypes ppl : TransformerTypes.values()) {
    		choices.add(ppl);
    	}

    	
     	builder.add(from);
    	builder.add(to);
    	
     	builder.add(transformerType);
    	builder.add(smooksTransformationType);
    	
    	builder.add(javaTransformerClassName);
    	builder.add(xsltFileLocation);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: Add Transformer")
                .description("Add a transformer definition.")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext shellContext) throws Exception {
        Project project = getSelectedProject(shellContext);
        SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);

        
        switch (transformerType.getValue()) {
	        case JAVA:
	        	switchYardConfigurator.addJavaTransformer(project, 
	        			from.getValue(), to.getValue(), javaTransformerClassName.getValue()); 
	        	break;
	        case SMOOKS:
	        	switchYardConfigurator.addSmooksTransformer(project, 
	        			from.getValue(), to.getValue(), smooksLocation.getValue(),
	        			smooksTransformationType.getValue());
	        	break;
	        case XSLT:
	        	switchYardConfigurator.addXSLTTransformer(project, 
	        			from.getValue(), to.getValue(), xsltFileLocation.getValue(), true);
	        	break;
	        case JSON:
	        	switchYardConfigurator.addJSONTransformer(project, 
	        			from.getValue(), to.getValue());
	        	break;
	        case JAXB:
	        	switchYardConfigurator.addJAXBTransformer(project, 
	        			from.getValue(), to.getValue());
	        	break;
	        default :
	        	throw new Exception("Transformer Type not recognized.");
        }
           
        
        shellContext.getUIContext().getProvider().getOutput().out().println("");
        return Results.success();
        
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }
}
