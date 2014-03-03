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
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
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
import org.switchyard.tools.forge.bean.BeanFacet;
import org.switchyard.tools.forge.bean.BeanReferenceConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Command for creating a new Bean Reference.
 */
public class BeanReferenceCommand extends AbstractSwitchYardCommand  {

    @Inject
    @WithAttributes(label = "Package Name", required = true, type = InputType.JAVA_PACKAGE_PICKER)
    private UIInput<String> packageName;
	
    @Inject
    @WithAttributes(label = "Bean Name", required = true)
    private UIInput<String> beanName;
    
    @Inject
    @WithAttributes(label = "Reference Name", required = true)
    private UIInput<String> referenceName;
    
    @Inject
    @WithAttributes(label = "Reference Bean Name", required = true)
    private UIInput<String> referenceBeanName;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private BeanReferenceConfigurator beanReferenceConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder.getUIContext());
        if (project.hasFacet(SwitchYardFacet.class))
        {
           SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
        }
        String pkgName = project.getFacet(MetadataFacet.class).getTopLevelPackage();
        packageName.setDefaultValue(pkgName);
        builder.add(packageName);
        builder.add(beanName);
        builder.add(referenceName);
        builder.add(referenceBeanName);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("SwitchYard: New Bean Reference")
                .description("Create a new Bean Reference")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        BeanFacet facet = facetFactory.install(project, BeanFacet.class);
        JavaSourceFacet jsFacet = facetFactory.install(project, JavaSourceFacet.class);
        
        beanReferenceConfigurator.newReference(project, 
        		beanName.getValue(),
        		packageName.getValue(), 
        		referenceName.getValue(), 
        		referenceBeanName.getValue());
        if (facet != null)
        {
           return Results.success("Bean Reference has been installed.");
        }
        return Results.fail("Could not install Bean Reference.");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
