The core module contains the core Geonetwork classes.  For example SearchManager for searching the metadata index, DataManager for saving
and loading Metadata.

Services, Harvesters, etc... that are are plugins will usually depend on core and will make use of these core classes to implement their
services.

Geonetwork is wired together via Spring-Dependency-Injection.  The critical classes (DataManager, SchemaManager, SearchManager, etc...) are
all singleton beans in the application context and can be either injected into other beans or obtained via the ServiceContext.getBean
method (or ServiceContext.getApplicationContext()).