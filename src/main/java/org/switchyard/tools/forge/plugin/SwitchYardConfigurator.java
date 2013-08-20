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
package org.switchyard.tools.forge.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.furnace.services.Exported;
import org.switchyard.config.model.composite.ComponentModel;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.config.model.composite.CompositeModel;
import org.switchyard.config.model.composite.v1.V1ComponentReferenceModel;
import org.switchyard.config.model.composite.v1.V1CompositeReferenceModel;
import org.switchyard.config.model.composite.v1.V1CompositeServiceModel;
import org.switchyard.config.model.composite.v1.V1InterfaceModel;
import org.switchyard.config.model.domain.DomainModel;
import org.switchyard.config.model.domain.HandlerModel;
import org.switchyard.config.model.domain.HandlersModel;
import org.switchyard.config.model.domain.v1.V1DomainModel;
import org.switchyard.config.model.domain.v1.V1HandlerModel;
import org.switchyard.config.model.domain.v1.V1HandlersModel;
import org.switchyard.config.model.switchyard.ArtifactModel;
import org.switchyard.config.model.switchyard.ArtifactsModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.config.model.switchyard.v1.V1ArtifactModel;
import org.switchyard.config.model.switchyard.v1.V1ArtifactsModel;
import org.switchyard.tools.forge.camel.CamelServiceConfigurator.Type;

/**
 * Project-level commands for SwitchYard applications.
 */
@Exported
public class SwitchYardConfigurator
{

   // Directory where artifacts are stored
   private static final String ARTIFACT_DIR = "lib";
   // Template file used for unit testing services
   // private static final String TEST_SERVICE_TEMPLATE = "java/TestTemplate.java";
   private static final String TEST_SERVICE_TEMPLATE = "/org/switchyard/tools/forge/plugin/TestTemplate.java";
   // MessageTrace handler name and class
   private static final String TRACE_CLASS = "org.switchyard.handlers.MessageTrace";
   private static final String TRACE_NAME = "MessageTrace";

   /**
    * Print SwitchYard version used in this application.
    * 
    * @param out shell output
    */
   public String getSwitchyardVersion(Project project)
   {
      return project.getFacet(SwitchYardFacet.class).getVersion();
   }

   /**
    * Promote a component-level service to a composite-level service.
    * 
    * @param serviceName name of the service to promote
    * @param out shell output
    */
   public void promoteService(Project project, final String serviceName)
   {

      SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);

      // Check to see if the service is already promoted
      if (switchYard.getCompositeService(serviceName) != null)
      {
         return;
      }

      // Make sure a component service exists
      if (switchYard.getComponentService(serviceName) == null)
      {
         throw new IllegalArgumentException("Component service name not found: " + serviceName);
      }

      // Create the composite service
      V1CompositeServiceModel service = new V1CompositeServiceModel();
      service.setName(serviceName);
      service.setPromote(serviceName);
      switchYard.getSwitchYardConfig().getComposite().addService(service);

