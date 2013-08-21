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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.RequiresFacet;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.projects.facets.RequiresPackagingType;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.switchyard.common.type.Classes;
import org.switchyard.common.version.Versions;
import org.switchyard.config.OutputKey;
import org.switchyard.config.model.ModelPuller;
import org.switchyard.config.model.Models;
import org.switchyard.config.model.composite.ComponentModel;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.config.model.composite.ComponentServiceModel;
import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.config.model.composite.v1.V1CompositeModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.config.model.switchyard.v1.V1SwitchYardModel;
import org.switchyard.tools.forge.AbstractSwitchyardFacet;

/**
 * Responsible for common functionality and dependency management for SwitchYard projects. Each component should provide
 * it's own facet implementation and reference the SwitchYard facet using <code>@RequiresFacet</code>.
 */
@RequiresFacet({ DependencyFacet.class, MavenPluginFacet.class, PackagingFacet.class })
@RequiresPackagingType("jar")
public class SwitchYardFacet extends AbstractSwitchyardFacet
{
   private static final Logger logger = Logger.getLogger(SwitchYardFacet.class.getName());
   private static final Log LOG = LogFactory.getLog(SwitchYardFacet.class);

   // repository id of JBoss Nexus repository
   private static final String CONFIG_ATTR = "switchyard.config";

   private static final String SWITCHYARD_PLUGIN =
            "org.switchyard:switchyard-plugin";

   // List of dependencies added to every SwitchYard application
   private static final String[] DEPENDENCIES = new String[] {
            "org.switchyard:switchyard-api",
            "org.switchyard:switchyard-plugin",
            "org.switchyard:switchyard-test"
   };

   // Used if we are dealing with an OpenShift application
   static final String OPEN_SHIFT_PROFILE = "openshift";
   static final String OPEN_SHIFT_TRANSFORM = "/org/switchyard/tools/forge/plugin/openshift.xsl";
   static final String OPEN_SHIFT_CONFIG = ".openshift/config/standalone.xml";

   private static final String JBOSS_NEXUS = "http://repository.jboss.org/nexus/content/groups/public/";

   /**
    * Create a new SwitchYard facet.
    */
   public SwitchYardFacet()
   {
      super(DEPENDENCIES);
   }

   // @Override
   // public void setFaceted(Project origin){
   // LOG.info("Called setFaceted");
   // super.setFaceted(origin);
   // this.install();
   // }

   @Override
   public boolean install()
   {
      logger.info("In SwitchYardFacetInstall");
      // Doing this in a try/finally to set and unset the context class loader
      final ClassLoader orig = Classes.setTCCL(getClass().getClassLoader());
      try
      {
         // TOOD allow user to choose this
         boolean rtnval = performInstall(getFaceted().getFacet(MetadataFacet.class).getProjectName());
         logger.info("Install returning " + rtnval);
         return rtnval;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         return false;
      }
      finally
      {
         Classes.setTCCL(orig);
      }
   }

   @Override
   public boolean isInstalled()
   {
      // checking switchyard.xml existence and if the POM has dependency on switchyard-api
      if (super.isInstalled() && getSwitchYardConfigFile().exists())
      {
         if (getVersion() == null)
         {
            // If it's switchyard project but POM doesn't have ${switchyard.version} property, add it.
            setVersion(Versions.getSwitchYardVersion());
         }
         return true;
      }
      return false;
   }

   /**
    * Save the current SwitchYard configuration model.
    */
   public void saveConfig()
   {
      getFaceted().getFacet(SwitchYardFacet.class).writeSwitchYardConfig();
   }

   /**
    * Retrieve the composite service config for the specified service name.
    * 
    * @param name composite service name
    * @return composite service config fragment, or null if the service does not exist
    */
   public CompositeServiceModel getCompositeService(String name)
   {
      for (CompositeServiceModel service : getSwitchYardConfig().getComposite().getServices())
      {
         if (service.getName().equals(name))
         {
            return service;
         }
      }
      // no matching service
      return null;
   }

