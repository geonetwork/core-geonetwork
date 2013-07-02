Domain Module
-------------

The domain module contains the domain model object and the Spring-Data-JPA Repositories for accessing the database.  In order to
compile correctly you have to enable the JPA Metamodel generator.  It is an annotation processor and the instruction for enabling 
it in your IDE are available at: http://docs.jboss.org/hibernate/jpamodelgen/1.0/reference/en-US/html_single/#whatisit

Maven already has it enabled so if you don't want to work on this package only need to run the maven build and the 
required files will be generated.

Structure and Conventions
-------------------------

The package structure of this module is to put:

	* JPA Entity and related Embeddable classes in the org.fao.geonet.domain package or sub-packages
	* JPA EntityListener objects in org.fao.geonet.entitylistener package or sub-packages
	* Spring-data repository objects in org.fao.geonet.repository package or sub-packages
	* Spring-data-jpa Specification Utility/factory objects in org.fao.geonet.repository.specification package or sub-packages

The file config-spring-geonetwork.xml in the src/main/resources directory configures spring and JPA to scan the 
org.fao.geonet.domain package (and sub-packages) for Entities and the org.fao.geonet.repository package for spring-data-jpa
Repository objects.

The exact list that are scanned the spring configuration files need to be checked.  At the time of this writing the file
is config-spring-geonetwork.xml in this module.

The side-effect of this means that repositories and entities can be in other modules as well.  At the time of this writting
that is not the case but should be considered in the future.   

