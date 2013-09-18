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

import java.net.URI;

import org.jboss.forge.addon.projects.Project;
import org.switchyard.component.camel.core.model.v1.V1CamelBindingModel;
import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Forge plugin for Camel binding commands.
 */

public class CamelBindingConfigurator
{

   /**
    * Bind a promoted service using the Camel binding.
    * 
    * @param serviceName name of the service to bind
    * @param configURI camel endpoint URI
    * @param operationName target operation name for the SwitchYard service
    * @param out shell output
    */
   public void bindService(
            Project project,
            final String serviceName,
            final String configURI,
            final String operationName
            )
   {

      SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
      CompositeServiceModel service = switchYard.getCompositeService(serviceName);
      // Check to see if the service is public
      if (service == null)
      {
         throw new IllegalArgumentException("No public service named: " + serviceName);
      }

      V1CamelBindingModel binding = new V1CamelBindingModel();
      binding.setConfigURI(URI.create(configURI));
      service.addBinding(binding);
      switchYard.saveConfig();
   }

   /**
    * Bind a promoted reference using the Camel binding.
    * 
    * @param referenceName name of the reference to bind
    * @param configURI camel endpoint URI
    * @param out shell output
    */
   public void bindReference(
            Project project,
            final String referenceName,
            final String configURI
            )
   {

      SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
      CompositeReferenceModel reference = switchYard.getCompositeReference(referenceName);
      // Check to see if the reference is public
      if (reference == null)
      {
         throw new IllegalArgumentException("No public reference named: " + referenceName);
      }

      V1CamelBindingModel binding = new V1CamelBindingModel();
      binding.setConfigURI(URI.create(configURI));

      reference.addBinding(binding);
      switchYard.saveConfig();
   }

}
