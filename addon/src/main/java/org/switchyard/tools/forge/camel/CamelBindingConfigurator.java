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

import java.net.URI;

import org.jboss.forge.addon.projects.Project;
import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

import org.switchyard.component.camel.core.model.CamelCoreNamespace;
import org.switchyard.component.camel.core.model.v1.V1CamelUriBindingModel;

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

      V1CamelUriBindingModel binding = new V1CamelUriBindingModel(CamelCoreNamespace.DEFAULT.uri());
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

      V1CamelUriBindingModel binding = new V1CamelUriBindingModel(CamelCoreNamespace.DEFAULT.uri());
      binding.setConfigURI(URI.create(configURI));

      reference.addBinding(binding);
      switchYard.saveConfig();
   }

}
