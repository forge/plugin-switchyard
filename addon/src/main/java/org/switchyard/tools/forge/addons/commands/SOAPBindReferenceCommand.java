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
import org.switchyard.tools.forge.soap.SOAPFacet;
import org.switchyard.tools.forge.soap.SOAPServiceConfigurator;

/**
 * Binds a reference to a SOAP endpoint.
 */
public class SOAPBindReferenceCommand extends AbstractSwitchYardCommand  {

    @Inject
    @WithAttributes(label = "Reference Name", required = true)
    private UIInput<String> referenceName;
    
    @Inject
    @WithAttributes(label = "WSDL", required = true)
    private UIInput<String> wsdl;

    @Inject
    @WithAttributes(label = "Port Name", required = true)
    private UIInput<String> portName;
    
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
        builder.add(referenceName);
        builder.add(wsdl);
        builder.add(portName);
        builder.add(portType);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard : SOAP Bind Reference")
                .description("Bind a service with a SOAP reference")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        SOAPFacet facet = facetFactory.install(project, SOAPFacet.class);
        
        soapServiceConfigurator.bindReference(project, referenceName.getValue(), 
        		wsdl.getValue(), portName.getValue(), portType.getValue());
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
