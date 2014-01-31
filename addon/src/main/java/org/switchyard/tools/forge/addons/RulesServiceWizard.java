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
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.rules.RulesFacet;
import org.switchyard.tools.forge.rules.RulesServiceConfigurator;

public class RulesServiceWizard extends AbstractSwitchYardCommand  {

    @Inject
    @WithAttributes(label = "Service Name", required = true)
    private UIInput<String> serviceName;
    
    @Inject
    @WithAttributes(label = "Package Name", type = InputType.JAVA_PACKAGE_PICKER)
    private UIInput<String> packageName;
    
    @Inject
    @WithAttributes(label = "Interface Class", type = InputType.JAVA_CLASS_PICKER)
    private UIInput<String> interfaceClass;
    

    @Inject
    @WithAttributes(label = "Rule File Path", required = true, type = InputType.FILE_PICKER)
    private UIInput<String> ruleFilePath;
    
    @Inject
    @WithAttributes(label = "Agent", required = true)
    private UIInput<Boolean> isAgent;
    
    @Inject
    private FacetFactory facetFactory;
    
    @Inject 
    private RulesServiceConfigurator rulesServiceConfigurator;
    
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder.getUIContext());
        if (project.hasFacet(SwitchYardFacet.class))
        {
           SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
        }
        builder.add(serviceName);
        builder.add(packageName);
        builder.add(interfaceClass);
        builder.add(ruleFilePath);
        builder.add(isAgent);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("New Rules Service")
                .description("Create a new Rules Service")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        RulesFacet facet = facetFactory.install(project, RulesFacet.class);
        
        rulesServiceConfigurator.newRules(project, serviceName.getValue(), 
        		packageName.getValue(), interfaceClass.getValue(), ruleFilePath.getValue(), 
        		isAgent.getValue());
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
