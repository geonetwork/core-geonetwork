Domain Module
-------------

The domain module contains the domain model object and the Spring-Data-JPA Repositories for accessing the database.  In order to
compile correctly you have to enable the JPA Metamodel generator.  It is an annotation processor and the instruction for enabling 
it in your IDE are available at: http://docs.jboss.org/hibernate/jpamodelgen/1.0/reference/en-US/html_single/#whatisit

Maven already has it enabled so if you don't want to work on this package only need to run the maven build and the 
required files will be generated.  

