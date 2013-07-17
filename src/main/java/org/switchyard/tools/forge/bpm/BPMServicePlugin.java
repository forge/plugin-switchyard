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

package org.switchyard.tools.forge.bpm;

import static org.switchyard.component.bpm.task.work.SwitchYardServiceTaskHandler.SWITCHYARD_SERVICE;

import java.io.File;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourceFacet;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaInterface;
import org.switchyard.common.io.resource.SimpleResource;
import org.switchyard.component.bpm.config.model.v1.V1BPMComponentImplementationModel;
import org.switchyard.component.bpm.config.model.v1.V1TaskHandlerModel;
import org.switchyard.component.bpm.task.work.SwitchYardServiceTaskHandler;
import org.switchyard.config.model.composite.InterfaceModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.composite.v1.V1InterfaceModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.plugin.TemplateResource;

/**
 * Forge plugin for Bean component commands.
 */
public class BPMServicePlugin
{

   // process definition template
   private static final String PROCESS_TEMPLATE = "ProcessTemplate.bpmn";
   // process definition file extension
   private static final String PROCESS_EXTENSION = ".bpmn";
   // process definition directory
   private static final String PROCESS_DIR = "META-INF";
   // VAR_* constants reference substitution tokens in the process definition template
   private static final String VAR_PROCESS_ID = "${process.id}";
   private static final String VAR_MESSAGE_CONTENT_IN_NAME = "${message.content.in.name}";
   private static final String VAR_MESSAGE_CONTENT_OUT_NAME = "${message.content.out.name}";

   /**
    * Create a new BPM service interface and implementation.
    * 
    * @param serviceName service name
    * @param interfaceClass class name of Java service interface
    * @param processFilePath path to the BPMN process definition
    * @param processId business process id
    * @param persistent persistent flag
    * @param argSessionId session id
    * @param argMessageContentInName process variable name for the content of the incoming message
    * @param argMessageContentOutName process variable name for the content of the outgoing message
    * @param argAgent whether to use an agent
    * @param out shell output
    * @throws java.io.IOException error with file resources
    */
   public void newProcess(
            Project project,
            final String serviceName,
            String pkgName,
            String interfaceClass,
            final String processFilePath,
            String processId,
            final boolean persistent,
            final Integer argSessionId,
            final String argMessageContentInName,
            final String argMessageContentOutName,
            final Boolean argAgent
            ) throws java.io.IOException
   {

      if (pkgName == null)
      {
         pkgName = project.getFacet(MetadataFacet.class).getTopLevelPackage() + ".processes";
      }

      project.getFacet(JavaSourceFacet.class);

      if (interfaceClass == null)
      {
         // Create the service interface
         JavaInterface processInterface = JavaParser.create(JavaInterface.class)
                  .setPackage(pkgName)
                  .setName(serviceName)
                  .setPublic();

         // Add a default process method...
         processInterface.addMethod("void process(String content);");

         JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
         java.saveJavaSource(processInterface);
         interfaceClass = processInterface.getQualifiedName();
      }

      if (processId == null)
      {
         processId = serviceName;
      }

      String processDefinitionPath = processFilePath;
      if (processDefinitionPath == null)
      {
         // Create an empty process definition
         processDefinitionPath = PROCESS_DIR + File.separator + serviceName + PROCESS_EXTENSION;
         TemplateResource template = new TemplateResource(PROCESS_TEMPLATE)
                  .serviceName(serviceName)
                  .replaceToken(VAR_PROCESS_ID, processId)
                  .replaceToken(VAR_MESSAGE_CONTENT_IN_NAME, argMessageContentInName)
                  .replaceToken(VAR_MESSAGE_CONTENT_OUT_NAME, argMessageContentOutName)
                  .packageName(pkgName);
         template.writeResource(project.getFacet(ResourceFacet.class).getResource(processDefinitionPath));
      }

      boolean agent = argAgent != null ? argAgent.booleanValue() : false;

      // Add the SwitchYard config
      createImplementationConfig(project, serviceName, interfaceClass, processId, persistent, argSessionId,
               processDefinitionPath, argMessageContentInName, argMessageContentOutName, agent);
   }

   private void createImplementationConfig(Project project,
            String serviceName,
            String interfaceName,
            String processId,
            boolean persistent,
            Integer sessionId,
            String processDefinition,
            String messageContentInName,
            String messageContentOutName,
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

      // Create the BPM implementation model and add it to the component model
      V1BPMComponentImplementationModel bpm = new V1BPMComponentImplementationModel();
      bpm.setProcessDefinition(new SimpleResource(processDefinition));
      bpm.setProcessId(processId);
      bpm.setPersistent(persistent);
      if (sessionId != null && sessionId.intValue() > -1)
      {
         bpm.setSessionId(sessionId);
      }
      bpm.setMessageContentInName(messageContentInName);
      bpm.setMessageContentOutName(messageContentOutName);
      bpm.setAgent(agent);
      V1TaskHandlerModel switchyardHandler = new V1TaskHandlerModel();
      switchyardHandler.setName(SWITCHYARD_SERVICE);
      switchyardHandler.setClazz(SwitchYardServiceTaskHandler.class);
      bpm.addTaskHandler(switchyardHandler);
      component.setImplementation(bpm);

      // Add the new component service to the application config
      SwitchYardModel syConfig = switchYard.getSwitchYardConfig();
      syConfig.getComposite().addComponent(component);
      switchYard.saveConfig();
   }

}