   /**
    * Retrieve the component service config for the specified service name.
    * 
    * @param name component service name
    * @return component service config fragment, or null if the service does not exist
    */
   public ComponentServiceModel getComponentService(String name)
   {
      for (ComponentModel component : getMergedSwitchYardConfig().getComposite().getComponents())
      {
         for (ComponentServiceModel service : component.getServices())
         {
            if (service.getName().equals(name))
            {
               return service;
            }
         }
      }
      // no matching service
      return null;
   }

   /**
    * Retrieve the composite reference config for the specified reference name.
    * 
    * @param name composite reference name
    * @return composite reference config fragment, or null if the reference does not exist
    */
   public CompositeReferenceModel getCompositeReference(String name)
   {
      for (CompositeReferenceModel reference : getSwitchYardConfig().getComposite().getReferences())
      {
         if (reference.getName().equals(name))
         {
            return reference;
         }
      }
      // no matching reference
      return null;
   }

   /**
    * Retrieve the component reference config for the specified reference name.
    * 
    * @param name component reference name
    * @return component reference config fragment, or null if the reference does not exist
    */
   public ComponentReferenceModel getComponentReference(String name)
   {
      for (ComponentModel component : getMergedSwitchYardConfig().getComposite().getComponents())
      {
         for (ComponentReferenceModel reference : component.getReferences())
         {
            if (reference.getName().equals(name))
            {
               return reference;
            }
         }
      }
      // no matching reference
      return null;
   }

   /**
    * Returns the editable SwitchYard configuration for the current project. The configuration is loaded if a config
    * property has not been associated with the project, so this method must be synchronized to prevent phantom config
    * references due to concurrent loading.
    * 
    * @return switchyard configuration
    */
   public synchronized SwitchYardModel getSwitchYardConfig()
   {
      SwitchYardModel config = (SwitchYardModel)
               getFaceted().getAttribute(CONFIG_ATTR);
      if (config == null)
      {
         try
         {
            config = readSwitchYardConfig(getSwitchYardConfigFile());
            setSwitchYardConfig(config);
         }
         catch (java.io.IOException ioEx)
         {
            logger.log(Level.SEVERE, "Error while reading SwitchYard configuration: " + ioEx.getMessage());
         }
      }
      config.orderModelChildren();
      return config;
   }

   /**
    * Retrieves a merge of the user config and the generated configuration in
    * <code>target/classes/META-INF/switchyard.xml</code>.
    * 
    * @return merged switchyard configuration
    */
   public SwitchYardModel getMergedSwitchYardConfig()
   {
      SwitchYardModel mergedConfig = null;
      FileResource<?> generatedFile = getGeneratedConfigFile();

      if (generatedFile != null && generatedFile.exists())
      {
         try
         {
            SwitchYardModel generatedConfig = readSwitchYardConfig(generatedFile);
            mergedConfig = Models.merge(generatedConfig, getSwitchYardConfig());
         }
         catch (java.io.IOException ioEx)
         {
            logger.log(Level.SEVERE, "Error while reading SwitchYard configuration: " + ioEx.getMessage());
         }
      }

      // If we don't have a merged config at this point, then we should just
      // return the user configuration
      if (mergedConfig == null)
      {
         mergedConfig = getSwitchYardConfig();
      }

      return mergedConfig;
   }

   void writeSwitchYardConfig()
   {
      FileResource<?> configFile = getSwitchYardConfigFile();
      SwitchYardModel config = getSwitchYardConfig();
      FileOutputStream fos = null;
      try
      {
         fos = new FileOutputStream(configFile.getUnderlyingResourceObject());
         config.write(fos, OutputKey.PRETTY_PRINT);
      }
      catch (java.io.IOException ioEx)
      {
         logger.log(Level.SEVERE, "Error while saving SwitchYard configuration: " + ioEx.getMessage());
      }
      finally
      {
         if (fos != null)
         {
            try
            {
               fos.close();
            }
            catch (java.io.IOException ioEx)
            {
               return;
            }
         }
      }
   }

