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
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.policy.Policy;
import org.switchyard.policy.PolicyFactory;
import org.switchyard.tools.forge.rules.RulesFacet;
import org.switchyard.tools.forge.plugin.SwitchYardConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.plugin.PolicyPlacementLocations;
import org.switchyard.tools.forge.plugin.ValidatorTypes;
import org.switchyard.validate.config.model.XmlSchemaType;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Command to add a message validator. 
 */
public class AddValidatorCommand extends AbstractSwitchYardCommand {
	
    @Inject
    @WithAttributes(label = "Validator Type", required = true)
    private UISelectOne<ValidatorTypes> type;
    
    @Inject
    @WithAttributes(label = "Type (QName) to be validated", required = true)
    private UIInput<String> qname;
    
    @Inject
    @WithAttributes(label = "Class Name")
    private UIInput<String> classname;
	
    @Inject
    @WithAttributes(label = "Schema Type")
    private UISelectOne<XmlSchemaType> schemaType;
    
    @Inject
    @WithAttributes(label = "Schema Catalog File Location")
    private UIInput<String> schemaCatalogFileLocation;
    
    @Inject
    @WithAttributes(label = "Schema File Location")
    private UIInput<String> schemaFileLocation;

    @Inject
    @WithAttributes(label = "Namespace Aware")
    private UIInput<Boolean> namespaceAware;

    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private SwitchYardConfigurator switchYardConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
    	
        type.setItemLabelConverter(new Converter<ValidatorTypes, String>()
        {
           @Override
           public String convert(ValidatorTypes source)
           {
              return source.getType();
           }
        });
    	
    	List choices = new ArrayList<ValidatorTypes>();
    	for (ValidatorTypes ppl : ValidatorTypes.values()) {
    		choices.add(ppl);
    	}
    	
        schemaType.setItemLabelConverter(new Converter<XmlSchemaType, String>()
        {
           @Override
           public String convert(XmlSchemaType source)
           {
              return source.toString();
           }
        });
    	
    	List schemaTypeChoices = new ArrayList<XmlSchemaType>();
    	for (XmlSchemaType ppl : XmlSchemaType.values()) {
    		choices.add(ppl);
    	}
    	
    	builder.add(type);
    	builder.add(qname);
    	builder.add(classname);
    	builder.add(schemaType);
    	builder.add(schemaCatalogFileLocation);
    	builder.add(schemaFileLocation);
    	builder.add(namespaceAware);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: Add Validator")
                .description("Add a message validator definition.")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext shellContext) throws Exception {
        Project project = getSelectedProject(shellContext);
        SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
                
        switch (type.getValue()) {
	        case JAVA:
	        	switchYardConfigurator.addJavaValidator(project, 
	        			qname.getValue(), classname.getValue()); 
	        	break;
	        case XML:
	        	switchYardConfigurator.addXMLValidator(project, 
	        			qname.getValue(), schemaType.getValue(),
	        			schemaCatalogFileLocation.getValue(), schemaFileLocation.getValue(),
	        			namespaceAware.getValue(), true);
	        	break;
	        default :
	        	throw new Exception("Validator Type not recognized.");
        }
        
        shellContext.getUIContext().getProvider().getOutput().out().println(
        		type.getValue().getType() + " Validator added.");
        return Results.success();
        
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }
}
