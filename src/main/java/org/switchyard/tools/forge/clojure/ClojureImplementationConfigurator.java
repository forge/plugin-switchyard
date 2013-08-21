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

package org.switchyard.tools.forge.clojure;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.furnace.services.Exported;
import org.switchyard.component.clojure.config.model.ClojureComponentImplementationModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Commands related to Clojure services.
 * 
 * @author Daniel Bevenius
 */
@Exported
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
      final V1ComponentServiceModel service = new V1ComponentServiceModel();
      service.setName(serviceName);
      component.addService(service);
      return component;
   }

}
