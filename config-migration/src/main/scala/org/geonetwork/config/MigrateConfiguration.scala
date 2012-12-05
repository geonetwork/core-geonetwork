package org.geonetwork.config

import scala.xml.transform.RuleTransformer
import java.io.File
import org.apache.commons.io.FileUtils
import scala.xml._
import org.apache.commons.io.IOUtils
import scala.xml.transform.RewriteRule
import java.io.FileWriter
import scalax.io.Resource.fromFile

class MigrateConfiguration {
  def migrate(webInfDir:String, configDir: String, throwException: Boolean) = {
    migrateUserProfiles(webInfDir, configDir,throwException)
  }
  
  def migrateUserProfiles(webInfDir:String, configDir: String, throwException: Boolean) = {
    val userProfiles = new File(configDir, "user-profiles.xml")
    val configSecurityMappingFile = new File(configDir, "config-security-mapping.xml")
    if (!configSecurityMappingFile.exists() && userProfiles.exists) {
      val original = XML.loadFile(userProfiles)
      userProfiles.renameTo(new File(configDir,"user-profiles-old.xml"))
      
      val springSecXml =
<beans
	xsi:schemaLocation="http://www.springframework.org/schema/beans
          http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
          http://www.springframework.org/schema/context
          http://www.springframework.org/schema/context/spring-context-3.0.xsd
          http://www.springframework.org/schema/security
          http://www.springframework.org/schema/security/spring-security-3.1.xsd"
	xmlns:sec="http://www.springframework.org/schema/security" xmlns:ctx="http://www.springframework.org/schema/context"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.springframework.org/schema/beans">

	<bean id="filterSecurityInterceptor"
		class="org.springframework.security.web.access.intercept.FilterSecurityInterceptor">
  		<property name="authenticationManager" ref="authenticationManager"/>
  		<property name="accessDecisionManager" ref="accessDecisionManager"/>
		<property name="securityMetadataSource">
			<sec:filter-security-metadata-source use-expressions="true"  request-matcher="regex">
		        <sec:intercept-url pattern="/monitor/.*" access="hasRole('Monitor')"></sec:intercept-url>
		        <sec:intercept-url pattern="/.*healthcheck" access="hasRole('Monitor')"></sec:intercept-url>
		        <sec:intercept-url pattern="/srv/.+/login.form(|!)" access="permitAll"></sec:intercept-url>
		        <sec:intercept-url pattern="/srv/.+/.+\?casLogin.*" access="hasRole('RegisteredUser')"></sec:intercept-url>

                {(original \ "profile") map interceptUrls }
            </sec:filter-security-metadata-source>
      	</property>
    </bean>
</beans>

      write(configSecurityMappingFile, springSecXml)
      write(userProfiles, <profiles><profile name="Developer" extends="Administrator"/>{original \ "profile" map { _.asInstanceOf[Elem].copy(child=Nil)}}</profiles>)
      copy(webInfDir, configDir, "config-security.xml")
      copy(webInfDir, configDir, "config-security-ldap.xml")
      copy(webInfDir, configDir, "config-security-cas.xml")
      copy(webInfDir, configDir, "config-security-cas-ldap.xml")
      copy(webInfDir, configDir, "config-security-cas-database.xml")
      copy(webInfDir, configDir, "config-security-cas-database.xml")
    }
  }
  def copy(webInfDir:String, configDir:String, fileName:String) {
    val from = fromFile(new File(webInfDir, fileName))
    val to = fromFile(new File(configDir, fileName))
    to write from.byteArray
  }
  def write(file: File, xml: Node) = {
    val out = new FileWriter(file)
    try {
      out.write(new PrettyPrinter(140,4).format(xml).replace("&gt;", ">"))
    } finally {
      out.close()
    }
  } 
  def interceptUrls(profile: Node) = {
    val role = (profile att "name")
    profile \ "allow" map {allow =>
      val access = if (role == "Guest") "permitAll" else "hasRole('"+role+"')"
      <sec:intercept-url pattern={"/srv/.*/"+(allow att "service")+"!?.*"} access={access}/>        
    }
  }
  
}

class ProfileNode(name:String, var parents: Set[ProfileNode] ,var children:Set[ProfileNode])