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

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.ResourceFacet;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.tools.forge.addons.commands.CamelServiceCreateCommand;
import org.switchyard.tools.forge.addons.commands.CamelBindServiceCommand;
import org.switchyard.tools.forge.addons.commands.CamelBindReferenceCommand;
import org.switchyard.config.model.composite.v1.V1CompositeServiceModel;
import org.switchyard.config.model.composite.v1.V1BindingModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.config.model.composite.BindingModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.tools.forge.bean.BeanFacet;
import org.switchyard.tools.forge.camel.CamelFacet;
import org.switchyard.tools.forge.camel.CamelBindingConfigurator;
import org.switchyard.tools.forge.camel.CamelServiceConfigurator;
import org.switchyard.tools.forge.camel.InterfaceTypes;
import org.switchyard.tools.forge.camel.RouteTypes;
import org.switchyard.component.camel.core.model.v1.V1CamelUriBindingModel;


@RunWith(Arquillian.class)
public class CamelCommandTest {
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
 
   @Test
   public void testCamelServices() throws Exception
   {
      final Project project = projectFactory.createTempProject();
      facetFactory.install(project, CamelFacet.class);
      facetFactory.install(project, ResourcesFacet.class);
      try (CommandController tester = testHarness.createCommandController(CamelServiceCreateCommand.class,
              project.getRootDirectory()))
     {
        tester.initialize();
        
        tester.setValueFor("routeName", "TestRoute");
        tester.setValueFor("packageName", "foo");
        tester.setValueFor("type", RouteTypes.JAVA);
        Assert.assertTrue(tester.isValid());

        Result result = tester.execute();
        Assert.assertTrue(result.getMessage().equals("Camel Service has been installed."));
               
     }

      final Project xmlProject = projectFactory.createTempProject();
      facetFactory.install(xmlProject, CamelFacet.class);
      try (CommandController tester = testHarness.createCommandController(CamelServiceCreateCommand.class,
              project.getRootDirectory()))
     {
        tester.initialize();
        
        tester.setValueFor("routeName", "TestRoute");
        tester.setValueFor("packageName", "foo");
        tester.setValueFor("type", RouteTypes.XML);
        tester.setValueFor("interfaceType", InterfaceTypes.JAVA);
        tester.setValueFor("interfaceClass", "FooBar.java");
        Assert.assertTrue(tester.isValid());

        Result result = tester.execute();
        Assert.assertTrue(result.getMessage().equals("Camel Service has been installed."));
               
     }
     
      //Create Bean Service we can bind a Camel reference to 
      final Project camelProject = projectFactory.createTempProject();
      facetFactory.install(camelProject, CamelFacet.class);
      SwitchYardFacet switchYard = facetFactory.install(camelProject, SwitchYardFacet.class);
      String serviceName = "foobar";
      CompositeServiceModel service = new V1CompositeServiceModel();
      service.setName(serviceName);
      switchYard.getSwitchYardConfig().getComposite().addService(service);
      switchYard.saveConfig();
      
      // Bind Camel Reference
      String configURI = "http://foo.com";
      try (CommandController tester = testHarness.createCommandController(CamelBindServiceCommand.class,
    		  camelProject.getRootDirectory()))
      {
        tester.initialize();
        
        
        tester.setValueFor("serviceName", serviceName);
        tester.setValueFor("configURI", configURI);
        tester.setValueFor("operationName", "doStuff");
        Assert.assertTrue(tester.isValid());

        Result result = tester.execute();
        Assert.assertTrue(result.getMessage().equals("Camel Service Binding has been installed."));        
      }
      
   }

}
