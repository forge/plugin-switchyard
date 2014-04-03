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
package org.switchyard.tools.forge.bpm;

import java.io.File;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.switchyard.common.io.resource.SimpleResource;
import org.switchyard.common.io.resource.ResourceType;
import org.switchyard.component.bpm.BPMOperationType;
import org.switchyard.component.bpm.config.model.BPMNamespace;
import org.switchyard.component.bpm.config.model.v1.V1BPMComponentImplementationModel;
import org.switchyard.component.bpm.config.model.v1.V1BPMOperationModel;
import org.switchyard.component.common.knowledge.config.model.OperationModel;
import org.switchyard.component.common.knowledge.config.model.v1.V1ManifestModel;
import org.switchyard.component.common.knowledge.config.model.v1.V1OperationsModel;
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
 * Forge plugin for Bean component commands.
 */
public class BPMServiceConfigurator
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
         JavaInterfaceSource processInterface = Roaster.create(JavaInterfaceSource.class)
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
         template.writeResource(project.getFacet(ResourcesFacet.class).getResource(processDefinitionPath));
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
      V1ComponentServiceModel service = new V1ComponentServiceModel(SwitchYardNamespace.DEFAULT.uri());
      service.setName(serviceName);
      InterfaceModel csi = new V1InterfaceModel(InterfaceModel.JAVA);
      csi.setInterface(interfaceName);
      service.setInterface(csi);
      component.addService(service);

      // Create the BPM implementation model and add it to the component model
      V1BPMComponentImplementationModel bpm = new V1BPMComponentImplementationModel(BPMNamespace.DEFAULT.uri());
      bpm.setProcessId(processId);
      bpm.setPersistent(persistent);

      V1OperationsModel operations = new V1OperationsModel(BPMNamespace.DEFAULT.uri());
      OperationModel operation = (OperationModel)new V1BPMOperationModel(BPMNamespace.DEFAULT.uri()).setType(BPMOperationType.START_PROCESS).setName("operation");
      operations.addOperation(operation);
      bpm.setOperations(operations);
      V1ManifestModel manifest = new V1ManifestModel(BPMNamespace.DEFAULT.uri());
      V1ResourcesModel resources = new V1ResourcesModel(BPMNamespace.DEFAULT.uri());
      resources.addResource(new V1ResourceModel(BPMNamespace.DEFAULT.uri()).setLocation(processDefinition).setType(ResourceType.valueOf("BPMN2")));
      manifest.setResources(resources);
      bpm.setManifest(manifest);

      // Add the new component service to the application config
      SwitchYardModel syConfig = switchYard.getSwitchYardConfig();
      syConfig.getComposite().addComponent(component);
      switchYard.saveConfig();
   }

}
