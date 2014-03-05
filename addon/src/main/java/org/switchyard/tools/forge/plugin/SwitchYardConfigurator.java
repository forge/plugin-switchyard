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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.switchyard.config.model.composite.BindingModel;
import org.switchyard.config.model.composite.ComponentModel;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.config.model.composite.ComponentServiceModel;
import org.switchyard.config.model.composite.CompositeModel;
import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.config.model.composite.v1.V1ComponentReferenceModel;
import org.switchyard.config.model.composite.v1.V1CompositeReferenceModel;
import org.switchyard.config.model.composite.v1.V1CompositeServiceModel;
import org.switchyard.config.model.composite.v1.V1InterfaceModel;
import org.switchyard.config.model.domain.DomainModel;
import org.switchyard.config.model.domain.v1.V1DomainModel;
import org.switchyard.config.model.property.PropertiesModel;
import org.switchyard.config.model.property.PropertyModel;
import org.switchyard.config.model.property.v1.V1PropertiesModel;
import org.switchyard.config.model.property.v1.V1PropertyModel;
import org.switchyard.config.model.selector.JavaOperationSelectorModel;
import org.switchyard.config.model.selector.OperationSelectorModel;
import org.switchyard.config.model.selector.RegexOperationSelectorModel;
import org.switchyard.config.model.selector.StaticOperationSelectorModel;
import org.switchyard.config.model.selector.XPathOperationSelectorModel;
import org.switchyard.config.model.selector.v1.V1JavaOperationSelectorModel;
import org.switchyard.config.model.selector.v1.V1RegexOperationSelectorModel;
import org.switchyard.config.model.selector.v1.V1StaticOperationSelectorModel;
import org.switchyard.config.model.selector.v1.V1XPathOperationSelectorModel;
import org.switchyard.config.model.switchyard.ArtifactModel;
import org.switchyard.config.model.switchyard.ArtifactsModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.config.model.switchyard.SwitchYardNamespace;
import org.switchyard.config.model.switchyard.v1.V1ArtifactModel;
import org.switchyard.config.model.switchyard.v1.V1ArtifactsModel;
import org.switchyard.config.model.transform.TransformModel;
import org.switchyard.config.model.transform.v1.V1TransformsModel;
import org.switchyard.config.model.validate.ValidateModel;
import org.switchyard.config.model.validate.v1.V1ValidatesModel;
import org.switchyard.policy.Policy;
import org.switchyard.tools.forge.plugin.OperationSelectorTypes;
import org.switchyard.tools.forge.plugin.PolicyPlacementLocations;
import org.switchyard.tools.forge.plugin.TransformerTypes;
import org.switchyard.tools.forge.plugin.ValidatorTypes;
import org.switchyard.tools.forge.camel.InterfaceTypes;
import org.switchyard.transform.config.model.JavaTransformModel;
import org.switchyard.transform.config.model.SmooksTransformModel;
import org.switchyard.transform.config.model.TransformNamespace;
import org.switchyard.transform.config.model.XsltTransformModel;
import org.switchyard.transform.config.model.v1.V1JAXBTransformModel;
import org.switchyard.transform.config.model.v1.V1JSONTransformModel;
import org.switchyard.transform.config.model.v1.V1JavaTransformModel;
import org.switchyard.transform.config.model.v1.V1SmooksTransformModel;
import org.switchyard.transform.config.model.v1.V1XsltTransformModel;
import org.switchyard.validate.config.model.FileEntryModel;
import org.switchyard.validate.config.model.JavaValidateModel;
import org.switchyard.validate.config.model.SchemaCatalogsModel;
import org.switchyard.validate.config.model.SchemaFilesModel;
import org.switchyard.validate.config.model.ValidateNamespace;
import org.switchyard.validate.config.model.XmlSchemaType;
import org.switchyard.validate.config.model.XmlValidateModel;
import org.switchyard.validate.config.model.v1.V1FileEntryModel;
import org.switchyard.validate.config.model.v1.V1JavaValidateModel;
import org.switchyard.validate.config.model.v1.V1SchemaCatalogsModel;
import org.switchyard.validate.config.model.v1.V1SchemaFilesModel;
import org.switchyard.validate.config.model.v1.V1XmlValidateModel;

