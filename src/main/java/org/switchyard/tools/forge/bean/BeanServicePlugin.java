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

package org.switchyard.tools.forge.bean;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.ResourceFacet;
import org.switchyard.tools.forge.plugin.TemplateResource;

/**
 * Forge plugin for Bean component commands.
 */
public class BeanServicePlugin
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
               project.getFacet(ResourceFacet.class), pkgName, serviceName, false);

      TemplateResource beanImpl = new TemplateResource(BEAN_IMPLEMENTATION_TEMPLATE);
      beanImpl.serviceName(serviceName);
      String implementationFile = beanImpl.writeJavaSource(
               project.getFacet(ResourceFacet.class), pkgName, serviceName + "Bean", false);
   }
}
