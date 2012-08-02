package org.geonetwork.config

import java.io.File

import org.apache.commons.io.FileUtils
import org.junit.Assert._
import org.junit.After
import org.junit.Before
import org.junit.Test
import xml._

class MigrateConfigTest {

  private[this] var folder: File = _
  @Before
  def setup {
    folder = File.createTempFile("migrationtest", "_tmp")
    folder.delete
    folder.mkdirs
  }
  @After
  def after {
    FileUtils.deleteDirectory(folder);
  }
  @Test
  def test {
    val sources = (new File(classOf[MigrateConfigTest].getResource("testdata/user-profiles.xml").getFile())).getParentFile();
    FileUtils.copyDirectory(sources, folder);

    new MigrateConfiguration().migrate(folder.getAbsolutePath, true)
    
    for {
      f <- folder.listFiles().filter(_.isFile)
      if f.getName == "user-profiles.xml" || f.getName == "config-security.xml" 
    } {
      println("checking that " + f.getName() + " has data")
      val configData = xml.XML.loadFile(f)
      try {
        assertTrue("There should not be empty configuration files in " + f.getName, configData.child.collect { case e: xml.Elem => e }nonEmpty)
        configData \\ "service" filter (_.prefix == "j") foreach {s => assertTrue((s att "id").nonEmpty) }
        f.getName match {
          case "user-profiles.xml" => assertUserProfiles(configData)
          case "config-security.xml" => assertConfigSecurity(configData)
          case _ => ()
        }
      } catch {
        case e =>
          println(configData)
          throw e
      }
    }
  }
  def assertUserProfiles(configData: Elem) {
    val profiles = configData \ "profile"

    assertTrue(profiles.nonEmpty)
	profiles foreach (e => assertTrue(e \ "_" isEmpty))
  }
  def assertConfigSecurity(configData: Elem) {
    def containsAccess(url: Node, expected: String*) = expected forall (url att "access" contains _) 
	  assertTrue(configData \\ "intercept-url" find {url => (url att "pattern" endsWith "/home!?.*") && (url att "access" equalsIgnoreCase "permitAll")} nonEmpty)
	  assertTrue(configData \\ "intercept-url" find {url => (url att "pattern" endsWith "/admin!?.*") && (url att "access" equalsIgnoreCase "hasRole('RegisteredUser')")} nonEmpty)
	  assertTrue(configData \\ "intercept-url" find {url => (url att "pattern" endsWith "/metadata.edit!?.*") && (url att "access" equalsIgnoreCase "hasRole('Editor')")} nonEmpty)
	  assertTrue(configData \\ "intercept-url" find {url => (url att "pattern" equals "/.*") && (url att "access" equalsIgnoreCase "denyAll")} nonEmpty)
  }
  def childrenByAtt(e: NodeSeq, childName: Symbol, attName: Symbol, attValue: String) =
    e \ childName.name filter (n => (n att attName.name) == attValue)
}