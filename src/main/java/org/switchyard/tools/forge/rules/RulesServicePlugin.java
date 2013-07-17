/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General Public License, 
 * v.2.1 along with this distribution; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */

package org.switchyard.tools.forge.rules;

import java.io.File;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourceFacet;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaInterface;
import org.switchyard.component.rules.RulesActionType;
import org.switchyard.component.rules.config.model.RulesComponentImplementationModel;
import org.switchyard.component.rules.config.model.v1.V1RulesActionModel;
import org.switchyard.component.rules.config.model.v1.V1RulesComponentImplementationModel;
import org.switchyard.config.model.composite.InterfaceModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.composite.v1.V1InterfaceModel;
import org.switchyard.config.model.resource.v1.V1ResourceModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.plugin.TemplateResource;

/**
 * Forge plugin for Rules component commands.
 */
public class RulesServicePlugin
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
            final Boolean argAgent
            )
                     throws java.io.IOException
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
         template.writeResource(project.getFacet(ResourceFacet.class).getResource(ruleDefinitionPath));
      }

      boolean agent = argAgent != null ? argAgent.booleanValue() : false;

      // Add the SwitchYard config
      createImplementationConfig(project, argServiceName, interfaceClass, ruleDefinitionPath, agent);
   }

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
      V1ComponentServiceModel service = new V1ComponentServiceModel();
      service.setName(serviceName);
      InterfaceModel csi = new V1InterfaceModel(InterfaceModel.JAVA);
      csi.setInterface(interfaceName);
      service.setInterface(csi);
      component.addService(service);

      // Create the Rules implementation model and add it to the component model
      V1RulesComponentImplementationModel rules = new V1RulesComponentImplementationModel();
      rules.addResource(new V1ResourceModel(RulesComponentImplementationModel.DEFAULT_NAMESPACE)
               .setLocation(rulesDefinition));
      rules.addRulesAction(new V1RulesActionModel()
               .setName("operation")
               .setType(RulesActionType.EXECUTE));
      rules.setAgent(agent);
      component.setImplementation(rules);

      // Add the new component service to the application config
      SwitchYardModel syConfig = switchYard.getSwitchYardConfig();
      syConfig.getComposite().addComponent(component);
      switchYard.saveConfig();
   }

}
