package org.switchyard.tools.forge.addons;

import java.util.Arrays;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
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
import org.switchyard.tools.forge.bean.BeanFacet;
import org.switchyard.tools.forge.bean.BeanServiceConfigurator;
import org.switchyard.tools.forge.camel.CamelBindingConfigurator;
import org.switchyard.tools.forge.camel.RouteType;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

public class CamelBindReferenceWizard extends AbstractSwitchYardCommand {

    @Inject
    @WithAttributes(label = "Reference Name", required = true)
    private UIInput<String> referenceName;
    
    @Inject
    @WithAttributes(label = "ConfigURI", required = true)
    private UIInput<String> configURI;

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
        builder.add(referenceName);
        builder.add(configURI);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("Bind Camel Reference")
                .description("Bind Camel Reference")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        BeanFacet facet = facetFactory.install(project, BeanFacet.class);
        
        camelBindingConfigurator.bindReference(project, referenceName.getValue(), 
        		configURI.getValue());
        if (facet != null)
        {
           return Results.success("Camel Reference has been bound.");
        }
        return Results.fail("Could not install Camel Reference.");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
