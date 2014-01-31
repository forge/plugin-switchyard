package org.switchyard.tools.forge.addons;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

public abstract class AbstractSwitchYardCommand extends AbstractProjectCommand {
    @Inject
    private ProjectFactory projectFactory;

    private static final String CATEGORY_NAME = "SwitchYard";
    
    @Override
    public UICommandMetadata getMetadata(UIContext context)
    {
       return Metadata.from(super.getMetadata(context), getClass()).category(Categories.create(CATEGORY_NAME));
    }

    @Override
    protected ProjectFactory getProjectFactory()
    {
       return projectFactory;
    }
}
