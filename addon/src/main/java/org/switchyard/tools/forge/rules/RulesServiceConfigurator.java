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
package org.switchyard.tools.forge.rules;

import java.io.File;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaInterface;

import org.switchyard.common.io.resource.ResourceType;
import org.switchyard.component.common.knowledge.config.model.OperationModel;
import org.switchyard.component.common.knowledge.config.model.v1.V1ManifestModel;
import org.switchyard.component.common.knowledge.config.model.v1.V1OperationsModel;
import org.switchyard.component.rules.RulesOperationType;
import org.switchyard.component.rules.config.model.RulesNamespace;
import org.switchyard.component.rules.config.model.v1.V1RulesComponentImplementationModel;
import org.switchyard.component.rules.config.model.v1.V1RulesOperationModel;
import org.switchyard.config.model.composite.InterfaceModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.composite.v1.V1InterfaceModel;
import org.switchyard.config.model.resource.v1.V1ResourceModel;
import org.switchyard.config.model.resource.v1.V1ResourcesModel;

import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.config.model.switchyard.SwitchYardNamespace;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.plugin.TemplateResource;

/**
 * Forge plugin for Rules component commands.
 */
public class RulesServiceConfigurator
{

   // rule definition template
   private static final String RULES_TEMPLATE = "RulesTemplate.drl";
   // rule definition file extension
   private static final String RULES_EXTENSION = ".drl";
   // rule definition directory
   private static final String RULES_DIR = "META-INF";

   /**
    * Create a new rules service interface and implementation.
    * 
    * @param argServiceName service name
    * @param out shell output
    * @param argInterfaceClass class name of Java service interface
    * @param argRuleFilePath path to the rule definition
    * @param argAgent whether to use an agent
    * @throws java.io.IOException error with file resources
    */
   public void newRules(
            Project project,
            final String argServiceName,
            String pkgName,
            final String argInterfaceClass,
            final String argRuleFilePath,
            final Boolean argAgent) throws java.io.IOException
   {

      JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
      String interfaceClass = argInterfaceClass;

      if (interfaceClass == null)
      {
         if (pkgName == null)
            pkgName = project.getFacet(MetadataFacet.class).getTopLevelPackage();

         // Create the service interface
         JavaInterface ruleInterface = JavaParser.create(JavaInterface.class)
                  .setPackage(pkgName)
                  .setName(argServiceName)
                  .setPublic();
         java.saveJavaSource(ruleInterface);
         interfaceClass = ruleInterface.getQualifiedName();
      }

      String ruleDefinitionPath = argRuleFilePath;
      if (ruleDefinitionPath == null)
      {
         // Create an empty rule definition
         ruleDefinitionPath = RULES_DIR + File.separator + argServiceName + RULES_EXTENSION;
         TemplateResource template = new TemplateResource(RULES_TEMPLATE)
                  .serviceName(argServiceName)
                  .packageName(pkgName);
         template.writeResource(project.getFacet(ResourcesFacet.class).getResource(ruleDefinitionPath));
      }

      boolean agent = argAgent != null ? argAgent.booleanValue() : false;

      // Add the SwitchYard config
      createImplementationConfig(project, argServiceName, interfaceClass, ruleDefinitionPath, agent);
   }

   
   /**
    * Create the implementation config.
    * @param project project
    * @param serviceName service name
    * @param interfaceName interface name
    * @param rulesDefinition rules definition
    * @param agent agent
    */
   private void createImplementationConfig(Project project,
            String serviceName,
            String interfaceName,
            String rulesDefinition,
            boolean agent)
   {

      SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
      // Create the component service model
      V1ComponentModel component = new V1ComponentModel();
      component.setName(serviceName);
      V1ComponentServiceModel service = new V1ComponentServiceModel(SwitchYardNamespace.DEFAULT.uri());
      service.setName(serviceName);
      InterfaceModel csi = new V1InterfaceModel(InterfaceModel.JAVA);
      csi.setInterface(interfaceName);
      service.setInterface(csi);
      component.addService(service);

      // Create the Rules implementation model and add it to the component model
      V1RulesComponentImplementationModel rules = new V1RulesComponentImplementationModel(SwitchYardNamespace.DEFAULT.uri());
      V1OperationsModel operations = new V1OperationsModel(RulesNamespace.DEFAULT.uri());
      OperationModel operation = (OperationModel)new V1RulesOperationModel(RulesNamespace.DEFAULT.uri()).setType(RulesOperationType.EXECUTE).setName("operation");
      operations.addOperation(operation);
      rules.setOperations(operations);
      V1ManifestModel manifest = new V1ManifestModel(RulesNamespace.DEFAULT.uri());
      V1ResourcesModel resources = new V1ResourcesModel(RulesNamespace.DEFAULT.uri());
      resources.addResource(new V1ResourceModel(RulesNamespace.DEFAULT.uri()).setLocation(rulesDefinition).setType(ResourceType.valueOf("DRL")));
      manifest.setResources(resources);
      rules.setManifest(manifest);
      component.setImplementation(rules);
      
      // Add the new component service to the application config
      SwitchYardModel syConfig = switchYard.getSwitchYardConfig();
      syConfig.getComposite().addComponent(component);
      switchYard.saveConfig();
   }
}
