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
import org.switchyard.tools.forge.camel.CamelBindingConfigurator;
import org.switchyard.tools.forge.camel.CamelFacet;
import org.switchyard.tools.forge.camel.CamelServiceConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

public class CamelJavaServiceWizard extends AbstractSwitchYardCommand  {

    @Inject
    @WithAttributes(label = "Route Name", required = true)
    private UIInput<String> routeName;
    
    @Inject
    @WithAttributes(label = "Package name", type = InputType.JAVA_PACKAGE_PICKER)
    private UIInput<String> packageName;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private CamelServiceConfigurator camelServiceConfigurator;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder.getUIContext());
        if (project.hasFacet(SwitchYardFacet.class))
        {
           SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
        }
        builder.add(routeName);
        builder.add(packageName);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("New Camel Service")
                .description("Create a new Camel Service")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        CamelFacet facet = facetFactory.install(project, CamelFacet.class);
        
        camelServiceConfigurator.createJavaRoute(project, routeName.getValue(), 
        		packageName.getValue());
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
