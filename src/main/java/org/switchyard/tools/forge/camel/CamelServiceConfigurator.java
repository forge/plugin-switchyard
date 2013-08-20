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

package org.switchyard.tools.forge.camel;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.furnace.services.Exported;
import org.switchyard.component.camel.config.model.v1.V1CamelImplementationModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.composite.v1.V1InterfaceModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.plugin.TemplateResource;

/**
 * Commands related to Camel services.
 */
@Exported
public class CamelServiceConfigurator
{

   // Template files used for camel route services
   private static final String ROUTE_INTERFACE_TEMPLATE =
            "/org/switchyard/tools/forge/camel/RouteInterfaceTemplate.java";
   private static final String ROUTE_XML_TEMPLATE =
            "/org/switchyard/tools/forge/camel/RouteXmlTemplate.xml";
   private static final String ROUTE_IMPLEMENTATION_TEMPLATE =
            "/org/switchyard/tools/forge/camel/RouteImplementationTemplate.java";

   public enum Type
   {
      WSDL("wsdl"), JAVA("java");

      String type;

      private Type(String type)
      {
         this.type = type;
      }

      public String getType()
      {
         return type;
      }
   }

   /**
    * @param routeName route name
    * @param wsdlPath WSDL path (ex. wsdl/MyService.wsdl)
    * @param wsdlPort WSDL portType (ex. MyService);
    * @param interfaceClass the fully qualified java interface class (only required if Type is JAVA)
    */
   public void createXMLRoute(Project project, String routeName, Type type, String wsdlPath, String wsdlPort,
            String interfaceClass) throws java.io.IOException
   {
      // Gather interface details
      String intfValue;
      if (Type.WSDL.equals(type))
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
      V1ComponentServiceModel service = new V1ComponentServiceModel();
      service.setName(routeName);
      component.addService(service);
      V1InterfaceModel intfModel = new V1InterfaceModel(type.getType());
      intfModel.setInterface(intfValue);
      service.setInterface(intfModel);

      // Create the Camel implementation model and XML route
      V1CamelImplementationModel impl = new V1CamelImplementationModel();
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
