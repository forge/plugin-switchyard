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
package org.switchyard.tools.forge.bean;

import javax.inject.Inject;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.Annotation;
import org.jboss.forge.parser.java.Field;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.JavaInterface;
import org.switchyard.component.bean.Reference;

/**
 * Forge plugin for Bean references commands.
 * 
 * @author Antollini Mario.
 * 
 */
public class BeanReferenceConfigurator
{

   /**
    * Create a new Bean service interface and implementation.
    * 
    * @param beanName bean name
    * @param referenceName reference name
    * @param referenceBeanName the bean to be referenced
    * @param out shell output
    * @throws java.io.IOException trouble reading Switchyard config or reading/writing Bean file
    */
   public void newReference(Project project, final String beanName, final String pkgName, final String referenceName,
            final String referenceBeanName) throws java.io.IOException
   {
      final MetadataFacet meta = project.getFacet(MetadataFacet.class);
      final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);

      // Make sure the bean exists
      JavaResource beanFile = project.getRootDirectory().getChildOfType(JavaResource.class,
               "src/main/java/" + meta.getTopLevelPackage().replace(".", "/") + "/" + beanName + "Bean.java");
      if (!beanFile.exists())
      {
         throw new IllegalArgumentException("Bean not found: " + beanFile);
      }

      JavaClass javaClass = JavaParser.parse(JavaClass.class, beanFile.getUnderlyingResourceObject());

      if (!javaClass.hasImport(Inject.class))
      {
         javaClass.addImport(Inject.class);
      }
      if (!javaClass.hasImport(Reference.class))
      {
         javaClass.addImport(Reference.class);
      }

      String referenceBeanJavaType = referenceBeanName;
      if (referenceBeanJavaType == null)
      {
         referenceBeanJavaType = referenceName;
      }

      String referenceFieldName = new StringBuilder(referenceBeanJavaType.length())
               .append(Character.toLowerCase(referenceBeanJavaType.charAt(0)))
               .append(referenceBeanJavaType.substring(1))
               .toString();

      if (javaClass.hasField(referenceFieldName))
      {
         referenceFieldName = referenceFieldName + Math.random() * 100;
      }

      Field<JavaClass> referenceField = javaClass.addField("private " + referenceBeanJavaType + " "
               + referenceFieldName + ";");

      referenceField.addAnnotation(Inject.class);
      Annotation<?> referenceAnnotation = referenceField.addAnnotation(Reference.class);
      referenceAnnotation.setStringValue(referenceName);

      if (javaClass.hasSyntaxErrors())
      {
         throw new IllegalStateException("WARNING: " + javaClass.getName() + " seems to have syntax errors.");
      }

      java.saveJavaSource(javaClass);

      /*
       * If not present already, let's create a bare bean interface file for the specifiedreferenceName in order to
       * avoid compilation errors.
       */
      JavaResource referenceFile = project.getRootDirectory().getChildOfType(JavaResource.class,
               "src/main/java/" + meta.getTopLevelPackage().replace(".", "/") + "/" + referenceBeanJavaType + ".java");
      if (!referenceFile.exists())
      {

         JavaInterface skeletonReferencedBean = JavaParser.create(JavaInterface.class);
         skeletonReferencedBean.setPackage(meta.getTopLevelPackage());
         skeletonReferencedBean.setName(referenceBeanJavaType);

         if (skeletonReferencedBean.hasSyntaxErrors())
         {
            throw new IllegalStateException("WARNING: " + skeletonReferencedBean.getName()
                     + " seems to have syntax errors.");
         }

         java.saveJavaSource(skeletonReferencedBean);
      }
   }

}
