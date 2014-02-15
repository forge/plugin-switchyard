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
import org.switchyard.tools.forge.camel.CamelFacet;
import org.switchyard.tools.forge.camel.CamelServiceConfigurator;
import org.switchyard.tools.forge.camel.RouteTypes;
import org.switchyard.tools.forge.camel.InterfaceTypes;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Creates a new Camel Java DSL or XML route.
 */
public class CamelServiceCreateCommand extends AbstractSwitchYardCommand  {

    @Inject
    @WithAttributes(label = "Route Name", required = true)
    private UIInput<String> routeName;
    
    @Inject
    @WithAttributes(label = "Package name", type = InputType.JAVA_PACKAGE_PICKER)
    private UIInput<String> packageName;
    
    @Inject
    @WithAttributes(label = "Route Type", required = true)
    private UISelectOne<RouteTypes> type;
    
    @Inject
    @WithAttributes(label = "Interface Type")
    private UIInput<InterfaceTypes> interfaceType;

    @Inject
    @WithAttributes(label = "WSDL Path")
    private UIInput<String> wsdlPath;
    
    @Inject
    @WithAttributes(label = "WSDL Port")
    private UIInput<String> wsdlPort;
    
    @Inject
    @WithAttributes(label = "Interface Class", type = InputType.JAVA_CLASS_PICKER)
    private UIInput<String> interfaceClass;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private CamelServiceConfigurator camelServiceConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
    	
        type.setItemLabelConverter(new Converter<RouteTypes, String>()
        {
           @Override
           public String convert(RouteTypes source)
           {
              return source.getType();
           }
        });
       
    	List choices = new ArrayList<RouteTypes>();
    	for (RouteTypes ppl : RouteTypes.values()) {
    		choices.add(ppl);
    	}
    	
        builder.add(routeName);
        builder.add(packageName);
        builder.add(type);
        
        builder.add(interfaceType);
        builder.add(wsdlPath);
        builder.add(wsdlPort);
        builder.add(interfaceClass);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: Camel Service Create")
                .description("Create a new Camel Service")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        CamelFacet facet = facetFactory.install(project, CamelFacet.class);
        
        switch(type.getValue()) {
        	case JAVA:
                camelServiceConfigurator.createJavaRoute(project, routeName.getValue(), 
                		packageName.getValue());
        		break;
        	case XML:
                camelServiceConfigurator.createXMLRoute(project, routeName.getValue(),
                		interfaceType.getValue(), wsdlPath.getValue(), 
                		wsdlPort.getValue(), interfaceClass.getValue());
        		break;
	        default :
	        	throw new Exception("Camel Route Type not recognized.");
        		
        }
        
        if (facet != null)
        {
           return Results.success("Camel Service has been installed.");
        }
        return Results.fail("Could not install Camel Service.");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
