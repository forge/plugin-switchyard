/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.switchyard.tools.forge.plugin;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.config.model.composite.v1.V1BindingModel;
import org.switchyard.config.model.composite.v1.V1CompositeServiceModel;
import org.switchyard.tools.forge.plugin.SwitchYardConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

@RunWith(Arquillian.class)
public class SwitchyardConfigurationTest
{
   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.switchyard.forge:switchyard-forge-plugin", version = "1.0.0-SNAPSHOT"),
            @AddonDependency(name = "org.jboss.forge.addon:projects"),
            @AddonDependency(name = "org.jboss.forge.addon:maven")
   })
   public static ForgeArchive getDeployment()
   {
      return ShrinkWrap.create(ForgeArchive.class).
               addBeansXML().
               addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.addon:projects"),
                        AddonDependencyEntry.create("org.switchyard.forge:switchyard-forge-plugin"),
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
               );
   }

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private FacetFactory facetFactory;
   
   @Inject
   private SwitchYardConfigurator switchYardConfigurator;

   Project project;

   private static final String ORIG_CONFIG_PATH = "target/test-classes/standalone.xml";
   private static final String BACKUP_CONFIG_PATH = ORIG_CONFIG_PATH + ".orig";

   
   @Before
   public void setupTestProject()
   {
      project = projectFactory.createTempProject();
      project.getProjectRoot().deleteOnExit();

      MetadataFacet metadataFacet = project.getFacet(MetadataFacet.class);
      metadataFacet.setProjectName("testproject");
      metadataFacet.setProjectVersion("1.0.0-SNAPSHOT");
      metadataFacet.setTopLevelPackage("com.acme.testproject");
   }

   @After
   public void cleanupTestProject()
   {
      if (project != null)
         project.getProjectRoot().delete(true);
   }

   @Test
   public void testInstallSwitchyard() throws Exception
   {
      Assert.assertFalse(project.hasFacet(SwitchYardFacet.class));
      SwitchYardFacet switchYard = facetFactory.install(project, SwitchYardFacet.class);
      Assert.assertNotNull(switchYard);
      
      switchYard.addSwitchYardToASConfig(ORIG_CONFIG_PATH);
      Assert.assertTrue(new File(ORIG_CONFIG_PATH).exists());
      Assert.assertTrue(new File(BACKUP_CONFIG_PATH).exists());

      String serviceName = "ForgeTestService";
      CompositeServiceModel service = new V1CompositeServiceModel();
      service.setName(serviceName);
      service.addBinding(new V1BindingModel("bean"));
      switchYard.getSwitchYardConfig().getComposite().addService(service);
      switchYard.saveConfig();
      
      System.out.println("facetFactory implemented by" + facetFactory.getClass().getName());
      Assert.assertTrue(project.hasFacet(SwitchYardFacet.class));
      Assert.assertNotNull(project.getFacet(SwitchYardFacet.class));
      Assert.assertNotNull(switchYard.getMergedSwitchYardConfig().getComposite());
            
   }
}
