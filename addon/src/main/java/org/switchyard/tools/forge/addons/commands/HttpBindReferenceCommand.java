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
import org.switchyard.tools.forge.camel.CamelFacet;
import org.switchyard.tools.forge.http.HttpServiceConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Binds a reference to a HTTP endpoint.
 */
public class HttpBindReferenceCommand extends AbstractSwitchYardCommand  {

    @Inject
    @WithAttributes(label = "Reference Name", required = true)
    private UIInput<String> referenceName;
    
    @Inject
    @WithAttributes(label = "Address", required = true)
    private UIInput<String> address;

    @Inject
    @WithAttributes(label = "Method", required = true)
    private UIInput<String> method;
    
    @Inject
    @WithAttributes(label = "Content type", required=true)
    private UIInput<String> contentType;
    
    @Inject
    @WithAttributes(label = "User Name", required = false)
    private UIInput<String> userName;
    
    @Inject
    @WithAttributes(label = "Password", required = false)
    private UIInput<String> password;
    
    @Inject
    @WithAttributes(label = "Realm", required = false)
    private UIInput<String> realmName;
    
    @Inject
    @WithAttributes(label = "Domain", required = false)
    private UIInput<String> domainName;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private HttpServiceConfigurator httpServiceConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder.getUIContext());
        if (project.hasFacet(SwitchYardFacet.class))
        {
           SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
        }
        builder.add(referenceName);
        builder.add(address);
        builder.add(method);
        builder.add(contentType);
        builder.add(userName);
        builder.add(password);
        builder.add(domainName);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard : HTTP Bind Reference")
                .description("Bind Service with a HTTP Reference")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        CamelFacet facet = facetFactory.install(project, CamelFacet.class);
        
        httpServiceConfigurator.bindReference(project, referenceName.getValue(), 
        		address.getValue(), method.getValue(), contentType.getValue(), 
        		userName.getValue(), password.getValue(), realmName.getValue(), 
        		domainName.getValue());
        if (facet != null)
        {
           return Results.success("HTTP Reference has been installed.");
        }
        return Results.fail("Could not install HTTP Reference.");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
