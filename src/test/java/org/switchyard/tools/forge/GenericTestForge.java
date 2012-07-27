package org.switchyard.tools.forge;

import java.io.IOException;

import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.test.AbstractShellTest;
import org.junit.Assert;
import org.junit.Before;
import org.switchyard.common.version.Versions;

/**
 * Generic Test Class to be used for Forge testing.
 * 
 * @author Mario Antollini
 * @author George Gastaldi
 */
public abstract class GenericTestForge extends AbstractShellTest
{

   private static String switchyardVersion;

   private static String switchyardVersionSuccessMsg;

   private static final String FORGE_APP_NAME = "ForgeTestApp";

   /**
    * Constructor.
    */
   public GenericTestForge()
   {
      switchyardVersion = Versions.getSwitchYardVersion();
      switchyardVersionSuccessMsg = "SwitchYard version " + switchyardVersion;
   }

   /**
    * Let's setup the environment for the test.
    * 
    * @throws IOException exception will be caught and printed out
    */
   @Before
   public void prepareSwitchYardForge() throws Exception
   {
      initializeJavaProject();
      queueInputLines(FORGE_APP_NAME);
      getShell().execute("project install-facet switchyard");
      getShell().execute("switchyard get-version");
      Assert.assertTrue(getOutput().contains(switchyardVersionSuccessMsg));
   }

   /**
    * Handy method for building the Forge project skipping tests.
    */
   protected void mavenBuildSkipTest()
   {
      String[] mvnCommand = new String[] { "package", "-e", "-Dmaven.test.skip=true" };
      getProject().getFacet(MavenCoreFacet.class).executeMaven(mvnCommand);
   }

   /**
    * Handy method for building the Forge project.
    */
   protected void build()
   {
      try
      {
         getShell().execute("build");
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   /**
    * Self-explanatory accessor method.
    * 
    * @return the Switchyard version
    */
   public static String getSwitchyardVersion()
   {
      return switchyardVersion;
   }

}
