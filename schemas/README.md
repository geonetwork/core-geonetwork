# Maven module containing schema plugins

## Plugin structure

A schema plugin is composed off:

* A configuration folder containing XML file for configuration,
the schema as XSDs and a set of transformation (See http://geonetwork-opensource.org/manuals/trunk/eng/developer/schemaPlugins/index.html).

* (optional) A SchemaPlugin bean


## Add a plugin to the build

To include a new schema plugin in a build, copy the schema folder
here and add it to the copy-schemas execution in web/pom.xml.

