package org.geonetwork.config

import scala.xml.transform.RuleTransformer
import java.io.File
import org.apache.commons.io.FileUtils
import scala.xml._
import org.apache.commons.io.IOUtils
import scala.xml.transform.RewriteRule
import java.io.FileWriter

class MigrateConfiguration {
  def migrate(configDir: String, throwException: Boolean) = {
    migrateUserProfiles(configDir,throwException)
  }
  
  def migrateUserProfiles(configDir: String, throwException: Boolean) = {
    val userProfiles = new File(configDir, "user-profiles.xml")
    val springSecFile = new File(configDir, "config-security.xml") 
    if (!springSecFile.exists() && userProfiles.exists) {
      val original = XML.loadFile(userProfiles)
      userProfiles.renameTo(new File(configDir,"user-profiles-old.xml"))
      val springSecXml =
<beans xmlns:sec="http://www.springframework.org/schema/security"
	xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
          http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
          http://www.springframework.org/schema/security
          http://www.springframework.org/schema/security/spring-security-3.1.xsd">

    <!-- <sec:debug/> -->
	<sec:http pattern="/apps/**" security="none" create-session="stateless"/>
    <sec:http pattern="/" create-session="stateless" security="none"></sec:http>
    <sec:http pattern="/*.html" create-session="stateless" security="none"></sec:http>
    <sec:http pattern="/*.jsp" create-session="stateless" security="none"></sec:http>
    <sec:http pattern="/*.css" create-session="stateless" security="none"></sec:http>
    <sec:http pattern="/images/**" create-session="stateless" security="none"></sec:http>
    <sec:http pattern="/htmlcache/**" create-session="stateless" security="none"></sec:http>
    <sec:http pattern="/scripts/**" create-session="stateless" security="none"></sec:http>

	<sec:http auto-config='true' access-decision-manager-ref="accessDecisionManager" realm="Geonetwork">
        <sec:anonymous granted-authority="ROLE_Guest"/>
        {(original \ "profile") map interceptUrls }
		<sec:intercept-url pattern="/monitor/**" access="ROLE_Monitor"/>
		<sec:intercept-url pattern="/*healthcheck" access="ROLE_Monitor"/>
		<sec:intercept-url pattern="/**" access="ROLE_REJECT_ALL"/>
		<sec:logout delete-cookies="JSESSIONID" />
	</sec:http>

    <sec:authentication-manager>
        <sec:authentication-provider ref="geonetworkAuthenticationProvider"/>
    </sec:authentication-manager>

    <bean id="geonetworkAuthenticationProvider" class="org.fao.geonet.kernel.security.GeonetworkAuthenticationProvider">
      <constructor-arg value="system-wide-password-salt-please-customize-this" />
    </bean>
	￼￼
    <bean id="roleHierarchy" class="org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl">
      <property name="hierarchy">
      	<value>{
    	  val relations = for { 
    	    p <- original \\ "profile"
    	    name = p att "name"
    	    extended <- p att "extends" split ","
    	    if extended.trim.nonEmpty
    	  } yield "ROLE_"+name+" > ROLE_"+extended.trim
    	  
    	  relations mkString "\n"
        }</value>
      </property>
    </bean>
    <bean id="accessDecisionManager" class="org.springframework.security.access.vote.AffirmativeBased">
       <property name="decisionVoters">
        <list>
          <bean class="org.springframework.security.access.vote.RoleHierarchyVoter"> 
      		<constructor-arg ref="roleHierarchy" />
          </bean>
      	  <bean class="org.springframework.security.access.vote.AuthenticatedVoter"/>
        </list>
       </property>
    </bean>

</beans>

      write(springSecFile, springSecXml)
      write(userProfiles, <profiles>{original \ "profile" map { _.asInstanceOf[Elem].copy(child=Nil)}}</profiles>)
    }
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
    val role = "ROLE_"+(profile att "name")
    profile \ "allow" map {allow => 
        <sec:intercept-url pattern={"/srv/*/"+(allow att "service")} access={role}/>
    }
  }
    
}

class ProfileNode(name:String, var parents: Set[ProfileNode] ,var children:Set[ProfileNode])