/**
 * Project-level commands for SwitchYard applications.
 */
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
   // MessageTrace handler name and class
   private static final String TRACE_PROPERTY = "org.switchyard.handlers.messageTrace.enabled";

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
   public String traceMessages(Project project, final Boolean enable)
   {

      SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
      SwitchYardModel switchYardConfig = switchYard.getSwitchYardConfig();
      String switchYardNamespace = getSwitchYardNamespace(switchYardConfig);
      DomainModel domain = switchYard.getSwitchYardConfig().getDomain();
      String result;

      // If enable option is not specified or enable=true, then enable the MessageTrace handler
      if (enable == null || enable) {
          // create the domain config if it doesn't exist already
          if (domain == null) {
              domain = new V1DomainModel(switchYardNamespace);
              switchYardConfig.setDomain(domain);
          }
          // need to create the properties config if it's not already present
          PropertiesModel properties = domain.getProperties();
          if (properties == null) {
              properties = new V1PropertiesModel(switchYardNamespace);
              domain.setProperties(properties);
          }
          PropertyModel property = properties.getProperty(TRACE_PROPERTY);
          if (property == null) {
        	  property = new V1PropertyModel(switchYardNamespace);
              property.setName(TRACE_PROPERTY);
              properties.addProperty(property);
          }
          property.setValue("true");
          result = "Message tracing has been enabled.";
      } else {
          // Disable the handler by removing the configuration
          if (domain != null) {
              PropertiesModel properties = domain.getProperties();
              if (properties != null) {
                  PropertyModel property = properties.getProperty(TRACE_PROPERTY);
                  if (property != null) {
                	  property.setValue("false");
                  }
              }
          }
          result = "Message tracing has been disabled.";
      }

      // Save configuration changes
      switchYard.saveConfig();
      return result;
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
      SwitchYardModel switchYardConfig = switchYard.getSwitchYardConfig();
      String switchYardNamespace = getSwitchYardNamespace(switchYardConfig);
      ArtifactsModel artifacts = switchYardConfig.getArtifacts();
      if (artifacts == null)
      {
          artifacts = new V1ArtifactsModel(switchYardNamespace);
          switchYard.getSwitchYardConfig().setArtifacts(artifacts);
      }
      ArtifactModel artifact = new V1ArtifactModel(switchYardNamespace);
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
   public void addReference(Project project, final String referenceName, final String interfaceType, final String interfaze,
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

      addComponentReference(switchYard, componentName, referenceName, interfaceType, interfaze);
   }

   private void addComponentReference(SwitchYardFacet switchYard, String componentName, String referenceName,
            String interfaceType, String interfaze)
   {

      ComponentReferenceModel reference = new V1ComponentReferenceModel(SwitchYardNamespace.DEFAULT.uri());
      reference.setName(referenceName);
      V1InterfaceModel referenceInterfaceModel = new V1InterfaceModel(interfaceType);
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
   
   /**
    * Add a Java Transformer.
    * @param project project
    * @param from Transform from (QName)
    * @param to Transform to (QName)
    * @param clazz Java class name
    */
   public void addJavaTransformer(Project project, String from, String to, String clazz) {
       JavaTransformModel javaTransform = new V1JavaTransformModel(TransformNamespace.DEFAULT.uri());
       javaTransform.setClazz(clazz);
       addTransformer(project, from, to, javaTransform);
   }

   /**
    * Add a Smooks Transformer.
    * @param project project
    * @param from Transform from (QName)
    * @param to Transform to (QName)
    * @param location Smooks file location
    * @param smtype Type of smooks Transformation
    */
   public void addSmooksTransformer(Project project, String from, String to, String location,
		   String smtype) {
       SmooksTransformModel smooksTransform = new V1SmooksTransformModel(TransformNamespace.DEFAULT.uri());
       smooksTransform.setConfig(location);
       smooksTransform.setTransformType(smtype);
       addTransformer(project, from, to, smooksTransform);

   }
   
   /**
    * Add a XSLT Transformer.
    * @param project project
    * @param from Transform from (QName)
    * @param to Transform to (QName)
    * @param xsltFile XSLT file location
    * @param failOnWar Whether to fail on a warning
    */
   public void addXSLTTransformer(Project project, String from, String to, String xsltFile,
		   boolean failOnWarn) {
       XsltTransformModel xsltTransform = new V1XsltTransformModel(TransformNamespace.DEFAULT.uri());
       xsltTransform.setXsltFile(xsltFile);
       xsltTransform.setFailOnWarning(failOnWarn);
       addTransformer(project, from, to, xsltTransform);

   }
   
   /**
    * Add a JSON Transformer.
    * @param project project
    * @param from Transform from (QName)
    * @param to Transform to (QName)
    */
   public void addJSONTransformer(Project project, String from, String to) {
	   TransformModel jsonTransform = new V1JSONTransformModel(TransformNamespace.DEFAULT.uri());
       addTransformer(project, from, to, jsonTransform);

   }
   
   /**
    * Add a JAXB Transformer.
    * @param project project
    * @param from Transform from (QName)
    * @param to Transform to (QName)
    */
   public void addJAXBTransformer(Project project, String from, String to) {
	   TransformModel jaxbTransform  = new V1JAXBTransformModel(TransformNamespace.DEFAULT.uri());
       addTransformer(project, from, to, jaxbTransform);

   }
          
   /**
    * Add a Transformer.
    * @param project project
    * @param from Transform from (QName)
    * @param to Transform to (QName)
    */
   public void addTransformer(Project project, String from, String to, TransformModel transform) {

       transform.setFrom(QName.valueOf(from));
       transform.setTo(QName.valueOf(to));
	
       SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
       SwitchYardModel switchYardConfig = switchYard.getSwitchYardConfig();
       if (switchYard.getSwitchYardConfig().getTransforms() == null) {
    	   String switchYardNamespace = getSwitchYardNamespace(switchYardConfig);
           switchYardConfig.setTransforms(new V1TransformsModel(switchYardNamespace));
       }
       switchYard.getSwitchYardConfig().getTransforms().addTransform(transform);
       switchYard.saveConfig();	       
   }
       
   /**
    * Add a Java Validator.
    * @param project project
 	* @param type type of validator
 	* @param clazz Class 
 	*/
   public void addJavaValidator(Project project, String type, String clazz) {
       
       JavaValidateModel javaValidate = new V1JavaValidateModel(ValidateNamespace.DEFAULT.uri());
       javaValidate.setClazz(clazz);
       javaValidate.setName(QName.valueOf(type));
       
       addValidator(project, javaValidate, ValidatorTypes.JAVA);
   }
   
   /**
    * Add XML validator.
    * @param project project
 	* @param type type of validator
 	* @param schemaTypeString schema type
 	* @param schemaCatalog schema catalog
 	* @param schemaFile schema file location
 	* @param namespaceAware is it namespaceAware
 	* @param failOnWarn should we fail on warn
 	*/
   public void addXMLValidator(Project project, String type, XmlSchemaType schemaType, String schemaCatalog,
		   String schemaFile, boolean namespaceAware, boolean failOnWarn) {
       
       XmlValidateModel xmlValidate = new V1XmlValidateModel(ValidateNamespace.DEFAULT.uri());
       xmlValidate.setSchemaType(schemaType);

       if (schemaCatalog != null && schemaCatalog.trim().length() > 0) {
           FileEntryModel entry = new V1FileEntryModel(ValidateNamespace.DEFAULT.uri()).setFile(schemaCatalog);
           SchemaCatalogsModel catalogs = new V1SchemaCatalogsModel(ValidateNamespace.DEFAULT.uri()).addEntry(entry);
           xmlValidate.setSchemaCatalogs(catalogs);
       }
       
       if (XmlSchemaType.DTD != schemaType) {
    	   FileEntryModel entry = new V1FileEntryModel(ValidateNamespace.DEFAULT.uri()).setFile(schemaFile);
           SchemaFilesModel files = new V1SchemaFilesModel(ValidateNamespace.DEFAULT.uri()).addEntry(entry);
           xmlValidate.setSchemaFiles(files);
           xmlValidate.setNamespaceAware(namespaceAware);
       }
       
       xmlValidate.setFailOnWarning(failOnWarn);
       xmlValidate.setName(QName.valueOf(type));

       addValidator(project, xmlValidate, ValidatorTypes.XML);
   }
   

   /**
    * Add a message validator.
    * @param project project
    * @param validate validator model
 	* @param validatorType validator type
 	*/
   public void addValidator(Project project, ValidateModel validate, 
		   ValidatorTypes validatorType) { 
                     
       
       SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
       SwitchYardModel switchYardConfig = switchYard.getSwitchYardConfig();
       if (switchYardConfig.getValidates() == null) {
           String switchYardNamespace = getSwitchYardNamespace(switchYardConfig);
           switchYardConfig.setValidates(new V1ValidatesModel(switchYardNamespace));
       }
       switchYardConfig.getValidates().addValidate(validate);
       switchYard.saveConfig();


   }
   
   /**
    * Add a static operation selector.
    * @param project project
    * @param serviceName service name
    * @param binding binding
    * @param operationName operation name
    */
   public void addStaticOperationSelector(Project project, String serviceName, 
   			BindingModel binding, String operationName) {
       SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
       SwitchYardModel switchYardConfig = switchYard.getSwitchYardConfig();
       String switchYardNamespace = getSwitchYardNamespace(switchYardConfig);
	   StaticOperationSelectorModel staticSelector = new V1StaticOperationSelectorModel(switchYardNamespace);
       staticSelector.setOperationName(operationName);
       addOperationSelector(project, serviceName, binding, staticSelector);
   }

   /**
    * Add XPath operation selector.
    * @param project project
    * @param serviceName service name
    * @param binding binding
    * @param xpathExpression XPath expression
    */
   public void addXPathOperationSelector(Project project, String serviceName, 
  			BindingModel binding, String xpathExpression) {
       SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
       SwitchYardModel switchYardConfig = switchYard.getSwitchYardConfig();
       String switchYardNamespace = getSwitchYardNamespace(switchYardConfig);
	   XPathOperationSelectorModel xpathSelector = new V1XPathOperationSelectorModel(switchYardNamespace);
       xpathSelector.setExpression(xpathExpression);
       addOperationSelector(project, serviceName, binding, xpathSelector);
   }
   
   /**
    * Add Regex Operation Selector.
    * @param project project
    * @param serviceName service name 
    * @param binding binding
    * @param regex regex
    */
   public void addRegexOperationSelector(Project project, String serviceName,
		   BindingModel binding, String regex) {
       SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
       SwitchYardModel switchYardConfig = switchYard.getSwitchYardConfig();
       String switchYardNamespace = getSwitchYardNamespace(switchYardConfig);
	   RegexOperationSelectorModel regexSelector = new V1RegexOperationSelectorModel(switchYardNamespace);
       regexSelector.setExpression(regex);
       addOperationSelector(project, serviceName, binding, regexSelector);
   }
   
   /**
    * Add Java Operation Selector.
    * @param project project
    * @param serviceName service name
    * @param binding binding
    * @param clazz java class name
    */
   public void addJavaOperationSelector(Project project, String serviceName,
		   BindingModel binding, String clazz) {
       SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
       SwitchYardModel switchYardConfig = switchYard.getSwitchYardConfig();
       String switchYardNamespace = getSwitchYardNamespace(switchYardConfig);
	   JavaOperationSelectorModel javaSelector = new V1JavaOperationSelectorModel(switchYardNamespace);
       javaSelector.setClazz(clazz);
       addOperationSelector(project, serviceName, binding, javaSelector);
   }
   
   /**
    * Add Operation Selector.
    * @param project project
    * @param serviceName service name
    * @param binding binding
    * @param selector selector
    */
   public void addOperationSelector(Project project, String serviceName, 
		   BindingModel binding, OperationSelectorModel selector) {
       SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
       SwitchYardModel switchYardConfig = switchYard.getSwitchYardConfig();
       String switchYardNamespace = getSwitchYardNamespace(switchYardConfig);
       CompositeServiceModel service = null;
       for (CompositeServiceModel s : switchYard.getSwitchYardConfig().getComposite().getServices()) {
           if (s.getName().equals(serviceName)) {
               service = s;
           }
       }
       if (service == null) {
           throw new IllegalArgumentException("Service " + serviceName + " could not be found");
       }

       List<BindingModel> bindingList = service.getBindings(); 
       if (bindingList.size() == 0) {
           throw new IllegalArgumentException("There is no binding which supports OperationSelector");
       }

       List<String> bindingDescList = new ArrayList<String>();
       
       binding.setOperationSelector(selector);
       switchYard.saveConfig();
   }
      
   /**
    * Add a policy.
    * @param project project
    * @param componentName component name
    * @param p policy
    * @param where where the policy should be added
    * @param ref component reference
    */
   public void addPolicy(Project project, String componentName, Policy p, String where,
		   ComponentReferenceModel ref) {
       SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
       ComponentModel component = null;
       for (ComponentModel c : switchYard.getSwitchYardConfig().getComposite().getComponents()) {
           if (c.getName().equals(componentName)) {
               component = c;
               break;
           }
       }
       if (component == null) {
           for (ComponentModel c : switchYard.getMergedSwitchYardConfig().getComposite().getComponents()) {
               if (c.getName().equals(componentName)) {
                   switchYard.getSwitchYardConfig().getComposite().addComponent(c);
                   component = c;
                   break;
               }
           }
           
           if (component == null) {
        	   throw new IllegalArgumentException("Component " + componentName + " could not be found");
           }
       }
       
       String target = null;

       if (where.equalsIgnoreCase("Implementation")) {
           component.getImplementation().addPolicyRequirement(p.getName());
           target = "Implementation";
       } else if (where.equalsIgnoreCase("Service")) {
           // component service should be just one
           ComponentServiceModel service = component.getServices().get(0);
           service.addPolicyRequirement(p.getName());
           target = service.getName();
       } else if (where.equalsIgnoreCase("Reference")) {
           if (component.getReferences().size() == 0) {
        	   throw new IllegalArgumentException("No reference is found in " + componentName);
           }

           ref.addPolicyRequirement(p.getName());
           target = ref.getName();

       } else {
    	   throw new IllegalArgumentException("Unknown place " + where);
       }
       
       switchYard.saveConfig();
   }
   
    /**
     * Get the SwitchYard configuration for the given project.
     * @param project project
     * @param verbose whether to print out the XML
     * @return String containing config model
     */
    public String getConfig(Project project, Boolean verbose) {
       SwitchYardModel config = project.getFacet(SwitchYardFacet.class).getMergedSwitchYardConfig();
       String newLine = System.getProperty("line.separator");
       
       //TerminalTextStyle BOLD = new TerminalTextStyle(CharacterType.BOLD);
       //TerminalColor YELLOW = new TerminalColor(Color.YELLOW, Color.DEFAULT);
       //TerminalColor RED = new TerminalColor(Color.RED, Color.DEFAULT);
       
       // 'verbose' option with no value or value=true counts
       if (verbose == null || verbose) {
    	   return(config.toString());
       }

       StringBuilder builder = new StringBuilder();
       builder.append(String.format("[Public]%n"));
       // Print promoted service info
       for (CompositeServiceModel service : config.getComposite().getServices()) {
    	   //builder.append(new TerminalString("service: ", BOLD));
    	   builder.append("service: ");

    	   builder.append(String.format(service.getName() + "%n"));
           //builder.append(new TerminalString("   interface: ", BOLD));
           builder.append("   interface: ");

           if (service.getInterface() != null) {
               builder.append(String.format(service.getInterface().getInterface() + "%n"));
           } else {
               //builder.append(new TerminalString("inherited", YELLOW));
               builder.append("inherited");
        	   builder.append(newLine);
           }
           for (BindingModel binding : service.getBindings()) {
               //builder.append(new TerminalString("   binding: ", BOLD));
               builder.append("   binding: ");

        	   builder.append(String.format(binding.getType() + "%n"));
           }
       }
       // Print promoted reference info
       for (CompositeReferenceModel reference : config.getComposite().getReferences()) {
           //builder.append(new TerminalString("reference: ", BOLD));
           builder.append("reference: ");

           builder.append(String.format(reference.getName() + "%n"));
           //builder.append(new TerminalString("   interface: ", BOLD));
           builder.append("   interface: ");

           if (reference.getInterface() != null) {
               builder.append(String.format(reference.getInterface().getInterface() + "%n"));
           } else {
               //builder.append(new TerminalString("inherited", YELLOW));
               builder.append("inherited");

               builder.append(newLine);
           }
           for (BindingModel binding : reference.getBindings()) {
        	   //builder.append(new TerminalString("   binding: ", BOLD));
        	   builder.append("   binding: ");

               builder.append(binding.getType());
           }
       }
       
       builder.append(newLine);
       builder.append(String.format("[Private]%n"));
       for (ComponentModel component : config.getComposite().getComponents()) {
    	   //builder.append(new TerminalString( "component: ", BOLD));
    	   builder.append("component: ");

           builder.append(String.format(component.getName() + "%n"));
           for (ComponentServiceModel service : component.getServices()) {
        	   builder.append("   service: ");
        	   //builder.append(new TerminalString("   service: ", BOLD));
               builder.append(String.format(service.getName() + "%n"));
               //builder.append(new TerminalString("      interface: ", BOLD));
               builder.append("      interface: ");

               if (service.getInterface() != null) {
                   builder.append(String.format(service.getInterface().getInterface() + "%n"));
               } else {
            	   builder.append("unspecified");

            	   //builder.append(new TerminalString("unspecified", RED));
                   builder.append(newLine);
               }
           }
           for (ComponentReferenceModel reference : component.getReferences()) {
        	   //builder.append(new TerminalString("   reference: ", BOLD));
        	   builder.append("   reference: ");

        	   builder.append(String.format(reference.getName() + "%n"));
               //builder.append(new TerminalString("      interface: ", BOLD));
               builder.append("      interface: ");

               if (reference.getInterface() != null) {
            	   builder.append(String.format(reference.getInterface().getInterface()));
                   builder.append(newLine);
               } else {
            	   builder.append("unspecified");

            	   //builder.append(new TerminalString("unspecified", RED));
                   builder.append(newLine);
               }
           }
       }
       builder.append(newLine);
       return builder.toString();
   }


   /**
    * Get the namespace based on the SwitchYard config segment.
    * @param config SwitchYard config
    * @return namespace string
    */
   private String getSwitchYardNamespace(SwitchYardModel config) {
       String ns = null;
       if (config != null) {
           ns = config.getModelRootNamespace();
       }
       if (ns == null) {
           ns = SwitchYardNamespace.DEFAULT.uri();
       }
       return ns;
   }


   
}
