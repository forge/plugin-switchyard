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

package org.switchyard.tools.forge;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
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

   protected AbstractSwitchyardFacet(String... dependencies)
   {
      if (dependencies != null && dependencies.length > 0)
      {
         _depends = Arrays.asList(dependencies);
      }
   }

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
      return getFaceted().getFacet(DependencyFacet.class).getProperty(VERSION);
   }

   /**
    * Get the version of SwitchYard used by the application.
    * 
    * @param version SwitchYard version
    */
   public void setVersion(String version)
   {
      getFaceted().getFacet(DependencyFacet.class).setProperty(VERSION, version);
   }
}
