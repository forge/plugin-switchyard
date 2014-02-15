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
package org.switchyard.tools.forge.plugin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;

/**
 * Base implementation for SwitchYard facets.
 */
public abstract class AbstractSwitchyardFacet extends AbstractFacet<Project> implements ProjectFacet
{
   /**
    * Property name used in POMs to identify SwitchYard version.
    */
   public static final String VERSION = "switchyard.version";

   // List of dependencies added to every SwitchYard application
   private List<String> _depends = new LinkedList<String>();

   /**
    * Constructor method.
    * @param dependencies dependencies
 	*/
   protected AbstractSwitchyardFacet(String... dependencies)
   {
      if (dependencies != null && dependencies.length > 0)
      {
         _depends = Arrays.asList(dependencies);
      }
   }

   /**
    * Install dependencies.
 	*/
   protected void installDependencies()
   {
      DependencyFacet deps = getFaceted().getFacet(DependencyFacet.class);
      if (!_depends.isEmpty())
      {
         // Add base required dependencies
         for (String artifact : _depends)
         {
            DependencyBuilder dep = DependencyBuilder.create(artifact + ":${" + VERSION + "}");
            deps.addDirectDependency(dep);
         }
      }
   }

   /**
    * Returns whether the facet is installed.
    * @return installed status
 	*/
   @Override
   public boolean isInstalled()
   {
      boolean installed = false;
      // If the first dependency is present then we assume the facet is installed
      if (!_depends.isEmpty())
      {
         Dependency dep = DependencyBuilder.create(_depends.get(0));
         String packagingType = getFaceted().getFacet(PackagingFacet.class).getPackagingType();
         installed = getFaceted().getFacet(DependencyFacet.class).hasDirectDependency(dep)
                  && "jar".equalsIgnoreCase(packagingType);
      }
      return installed;
   }

   /**
    * Get the version of SwitchYard used by the application.
    * 
    * @return SwitchYard version
    */
   public String getVersion()
   {
      return getFaceted().getFacet(MetadataFacet.class).getEffectiveProperty(VERSION);
   }

   /**
    * Get the version of SwitchYard used by the application.
    * 
    * @param version SwitchYard version
    */
   public void setVersion(String version)
   {
      getFaceted().getFacet(MetadataFacet.class).setDirectProperty(VERSION, version);
   }
}
