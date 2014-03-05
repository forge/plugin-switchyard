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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.switchyard.tools.forge.camel.CamelFacet;
import org.switchyard.tools.forge.plugin.SwitchYardConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

import org.switchyard.config.model.composite.ComponentModel;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.config.model.composite.ComponentServiceModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentReferenceModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.switchyard.SwitchYardNamespace;


@RunWith(Arquillian.class)
public class CamelReferenceCommandTest {
   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.addon:projects"),
            @AddonDependency(name = "org.jboss.forge.addon:resources"),
            @AddonDependency(name = "org.jboss.forge.addon:maven"),
            @AddonDependency(name = "org.switchyard.forge:switchyard-addon", version = "2.0.0-SNAPSHOT"),
            @AddonDependency(name = "org.jboss.forge.addon:ui"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness")
   })
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap
               .create(ForgeArchive.class)
               .addBeansXML()
               .addAsAddonDependencies(
                    AddonDependencyEntry.create("org.jboss.forge.addon:projects"),
                    AddonDependencyEntry.create("org.jboss.forge.addon:resources"),
                    AddonDependencyEntry.create("org.jboss.forge.addon:maven"),
                    AddonDependencyEntry.create("org.switchyard.forge:switchyard-addon"),
                    AddonDependencyEntry.create("org.jboss.forge.addon:ui"),
                    AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                    AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness")
            		   );
      return archive;
   }

   @Inject
   private FacetFactory facetFactory;

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private UITestHarness testHarness;
   
   @Inject
   private SwitchYardConfigurator switchYardConfigurator;
   
   @Test
   public void testCamelReferences() throws Exception
   {
      final Project project = projectFactory.createTempProject();
      facetFactory.install(project, CamelFacet.class);
      String serviceName = "TestService";
      String referenceName = "TestReference";
      SwitchYardFacet switchYard = facetFactory.install(project, SwitchYardFacet.class);
  
      ComponentModel component = new V1ComponentModel();
      component.setName("TestComponent");
      ComponentServiceModel service = new V1ComponentServiceModel(SwitchYardNamespace.DEFAULT.uri());
      service.setName(serviceName);
      component.addService(service);

      ComponentReferenceModel reference = new V1ComponentReferenceModel(SwitchYardNamespace.DEFAULT.uri());
      reference.setName(referenceName);
      component.addReference(reference);
      
      switchYard.getSwitchYardConfig().getComposite().addComponent(component);
      //switchYard.promoteReference(referenceName);
      switchYard.saveConfig();
      
      switchYardConfigurator.promoteReference(project, referenceName);
      
      try (CommandController tester = testHarness.createCommandController(CamelBindReferenceCommand.class,
              project.getRootDirectory()))
     {
        tester.initialize();
        
        tester.setValueFor("referenceName", referenceName);
        tester.setValueFor("configURI", "blah");
        Assert.assertTrue(tester.isValid());

        Result result = tester.execute();
        Assert.assertTrue(result.getMessage().equals("Camel Reference has been bound."));
     }
      
   }

}