   private void addNexusRepository()
   {
      DependencyFacet deps = getFaceted().getFacet(DependencyFacet.class);
      deps.addRepository("JBOSS_NEXUS", JBOSS_NEXUS);
   }

   private void addPluginRepository()
   {
      MavenPluginFacet plugins = getFaceted().getFacet(MavenPluginFacet.class);
      plugins.addPluginRepository("JBOSS_NEXUS", JBOSS_NEXUS);
   }

   private void tweakForOpenShift() throws Exception
   {
      // Check to see if this is an openshift app
      if (!(new File(OPEN_SHIFT_CONFIG).exists()))
      {
         return;
      }

      // NEXUS repository definition is missing for OS apps
      addNexusRepository();

      // update standalone.xml to include switchyard bits
      addSwitchYardToASConfig(OPEN_SHIFT_CONFIG);

      // add a build section with the scanner plugins
      MavenFacet mvn = getFaceted().getFacet(MavenFacet.class);
      Model pom = mvn.getPOM();
      List<Profile> profiles = new ArrayList<Profile>();
      profiles.add(buildOpenShiftProfile());
      pom.setProfiles(profiles);
      mvn.setPOM(pom);
   }

   /**
    * Adds SwitchYard scanners to the build section of a pom.
    * 
    * @param pom the pom to update
    * @param profile the profile to update; a null parameter will simply update the main <build> section.
    * @throws Exception adding scanners failed
    */
   private void addScannerPlugin(Model pom, Profile profile) throws Exception
   {
      String version = getFaceted().getFacet(MetadataFacet.class).getEffectiveProperty(VERSION);
      Dependency dep = DependencyBuilder.create(SWITCHYARD_PLUGIN + ":" + version);
      org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();

      plugin.setArtifactId(dep.getCoordinate().getArtifactId());
      plugin.setGroupId(dep.getCoordinate().getGroupId());
      plugin.setVersion("${" + VERSION + "}");

      // This is terrible - find a better way to set the config
      String pluginConfig =
               "<configuration>"
                        + "<scannerClassNames>"
                        + "<param>org.switchyard.component.bean.config.model.BeanSwitchYardScanner</param>"
                        + "<param>org.switchyard.component.camel.config.model.RouteScanner</param>"
                        + "<param>org.switchyard.component.bpm.config.model.BPMSwitchYardScanner</param>"
                        + "<param>org.switchyard.component.rules.config.model.RulesSwitchYardScanner</param>"
                        + "<param>org.switchyard.transform.config.model.TransformSwitchYardScanner</param>"
                        + "<param>org.switchyard.transform.config.model.ValidateSwitchYardScanner</param>"
                        + "</scannerClassNames>"
                        + "</configuration>";
      Xpp3Dom dom = Xpp3DomBuilder.build(new ByteArrayInputStream(pluginConfig.getBytes()),
               "UTF-8");

      List<PluginExecution> executions = plugin.getExecutions();
      PluginExecution execution = new PluginExecution();
      execution.addGoal("configure");
      execution.setConfiguration(dom);
      executions.add(execution);

      // add to the build section
      if (profile == null)
      {
         if (pom.getBuild() == null)
         {
            pom.setBuild(new org.apache.maven.model.Build());
         }
         pom.getBuild().getPlugins().add(plugin);
      }
      else
      {
         List<Plugin> plugins = profile.getBuild().getPlugins();
         plugins.add(plugin);
         profile.getBuild().setPlugins(plugins);
      }
   }

   private boolean performInstall(String appName)
   {
      logger.info("Called PerformInstall");
      try
      {
         tweakForOpenShift();
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Unable to configure application for OpenShift: " + ex.getMessage());
         return false;
      }

      // Update the project with version and dependency info
      setVersion(Versions.getSwitchYardVersion());
      installDependencies();
      addPluginRepository();

      logger.log(Level.SEVERE, "Application name (e.g. myApp)");

      try
      {
         MavenFacet mvn = getFaceted().getFacet(MavenFacet.class);
         Model pom = mvn.getPOM();

         addScannerPlugin(pom, null);
         mvn.setPOM(pom);

         // Create the initial SwitchYard configuration
         V1SwitchYardModel syConfig = new V1SwitchYardModel();
         V1CompositeModel composite = new V1CompositeModel();
         composite.setName(appName);
         composite.setTargetNamespace("urn:switchyard:application:" + appName);
         syConfig.setComposite(composite);

         // Attach a reference to the current config to the current project
         setSwitchYardConfig(syConfig);
         // Save an initial version of the config
         writeSwitchYardConfig();
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         return false;
      }
      return true;
   }

