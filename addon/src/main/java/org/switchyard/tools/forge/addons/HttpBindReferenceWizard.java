package org.switchyard.tools.forge.addons;

import java.util.Arrays;

import javax.inject.Inject;

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

public class HttpBindReferenceWizard extends AbstractSwitchYardCommand  {

    @Inject
    @WithAttributes(label = "Service Name", required = true)
    private UIInput<String> serviceName;
    
    @Inject
    @WithAttributes(label = "SwitchYardVersion", required = true)
    private UIInput<String> switchYardVersion;

    @Inject
    @WithAttributes(label = "Target Directory", required = true)
    private UIInput<DirectoryResource> targetLocation;
    
    @Inject
    @WithAttributes(label = "Target package", type = InputType.JAVA_PACKAGE_PICKER)
    private UIInput<String> targetPackage;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder.getUIContext());
        if (project == null)
        {
            UISelection<FileResource<?>> currentSelection = builder.getUIContext().getInitialSelection();
            if (!currentSelection.isEmpty())
            {
               FileResource<?> resource = currentSelection.get();
               if (resource instanceof DirectoryResource)
               {
                  targetLocation.setDefaultValue((DirectoryResource) resource);
               }
               else
               {
                  targetLocation.setDefaultValue(resource.getParent());
               }
            }
        }
        else if (project.hasFacet(SwitchYardFacet.class))
        {
           SwitchYardFacet facet = project.getFacet(SwitchYardFacet.class);
//           targetLocation.setDefaultValue(facet.).setEnabled(false);
           switchYardVersion.setValue(facet.getVersion());
//           targetPackage.setValue(calculateConverterPackage(project));
        }
        builder.add(targetLocation);
    }
    
    @Override
    public Metadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).name("Bind Service with HTTP Reference")
                .description("Bind Service with a HTTP Reference")
                .category(Categories.create(super.getMetadata(context).getCategory(), "SwitchYard"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
