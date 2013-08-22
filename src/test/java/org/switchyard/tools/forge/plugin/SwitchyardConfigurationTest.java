/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.switchyard.tools.forge.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.config.model.composite.BindingModel;
import org.switchyard.config.model.composite.ComponentImplementationModel;
import org.switchyard.config.model.composite.ComponentModel;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.config.model.composite.ComponentServiceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.config.model.composite.v1.V1BindingModel;
import org.switchyard.config.model.composite.v1.V1ComponentImplementationModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentReferenceModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.composite.v1.V1CompositeServiceModel;
import org.switchyard.config.model.selector.JavaOperationSelectorModel;
import org.switchyard.config.model.selector.RegexOperationSelectorModel;
import org.switchyard.config.model.selector.StaticOperationSelectorModel;
import org.switchyard.config.model.selector.XPathOperationSelectorModel;
import org.switchyard.config.model.transform.TransformModel;
import org.switchyard.config.model.validate.ValidateModel;
import org.switchyard.policy.Policy;
import org.switchyard.policy.PolicyFactory;
import org.switchyard.policy.TransactionPolicy;
import org.switchyard.transform.config.model.JAXBTransformModel;
import org.switchyard.transform.config.model.JSONTransformModel;
import org.switchyard.transform.config.model.JavaTransformModel;
import org.switchyard.transform.config.model.SmooksTransformModel;
import org.switchyard.transform.config.model.XsltTransformModel;
import org.switchyard.validate.config.model.JavaValidateModel;
import org.switchyard.validate.config.model.XmlSchemaType;
import org.switchyard.validate.config.model.XmlValidateModel;

