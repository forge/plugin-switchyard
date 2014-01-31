package org.switchyard.tools.forge.addons;

import java.util.Arrays;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.context.UISelection;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;
import org.jboss.forge.addon.resource.FileResource;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.camel.CamelBindingConfigurator;
import org.switchyard.tools.forge.camel.CamelFacet;
import org.switchyard.tools.forge.camel.InterfaceType;

public class CamelBindServiceWizard extends AbstractSwitchYardCommand  {

    @Inject
    @WithAttributes(label = "Service Name", required = true)
    private UIInput<String> serviceName;
    
    @Inject
    @WithAttributes(label = "configURI", required = true)
    private UIInput<String> configURI;

    @Inject
    @WithAttributes(label = "operationName", required = true)
    private UIInput<String> operationName;

    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private CamelBindingConfigurator camelBindingConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder.getUIContext());
        if (project.hasFacet(SwitchYardFacet.class))
        {
           SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
        }
        builder.add(serviceName);
       	builder.add(configURI);
        builder.add(operationName);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("Bind a Service with Camel")
                .description("Bind a Service with Camel")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        CamelFacet facet = facetFactory.install(project, CamelFacet.class);
        
        camelBindingConfigurator.bindService(project, serviceName.getValue(), 
        		configURI.getValue(), operationName.getValue());
        if (facet != null)
        {
           return Results.success("Camel Service Binding has been installed.");
        }
        return Results.fail("Could not install Camel Service Binding Service.");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }    

}
