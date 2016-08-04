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

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.switchyard.tools.forge.plugin.AbstractSwitchyardFacet;

/**
 * Forge facet for Clojure implementation.
 *
 * @author Daniel Bevenius
 */
@FacetConstraint({ DependencyFacet.class, PackagingFacet.class })
public class ClojureFacet extends AbstractSwitchyardFacet
{

   private static final String CLOJURE_MAVEN_ID = "org.switchyard.components:switchyard-component-clojure";

   /**
    * Create a new Camel Facet.
    */
   public ClojureFacet()
   {
      super(CLOJURE_MAVEN_ID);
   }

   @Override
   public boolean install()
   {
      installDependencies();
      return true;
   }

}
