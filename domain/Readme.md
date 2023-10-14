# Domain Module

The domain module contains the domain model object and the Spring-Data-JPA Repositories for accessing the database.  In order to
compile correctly, you have to enable the JPA Metamodel generator.  It is an annotation processor and the instructions for enabling
it in your IDE are available at: http://docs.jboss.org/hibernate/jpamodelgen/1.0/reference/en-US/html_single/#whatisit

Maven already has it enabled, so if you don't want to work on this package, you only need to run the maven build and the
required files will be generated.

## Structure and Conventions

The package structure of this module is to put:

* JPA Entity and related Embeddable classes in the org.fao.geonet.domain package or sub-packages
* JPA EntityListener objects in org.fao.geonet.entitylistener package or sub-packages
* Spring-data repository objects in org.fao.geonet.repository package or sub-packages
* Spring-data-jpa Specification Utility/Factory objects in org.fao.geonet.repository.specification package or sub-packages

The file config-spring-geonetwork.xml in the src/main/resources directory configures spring and JPA to scan the
org.fao.geonet.domain package (and sub-packages) for Entities and the org.fao.geonet.repository package for spring-data-jpa
Repository objects.

The exact list that is scanned by the spring configuration files needs to be checked.  At the time of this writing the file
is config-spring-geonetwork.xml in this module.

The side effect of this means that repositories and entities can be in other modules as well.  At the time of this writing
that is not the case but should be considered in the future.

## Writing Custom Queries

IMPORTANT:  Please do not add any Specifications or Queries to the system without at least one test for each query!

Spring Data allows four different ways to write queries. (For more in-depth explanation see: http://projects.spring.io/spring-data-jpa/

1. Query methods
   - You can create methods in the ...Repository interface and the method name will be parsed by Spring Data and a query will be created for you based on the name.  There are many different options including in, and, or ways to write the methods.
   - While the methods are easy to write, they are not very flexible.  Using Specifications tends to be a more flexible solution and is therefore preferred.
   - **Examples: (See tests for example usage)**
     * [UserRepository](src/main/java/org/fao/geonet/repository/UserRepository.java)
     * [OperationAllowedRepository](src/main/java/org/fao/geonet/repository/OperationAllowedRepository.java)
2. Specifications
   - The Specification interface provides a composable API for writing queries.
   - For example, you could write a hasMetadataId specification and a hasOwnerId specification.  One could then compose them using not, and, or.
   - Each specification should be in the appropriate <DomainObject>Specs class.
   - Each specification is to be created with a static method.
   - Each specification must have a test.
   - **Examples: (See tests for example usage)**
     * [MetadataSpecs](src/main/java/org/fao/geonet/repository/specification/MetadataSpecs.java)
     * [UserSpecs](src/main/java/org/fao/geonet/repository/specification/UserSpecs.java)
3. Custom Queries
   - Spring JPA Repositories can have custom queries with very custom implementations.  A Repository will extend an interface with the custom methods and the implementations would be in a subclass of the interface.
   - The implementation will use the normal EntityManager (JPA) API for constructing the queries.
   - **Examples: (See tests for example usage)**
     * [MetadataRepositoryCustom](src/main/java/org/fao/geonet/repository/MetadataRepositoryCustom.java)
     * [MetadataRepositoryCustomImpl](src/main/java/org/fao/geonet/repository/MetadataRepositoryCustomImpl.java)
4. Standard JPA querying
   - One can obtain an EntityManager by using the @PersistentContext annotation on a field in a Spring bean.
   - The EntityManager can be used to write queries.
   - All Queries should be contained in the same module as the domain object that it is querying, unless the module is a plugin module.
   - **Examples: (See tests for example usage)**
     * [MetadataReportsQueries](src/main/java/org/fao/geonet/repository/reports/MetadataReportsQueries.java)