      // Save configuration changes
      switchYard.saveConfig();
   }

   /**
    * Promote a component-level reference to a composite-level reference.
    * 
    * @param referenceName name of the reference to promote
    * @param out shell output
    */
   public void promoteReference(Project project, final String referenceName)
   {

      SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);

      // Check to see if the service is already promoted
      if (switchYard.getCompositeReference(referenceName) != null)
      {
         return;
      }
      // Make sure a component service exists
      ComponentReferenceModel component = switchYard.getComponentReference(referenceName);
      if (component == null)
      {
         throw new IllegalArgumentException("Component reference not found: " + referenceName);
      }
      // Create the composite service
      V1CompositeReferenceModel reference = new V1CompositeReferenceModel();
      reference.setName(referenceName);
      reference.setPromote(component.getComponent().getName() + "/" + referenceName);
      switchYard.getSwitchYardConfig().getComposite().addReference(reference);

      // Save configuration changes
      switchYard.saveConfig();
   }

   /**
    * Add a unit test for a service.
    * 
    * @param serviceName name of the service to test
    * @param out shell output
    * @throws java.io.IOException failed to create unit test file
    */
   public JavaResource createServiceTest(Project project, final String serviceName, String pkgName)
            throws java.io.IOException
   {
      if (pkgName == null)
      {
         pkgName = project.getFacet(MetadataFacet.class).getTopLevelPackage() + ".services";
      }

      TemplateResource template = new TemplateResource(TEST_SERVICE_TEMPLATE);
      template.serviceName(serviceName);
      String testFile = template.writeJavaSource(project.getFacet(ResourcesFacet.class),
               pkgName, serviceName + "Test", true);
      return project.getFacet(JavaSourceFacet.class).getTestJavaResource(testFile);
   }

   /**
    * Adds or removes the message trace handler based on message tracing preference.
    * 
    * @param enable true to enable tracing, false to disable
    * @param out shell output
    */
   public void traceMessages(Project project, final boolean enable)
   {

      SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
      DomainModel domain = switchYard.getSwitchYardConfig().getDomain();
      String result;

      // If enable option is not specified or enable=true, then enable the MessageTrace handler
      if (enable)
      {
         // create the domain config if it doesn't exist already
         if (domain == null)
         {
            domain = new V1DomainModel();
            switchYard.getSwitchYardConfig().setDomain(domain);
         }
         // need to create the handlers config if it's not already present
         HandlersModel handlers = domain.getHandlers();
         if (handlers == null)
         {
            handlers = new V1HandlersModel();
            domain.setHandlers(handlers);
         }
         handlers.addHandler(new V1HandlerModel()
                  .setClassName(TRACE_CLASS)
                  .setName(TRACE_NAME));
         result = "Message tracing has been enabled.";
      }
      else
      {
         // Disable the handler by removing the configuration
         if (domain != null && domain.getHandlers() != null)
         {
            for (HandlerModel handler : domain.getHandlers().getHandlers())
            {
               if (TRACE_CLASS.equals(handler.getClass())
                        && TRACE_NAME.equals(handler.getName()))
               {
                  domain.getHandlers().removeHandler(TRACE_NAME);
               }
            }
         }
         result = "Message tracing has been disabled.";
      }

      // Save configuration changes
      switchYard.saveConfig();
   }

   /**
    * Import the specified artifact into the application project.
    * 
    * @param urlStr url for the artifact module
    * @param name name of the artifact module
    * @param download true will attempt download of the artifact module
    * @param out shell output
    */
   public void importArtifacts(Project project, final String urlStr, final String name, final boolean download)
   {
      SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);

      URL url;
      try
      {
         url = new URL(urlStr);
      }
      catch (Exception ex)
      {
         throw new IllegalArgumentException("Invalid Artifact URL: " + urlStr + ", " + ex.toString());
      }

      // Download the artifact if requested
      if (download)
      {
         try
         {
            File artifactDir = new File(ARTIFACT_DIR);
            if (!artifactDir.exists())
            {
               artifactDir.mkdirs();
            }
            // detect if this is a Guvnor repository
            if (url.getProtocol().contains("http") && url.getPath().contains("rest/packages"))
            {
               url = new URL(url.toString() + "/binary");
            }
            File artifactFile = new File(artifactDir, name + ".jar");
            streamToFile(url.openStream(), artifactFile);

         }
         catch (Exception ex)
         {
            throw new IllegalArgumentException("Invalid Artifact URL: " + urlStr + ", " + ex.toString());
         }
      }

      // update config
      ArtifactsModel artifacts = switchYard.getSwitchYardConfig().getArtifacts();
      if (artifacts == null)
      {
         artifacts = new V1ArtifactsModel();
         switchYard.getSwitchYardConfig().setArtifacts(artifacts);
      }
      ArtifactModel artifact = new V1ArtifactModel();
      artifact.setName(name);
      artifact.setURL(urlStr);
      artifacts.addArtifact(artifact);
      switchYard.saveConfig();
   }

   // This method reads from the source stream and writes to the specified path.
   // Both the input and output streams are closed by this method.
   private void streamToFile(InputStream stream, File filePath) throws Exception
   {
      if (filePath.exists())
      {
         throw new Exception("File already exists: " + filePath);
      }

      FileOutputStream fos = null;
      try
      {
         fos = new FileOutputStream(filePath);
         int count;
         byte[] buf = new byte[8192];
         while ((count = stream.read(buf)) != -1)
         {
            fos.write(buf, 0, count);
         }
      }
      finally
      {
         if (fos != null)
         {
            fos.close();
         }
         if (stream != null)
         {
            stream.close();
         }
      }
   }

   /**
    * Add a component-level reference to a given service.
    * 
    * @param referenceName the name of the reference being created
    * @param interfaceType possible values: wsdl, java
    * @param interfaze The interface of the reference
    * @param componentName the name of the component the reference will be applied to
    * @param out shell output
    */
   public void addReference(Project project, final String referenceName, final Type type, final String interfaze,
            final String componentName)
   {

      SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);

      // Make sure the source component service exists
      ComponentModel sourceComponent = null;
      CompositeModel composite = switchYard.getMergedSwitchYardConfig().getComposite();
      if (composite != null)
      {
         Iterator<ComponentModel> components = composite.getComponents().iterator();
         while (components.hasNext())
         {
            ComponentModel auxComponent = components.next();
            if (auxComponent.getName().equals(componentName))
            {
               sourceComponent = auxComponent;
               break;
            }
         }
      }
      if (sourceComponent == null)
      {
         throw new IllegalStateException("Component not found: " + componentName);
      }
      else
      {
         // Check the reference name is not already present in this component
         Iterator<ComponentReferenceModel> references = sourceComponent.getReferences().iterator();
         while (references.hasNext())
         {
            ComponentReferenceModel reference = references.next();
            if (reference.getName().equals(referenceName))
            {
               throw new IllegalStateException("A reference named " + referenceName + " already exists in "
                        + componentName);
            }
         }
      }

      addComponentReference(switchYard, componentName, referenceName, type, interfaze);
   }

   private void addComponentReference(SwitchYardFacet switchYard, String componentName, String referenceName,
            Type interfaceType, String interfaze)
   {

      ComponentReferenceModel reference = new V1ComponentReferenceModel();
      reference.setName(referenceName);
      V1InterfaceModel referenceInterfaceModel = new V1InterfaceModel(interfaceType.getType());
      referenceInterfaceModel.setInterface(interfaze);
      reference.setInterface(referenceInterfaceModel);

      SwitchYardModel userConfig = switchYard.getSwitchYardConfig();
      boolean isComponentInUserConfig = false;
      for (Iterator<ComponentModel> userConfigComponents = userConfig.getComposite().getComponents().iterator(); userConfigComponents
               .hasNext();)
      {
         ComponentModel componentModel = userConfigComponents.next();
         if (componentModel.getName().equals(componentName))
         {

            // The component is already in the user config. Let's just add the reference to it
            componentModel.addReference(reference);
            isComponentInUserConfig = true;
            break;
         }
      }

      if (!isComponentInUserConfig)
      {
         // The component is not in the user config. Let's: 1) get it from the merged config, 2) add the reference into
         // it, and
         // finally 3) save the component into the userConfig
         SwitchYardModel mergedConfig = switchYard.getMergedSwitchYardConfig();
         for (ComponentModel componentModel : mergedConfig.getComposite().getComponents())
         {
            if (componentModel.getName().equals(componentName))
            {
               componentModel.addReference(reference);
               userConfig.getComposite().addComponent(componentModel);
               break;
            }
         }
      }

      switchYard.saveConfig();
   }

}