@RunWith(Arquillian.class)
public class SwitchyardConfigurationTest
{
   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.switchyard.forge:switchyard-forge-plugin", version = "1.0.0-SNAPSHOT"),
            @AddonDependency(name = "org.jboss.forge.addon:projects"),
            @AddonDependency(name = "org.jboss.forge.addon:maven")
   })
   public static ForgeArchive getDeployment()
   {
      return ShrinkWrap.create(ForgeArchive.class).
               addBeansXML().
               addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.addon:projects"),
                        AddonDependencyEntry.create("org.switchyard.forge:switchyard-forge-plugin"),
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
               );
   }

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private FacetFactory facetFactory;
   
   @Inject
   private SwitchYardConfigurator switchYardConfigurator;

   Project project;

   private static final String ORIG_CONFIG_PATH = "target/test-classes/standalone.xml";
   private static final String BACKUP_CONFIG_PATH = ORIG_CONFIG_PATH + ".orig";

   
   @Before
   public void setupTestProject()
   {
      project = projectFactory.createTempProject();
      project.getProjectRoot().deleteOnExit();

      MetadataFacet metadataFacet = project.getFacet(MetadataFacet.class);
      metadataFacet.setProjectName("testproject");
      metadataFacet.setProjectVersion("1.0.0-SNAPSHOT");
      metadataFacet.setTopLevelPackage("com.acme.testproject");
   }

   @After
   public void cleanupTestProject()
   {
      if (project != null)
         project.getProjectRoot().delete(true);
   }

   @Test
   public void testInstallSwitchyard() throws Exception
   {
      Assert.assertFalse(project.hasFacet(SwitchYardFacet.class));
      SwitchYardFacet switchYard = facetFactory.install(project, SwitchYardFacet.class);
      Assert.assertNotNull(switchYard);
      
      switchYard.addSwitchYardToASConfig(ORIG_CONFIG_PATH);
      Assert.assertTrue(new File(ORIG_CONFIG_PATH).exists());
      Assert.assertTrue(new File(BACKUP_CONFIG_PATH).exists());

      String serviceName = "ForgeTestService";
      CompositeServiceModel service = new V1CompositeServiceModel();
      service.setName(serviceName);
      service.addBinding(new V1BindingModel("bean"));
      switchYard.getSwitchYardConfig().getComposite().addService(service);
      switchYard.saveConfig();
      
      System.out.println("facetFactory implemented by" + facetFactory.getClass().getName());
      Assert.assertTrue(project.hasFacet(SwitchYardFacet.class));
      Assert.assertNotNull(project.getFacet(SwitchYardFacet.class));
      Assert.assertNotNull(switchYard.getMergedSwitchYardConfig().getComposite());
            
   }
   
   @Test
   public void testAddOperationSelector() throws Exception 
   {
      SwitchYardFacet switchYard = facetFactory.install(project, SwitchYardFacet.class);
       String serviceName = "ForgeTestService";
       CompositeServiceModel service = new V1CompositeServiceModel();
       service.setName(serviceName);
       service.addBinding(new V1BindingModel("bean"));
       switchYard.getSwitchYardConfig().getComposite().addService(service);
       switchYard.saveConfig();

       String operation = "myOperation";
       BindingModel model = service.getBindings().get(0);
       switchYardConfigurator.addStaticOperationSelector(project, serviceName, 
      			model, operation);
       StaticOperationSelectorModel staticSelector = StaticOperationSelectorModel.class.cast(model.getOperationSelector());
       Assert.assertEquals(operation, staticSelector.getOperationName());

       String xpath = "//person/language";
       switchYardConfigurator.addXPathOperationSelector(project, serviceName, 
     			model, xpath);
       XPathOperationSelectorModel xpathSelector = XPathOperationSelectorModel.class.cast(model.getOperationSelector());
       Assert.assertEquals(xpath, xpathSelector.getExpression());

       String regex = "*";
       switchYardConfigurator.addRegexOperationSelector(project, serviceName,
    		   model, regex); 
       RegexOperationSelectorModel regexSelector = RegexOperationSelectorModel.class.cast(model.getOperationSelector());
       Assert.assertEquals(regex, regexSelector.getExpression());

       String clazz = this.getClass().getName();
       switchYardConfigurator.addJavaOperationSelector(project, serviceName,
    		   model, clazz);
       JavaOperationSelectorModel javaSelector = JavaOperationSelectorModel.class.cast(model.getOperationSelector());
       Assert.assertEquals(clazz, javaSelector.getClazz());
   }
   
   @Test
   public void testAddPolicy() throws Exception
   {
      SwitchYardFacet switchYard = facetFactory.install(project, SwitchYardFacet.class);
       ComponentModel component = new V1ComponentModel();
       component.setName("TestComponent");
       ComponentModel noReferenceComponent = new V1ComponentModel();
       noReferenceComponent.setName("NoReferenceComponent");
       ComponentServiceModel service = new V1ComponentServiceModel();
       service.setName("TestService");
       component.addService(service);
       ComponentReferenceModel reference = new V1ComponentReferenceModel();
       reference.setName("TestReference");
       component.addReference(reference);
       ComponentImplementationModel implementation = new V1ComponentImplementationModel("bean");
       component.setImplementation(implementation);
       switchYard.getSwitchYardConfig().getComposite().addComponent(component);
       switchYard.getSwitchYardConfig().getComposite().addComponent(noReferenceComponent);       
       switchYard.saveConfig();

       //Assert.assertTrue(managedTransLocal.supports(PolicyType.IMPLEMENTATION));       
       // Add to Implementation
       switchYardConfigurator.addPolicy(project, "TestComponent", PolicyFactory.getAvailableImplementationPolicies().toArray(new Policy[0])[0], 
       	"Implementation", reference);
             
       Policy propogatesTrans = PolicyFactory.getPolicy("propagatesTransaction");
       Assert.assertEquals(TransactionPolicy.PROPAGATES_TRANSACTION, propogatesTrans);
       //Assert.assertTrue(propogatesTrans.supports(PolicyType.INTERACTION));
       // Add to Interaction
       switchYardConfigurator.addPolicy(project, "TestComponent", PolicyFactory.getAvailableInteractionPolicies().toArray(new Policy[0])[0], "Service", 
    		   reference);

       boolean caughtException = false;
       try {
    	   switchYardConfigurator.addPolicy(project, "noReferenceComponent", propogatesTrans, "Reference",
    		   reference);
       } catch (IllegalArgumentException iae) {
    	   caughtException = true;
       }
       switchYardConfigurator.addPolicy(project, "TestComponent", PolicyFactory.getAvailableInteractionPolicies().toArray(new Policy[0])[1], "Reference", reference);
       
       // Verify generated policies
       component = switchYard.getSwitchYardConfig().getComposite().getComponents().get(0);
       Assert.assertEquals(PolicyFactory.getAvailableImplementationPolicies().toArray(new Policy[0])[0].getName(), component.getImplementation().getPolicyRequirements().iterator().next());
       Assert.assertEquals(PolicyFactory.getAvailableInteractionPolicies().toArray(new Policy[0])[0].getName(), component.getServices().get(0).getPolicyRequirements().iterator().next());       
       Assert.assertEquals(PolicyFactory.getAvailableInteractionPolicies().toArray(new Policy[0])[1].getName(), component.getReferences().get(0).getPolicyRequirements().iterator().next());
   }
   
   @Test
   public void testAddValidator() {
      SwitchYardFacet switchYard = facetFactory.install(project, SwitchYardFacet.class);
       String type = "\"{urn:switchyard:forge-test:0.1.0}order\"";
       
       // Java
       switchYardConfigurator.addJavaValidator(project, type, this.getClass().getName());

       // XML
       switchYardConfigurator.addXMLValidator(project, type, XmlSchemaType.XML_SCHEMA, "/xsd/catalog.xml", "/xsd/orders.xsd", true, true);
 
       // Verify generated validators
       List<String> expected = new ArrayList<String>(Arrays.asList(new String[]{"Java", "XML"}));
       for (ValidateModel validate : switchYard.getSwitchYardConfig().getValidates().getValidates()) {
           if (validate instanceof JavaValidateModel) {
               JavaValidateModel java = JavaValidateModel.class.cast(validate);
               Assert.assertEquals(this.getClass().getName(), java.getClazz());
               expected.remove("Java");
           } else if (validate instanceof XmlValidateModel) {
               XmlValidateModel xml = XmlValidateModel.class.cast(validate);
               Assert.assertEquals(XmlSchemaType.XML_SCHEMA, xml.getSchemaType());
               Assert.assertEquals("/xsd/orders.xsd", xml.getSchemaFiles().getEntries().get(0).getFile());
               Assert.assertEquals("/xsd/catalog.xml", xml.getSchemaCatalogs().getEntries().get(0).getFile());
               Assert.assertEquals(true, xml.failOnWarning());
               Assert.assertEquals(true, xml.namespaceAware());
               expected.remove("XML");
           }
       }
       Assert.assertEquals(0, expected.size());
   }
   
   
   @Test
   public void testAddTransformer()  {
      SwitchYardFacet switchYard = facetFactory.install(project, SwitchYardFacet.class);
       String from = "\"{urn:switchyard:forge-test:0.1.0}order\"";
       String to = "\"{urn:switchyard:forge-test:0.1.0}orderAck\"";

       // Java
       switchYardConfigurator.addJavaTransformer(project, from, to, this.getClass().getName());
       // Smooks
       switchYardConfigurator.addSmooksTransformer(project, from, to, "/smooks/OrderXML.xml", "JAVA2XML");
       // XSLT
       switchYardConfigurator.addXSLTTransformer(project, from, to, "xslt/order.xslt", true);
       // JSON
       switchYardConfigurator.addJSONTTransformer(project, from, to);
       // JAXB
       switchYardConfigurator.addJAXBTransformer(project, from, to);

       // Verify generated transformers
       List<String> expected = new ArrayList<String>(Arrays.asList(new String[]{"Java", "Smooks", "XSLT", "JSON", "JAXB"}));
       for (TransformModel transform : switchYard.getSwitchYardConfig().getTransforms().getTransforms()) {
           if (transform instanceof JavaTransformModel) {
               JavaTransformModel java = JavaTransformModel.class.cast(transform);
               Assert.assertEquals(this.getClass().getName(), java.getClazz());
               expected.remove("Java");
           } else if (transform instanceof SmooksTransformModel) {
               SmooksTransformModel smooks = SmooksTransformModel.class.cast(transform);
               Assert.assertEquals("/smooks/OrderXML.xml", smooks.getConfig());
               Assert.assertEquals("JAVA2XML", smooks.getTransformType());
               expected.remove("Smooks");
           } else if (transform instanceof XsltTransformModel) {
               XsltTransformModel xslt = XsltTransformModel.class.cast(transform);
               Assert.assertEquals("xslt/order.xslt", xslt.getXsltFile());
               Assert.assertEquals(true, xslt.failOnWarning());
               expected.remove("XSLT");
           } else if (transform instanceof JSONTransformModel) {
               expected.remove("JSON");
           } else if (transform instanceof JAXBTransformModel) {
               expected.remove("JAXB");
           } else {
               Assert.fail("Unknown transformer detected " + transform);
           }
       }
   } 
   
}
