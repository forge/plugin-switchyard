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
import org.switchyard.policy.Policy;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.rules.RulesFacet;
import org.switchyard.tools.forge.plugin.SwitchYardConfigurator;

/**
 * Command to import the specified artifact into the application project.
 */
public class ImportArtifactsCommand extends AbstractSwitchYardCommand {
	
    @Inject
    @WithAttributes(label = "URL String", required = true)
    private UIInput<String> urlStr;
    
    @Inject
    @WithAttributes(label = "Artifact Name", required = true)
    private UIInput<String> artifactName;
	
    @Inject
    @WithAttributes(label = "Download", required = false)
    private UIInput<String> download;
        
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private SwitchYardConfigurator switchYardConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
    	builder.add(urlStr);
    	builder.add(artifactName);
    	builder.add(download);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: Import Artifacts")
                .description("Import service artifacts into project")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext shellContext) throws Exception {
        Project project = getSelectedProject(shellContext);
        SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);

        switchYardConfigurator.importArtifacts(project, urlStr.getValue(), 
        		artifactName.getValue(), Boolean.valueOf(download.getValue()).booleanValue());
        	
        shellContext.getUIContext().getProvider().getOutput().out().println("Artifact " 
        + artifactName.getValue() + " imported.");
        return Results.success();
        
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}