   // Creates a new OpenShift profile with correct SwitchYard
   Profile buildOpenShiftProfile() throws Exception
   {
      Model pom = getFaceted().getFacet(MavenFacet.class).getPOM();
      Profile profile = new Profile();
      profile.setId(OPEN_SHIFT_PROFILE);

      Build build = new Build();
      build.setFinalName(pom.getArtifactId());
      profile.setBuild(build);
      addScannerPlugin(pom, profile);
      Plugin jarPlugin = new Plugin();
      jarPlugin.setGroupId("org.apache.maven.plugins");
      jarPlugin.setArtifactId("maven-jar-plugin");
      jarPlugin.setVersion("2.3.1");
      String pluginConfig =
               "<configuration>"
                        + "<outputDirectory>deployments</outputDirectory>"
                        + "</configuration>";
      Xpp3Dom dom = Xpp3DomBuilder.build(new ByteArrayInputStream(pluginConfig.getBytes()), "UTF-8");
      jarPlugin.setConfiguration(dom);
      build.addPlugin(jarPlugin);

      return profile;
   }

   // This method updates the config @ asConfigPath with required switchyard
   // subsystem details for OpenShift
   public void addSwitchYardToASConfig(String asConfigPath) throws Exception
   {
      File orig = new File(asConfigPath);
      // create a backup
      if (!orig.exists())
      {
         throw new Exception("standalone.xml not available at " + orig.getAbsolutePath());
      }
      File backup = new File(asConfigPath + ".orig");
      int i = 1;
      while (backup.exists())
      {
         backup = new File(asConfigPath + ".orig." + i++);
      }
      if (!orig.renameTo(backup))
      {
         throw new Exception("Failed to create backup standalone.xml " + backup.getAbsolutePath());
      }
      orig.createNewFile();

      // transform the original
      InputStream xslStream = null;
      try
      {
         Transformer t = TransformerFactory.newInstance().newTransformer(
                  new StreamSource(Classes.getResourceAsStream(OPEN_SHIFT_TRANSFORM)));
         t.transform(new StreamSource(backup), new StreamResult(orig));
      }
      finally
      {
         if (xslStream != null)
         {
            xslStream.close();
         }
      }
   }

   private SwitchYardModel readSwitchYardConfig(FileResource<?> file) throws java.io.IOException
   {
      return new ModelPuller<SwitchYardModel>().pull(file.getUnderlyingResourceObject());
   }

   /**
    * Any write activity to the project's config property should be synchronized.
    */
   private synchronized void setSwitchYardConfig(SwitchYardModel config)
   {
      getFaceted().setAttribute(CONFIG_ATTR, config);
   }

   private FileResource<?> getSwitchYardConfigFile()
   {
      DirectoryResource metaInf = getFaceted().getProjectRoot().getChildDirectory(
               "src"
                        + File.separator + "main"
                        + File.separator + "resources"
                        + File.separator + "META-INF");
      if (!metaInf.exists())
      {
         metaInf.mkdirs();
      }
      return (FileResource<?>) metaInf.getChild("switchyard.xml");
   }

   private FileResource<?> getGeneratedConfigFile()
   {
      FileResource<?> generatedConfig = null;
      DirectoryResource metaInf = getFaceted().getProjectRoot().getChildDirectory(
               "target"
                        + File.separator + "classes"
                        + File.separator + "META-INF");

      if (metaInf != null && metaInf.exists())
      {
         generatedConfig = (FileResource<?>) metaInf.getChild("switchyard.xml");
      }

      return generatedConfig;
   }
}
