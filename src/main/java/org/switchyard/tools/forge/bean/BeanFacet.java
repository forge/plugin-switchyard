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

import java.io.File;

import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.PackagingFacet;
import org.jboss.forge.project.packaging.PackagingType;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.RequiresPackagingType;
import org.switchyard.tools.forge.AbstractFacet;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Forge facet for Bean services.
 */
@Alias("switchyard.bean")
@RequiresFacet({ DependencyFacet.class, PackagingFacet.class, SwitchYardFacet.class})
@RequiresPackagingType(PackagingType.JAR)
public class BeanFacet extends AbstractFacet {
    
    private static final String BEAN_MAVEN_ID = 
        "org.switchyard.components:switchyard-component-bean";

    /**
     * Create a new BeanFacet.
     */
    public BeanFacet() {
        super(BEAN_MAVEN_ID);
    }
    
    @Override
    public boolean install() {
        installDependencies();
        createCDIDescriptor();
        return true;
    }
    
    private void createCDIDescriptor() {
        DirectoryResource metaInf = project.getProjectRoot().getChildDirectory(
                "src" 
                + File.separator + "main" 
                + File.separator + "resources"
                + File.separator + "META-INF");
        

        FileResource<?> beansXml = (FileResource<?>) metaInf.getChild("beans.xml");
        beansXml.setContents("");
    }
}
