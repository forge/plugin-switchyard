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
package org.switchyard.tools.forge.clojure;

import org.jboss.forge.addon.projects.Project;
import org.switchyard.component.clojure.config.model.ClojureComponentImplementationModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.config.model.switchyard.SwitchYardNamespace;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Commands related to Clojure services.
 * 
 * @author Daniel Bevenius
 */
public class ClojureImplementationConfigurator
{

   /**
    * Create a new Clojure implementation service.
    * 
    * @param serviceName The SwitchYard service name.
    * @param inlineScript Path to the Clojure script to inline
    * @param emptyInlineScript Create an empty 'script' element
    * @param externalScriptPath Path to the external Clojure script
    * @param emptyExternalScriptPath Create an empty 'scriptFile' element
    * @param injectExchange Inject the SwitchYard Exchange object into the Clojure script
    * @param out shell output.
    */
   public void newImplementation(
            Project project,
            final String serviceName,
            final String inlineScript,
            final boolean emptyInlineScript,
            final String externalScriptPath,
            final boolean emptyExternalScriptPath,
            final boolean injectExchange)
   {

      final ClojureComponentImplementationModel impl = createImplModel(inlineScript, emptyInlineScript,
               externalScriptPath, emptyExternalScriptPath, injectExchange);
      final V1ComponentModel component = createComponentModel(serviceName);
      component.setImplementation(impl);
      saveSwitchYardModel(project, component);
   }

   private ClojureComponentImplementationModel createImplModel(final String inlineScript,
            final boolean emptyInlineScript,
            final String externalScriptPath,
            final boolean emptyExternalScriptPath,
            final boolean injectExchange)
   {
      try
      {
         return new ClojureModelBuilder()
                  .inlineScript(inlineScript)
                  .emptyInlineScript(emptyInlineScript)
                  .externalScriptPath(externalScriptPath)
                  .emptyExternalScriptPath(emptyExternalScriptPath)
                  .injectExchange(injectExchange)
                  .build();
      }
      catch (final ClojureBuilderException e)
      {
         throw new RuntimeException(e.getMessage(), e);
      }
   }

   private void saveSwitchYardModel(Project project, final V1ComponentModel component)
   {
      final SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
      final SwitchYardModel syConfig = switchYard.getSwitchYardConfig();
      syConfig.getComposite().addComponent(component);
      switchYard.saveConfig();
   }

   private V1ComponentModel createComponentModel(final String serviceName)
   {
      final V1ComponentModel component = new V1ComponentModel();
      component.setName(serviceName + "Component");
      final V1ComponentServiceModel service = new V1ComponentServiceModel(SwitchYardNamespace.DEFAULT.uri());
      service.setName(serviceName);
      component.addService(service);
      return component;
   }

}
