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
import org.jboss.forge.addon.projects.facets.MetadataFacet;
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
import org.switchyard.tools.forge.bpm.BPMFacet;
import org.switchyard.tools.forge.bpm.BPMServiceConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Command for adding a new BPM process.
 */
public class BPMServiceCommand extends AbstractSwitchYardCommand {

    @Inject
    @WithAttributes(label = "Service Name", required = true)
    private UIInput<String> serviceName;

    @Inject
    @WithAttributes(label = "Package Name", required = true, type = InputType.JAVA_PACKAGE_PICKER)
    private UIInput<String> packageName;
    
    @Inject
    @WithAttributes(label = "Interface Class", required = true, type = InputType.JAVA_CLASS_PICKER)
    private UIInput<String> interfaceClass;
    
    @Inject
    @WithAttributes(label = "Process File Path")
    private UIInput<String> processFilePath;
    
    @Inject
    @WithAttributes(label = "Process Id")
    private UIInput<String> processId;
    
    @Inject
    @WithAttributes(label = "Persistent")
    private UIInput<Boolean> persistent;
    
    @Inject
    @WithAttributes(label = "Session Id")
    private UIInput<Integer> sessionId;
    
    @Inject
    @WithAttributes(label = "Message Content In Name")
    private UIInput<String> messageContentInName;
    
    @Inject
    @WithAttributes(label = "Message Content Out Name")
    private UIInput<String> messageContentOutName;
    
    @Inject
    @WithAttributes(label = "Agent")
    private UIInput<Boolean> agent;
    
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private BPMServiceConfigurator bpmServiceConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        builder.add(serviceName);
        builder.add(packageName);
        
        builder.add(interfaceClass);
        builder.add(processFilePath);
        builder.add(processId);
        builder.add(persistent);
        builder.add(sessionId);
        builder.add(messageContentInName);
        builder.add(messageContentOutName);
        builder.add(agent);

    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: BPM Service Create")
                .description("Create a new BPM Service")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        BPMFacet facet = facetFactory.install(project, BPMFacet.class);
        
        bpmServiceConfigurator.newProcess(project, serviceName.getValue(),
        		packageName.getValue(), interfaceClass.getValue(),
        		processFilePath.getValue(), processId.getValue(),
        		persistent.getValue(), sessionId.getValue(),
        		messageContentInName.getValue(), messageContentOutName.getValue(),
        		agent.getValue()
        		);
        if (facet != null)
        {
           return Results.success("BPM Service has been installed.");
        }
        return Results.fail("Could not install BPM Service.");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
