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
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.soap.SOAPFacet;
import org.switchyard.tools.forge.soap.SOAPServiceConfigurator;

/**
 * Binds a service to a SOAP endpoint.
 */
public class SOAPBindServiceCommand extends AbstractSwitchYardCommand  {

    @Inject
    @WithAttributes(label = "Service Name", required = true)
    private UIInput<String> serviceName;
    
    @Inject
    @WithAttributes(label = "WSDL Location", required = true)
    private UIInput<String> wsdlLocation;

    @Inject
    @WithAttributes(label = "Socket Address", required = true)
    private UIInput<String> socketAddr;
    
    @Inject
    @WithAttributes(label = "Port Type", required = true)
    private UIInput<String> portType;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private SOAPServiceConfigurator soapServiceConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder.getUIContext());
        if (project.hasFacet(SwitchYardFacet.class))
        {
           SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
        }
        builder.add(serviceName);
        builder.add(wsdlLocation);
        builder.add(socketAddr);
        builder.add(portType);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard : SOAP Bind Service")
                .description("Bind a Service with SOAP")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        SOAPFacet facet = facetFactory.install(project, SOAPFacet.class);
        
        soapServiceConfigurator.bindService(project, serviceName.getValue(), 
        		wsdlLocation.getValue(), socketAddr.getValue(), portType.getValue());
        if (facet != null)
        {
           return Results.success("SOAP Reference has been installed.");
        }
        return Results.fail("Could not install SOAP Reference.");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
