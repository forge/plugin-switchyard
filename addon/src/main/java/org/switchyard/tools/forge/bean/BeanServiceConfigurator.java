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
package org.switchyard.tools.forge.bean;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.switchyard.tools.forge.plugin.TemplateResource;

/**
 * Forge plugin for Bean component commands.
 */
public class BeanServiceConfigurator
{

   // Template files used for bean services
   private static final String BEAN_INTERFACE_TEMPLATE = "/org/switchyard/tools/forge/bean/BeanInterfaceTemplate.java";
   private static final String BEAN_IMPLEMENTATION_TEMPLATE = "/org/switchyard/tools/forge/bean/BeanImplementationTemplate.java";

   /**
    * Create a new Bean service interface and implementation.
    * 
    * @param serviceName service name
    * @param out shell output
    * @throws java.io.IOException trouble reading/writing SwitchYard config
    */
   public void newBean(Project project, String pkgName, final String serviceName) throws java.io.IOException
   {

      // Create the service interface and implementation
      TemplateResource beanIntf = new TemplateResource(BEAN_INTERFACE_TEMPLATE);
      beanIntf.serviceName(serviceName);
      String interfaceFile = beanIntf.writeJavaSource(
               project.getFacet(ResourcesFacet.class), pkgName, serviceName, false);
      TemplateResource beanImpl = new TemplateResource(BEAN_IMPLEMENTATION_TEMPLATE);
      beanImpl.serviceName(serviceName);
      String implementationFile = beanImpl.writeJavaSource(
               project.getFacet(ResourcesFacet.class), pkgName, serviceName + "Bean", false);
   }
}
