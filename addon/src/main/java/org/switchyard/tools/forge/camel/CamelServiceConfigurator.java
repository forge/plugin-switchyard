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
package org.switchyard.tools.forge.camel;

import java.util.Arrays;
import java.util.List;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;

import org.switchyard.component.camel.model.v1.V1CamelImplementationModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.composite.v1.V1InterfaceModel;

import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.component.camel.model.CamelNamespace;
import org.switchyard.config.model.switchyard.SwitchYardNamespace;
import org.switchyard.tools.forge.plugin.TemplateResource;


/**
 * Commands related to Camel services.
 */
public class CamelServiceConfigurator
{

   // Template files used for camel route services
   private static final String ROUTE_INTERFACE_TEMPLATE =
            "/org/switchyard/tools/forge/camel/RouteInterfaceTemplate.java";
   private static final String ROUTE_XML_TEMPLATE =
            "/org/switchyard/tools/forge/camel/RouteXmlTemplate.xml";
   private static final String ROUTE_IMPLEMENTATION_TEMPLATE =
            "/org/switchyard/tools/forge/camel/RouteImplementationTemplate.java";
   
   /**
    * @param routeName route name
    * @param wsdlPath WSDL path (ex. wsdl/MyService.wsdl)
    * @param wsdlPort WSDL portType (ex. MyService);
    * @param interfaceClass the fully qualified java interface class (only required if Type is JAVA)
    */
   public void createXMLRoute(Project project, String routeName, InterfaceTypes type, String wsdlPath, String wsdlPort,
            String interfaceClass) throws java.io.IOException
   {
       List<String> typeList = Arrays.asList(new String[] {"wsdl", "java"});
       String intfValue;
       if (InterfaceTypes.WSDL.equals(type))
       {
           intfValue = wsdlPath + "#wsdl.porttype(" + wsdlPort + ")";
       }
       else
       {
           intfValue = interfaceClass;
       }

       
      // Create the component service model
      SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
      V1ComponentModel component = new V1ComponentModel();
      component.setName(routeName + "Component");
      V1ComponentServiceModel service = new V1ComponentServiceModel(SwitchYardNamespace.DEFAULT.uri());
      service.setName(routeName);
      component.addService(service);
      V1InterfaceModel intfModel = new V1InterfaceModel(type.getType());
      intfModel.setInterface(intfValue);
      service.setInterface(intfModel);

      // Create the Camel implementation model and XML route
      V1CamelImplementationModel impl = new V1CamelImplementationModel(CamelNamespace.DEFAULT.uri());
      TemplateResource xmlRoute = new TemplateResource(ROUTE_XML_TEMPLATE);
      String routeFile = routeName + ".xml";
      xmlRoute.serviceName(routeName);
      xmlRoute.writeResource(project.getFacet(ResourcesFacet.class).getResource(routeFile));
      impl.setXMLPath(routeFile);
      component.setImplementation(impl);

      // Add the new component service to the application config
      SwitchYardModel syConfig = switchYard.getSwitchYardConfig();
      syConfig.getComposite().addComponent(component);
      switchYard.saveConfig();
   }

   /**
    * Creates a Java DSL bean containing a Camel route. You'll notice that this code is very similar to the Bean
    * generation logic in the bean component plugin. We need to look at ways to synchronize these two pieces (e.g.
    * create a bean component, then add a route definition to it).
    */
   public void createJavaRoute(Project project, String routeName, String pkgName)
            throws java.io.IOException
   {
      // Create the camel interface and implementation
      TemplateResource camelIntf = new TemplateResource(ROUTE_INTERFACE_TEMPLATE);
      camelIntf.serviceName(routeName);
      String interfaceFile = camelIntf.writeJavaSource(
               project.getFacet(ResourcesFacet.class), pkgName, routeName, false);

      TemplateResource camelImpl = new TemplateResource(ROUTE_IMPLEMENTATION_TEMPLATE);
      camelImpl.serviceName(routeName);
      String implementationFile = camelImpl.writeJavaSource(
               project.getFacet(ResourcesFacet.class), pkgName, routeName + "Builder", false);
   }
}
