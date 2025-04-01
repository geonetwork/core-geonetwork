# Datahub Module

Dathub module is integrated with maven build environment, making use of the following build locations:

- ``node`` - geonetwork-ui build environment
- ``target/geonetwork-ui`` - checkout used to build geonetwork-ui locally
- ``target/resources`` - staged datahub app

A successful build result is assembled into ``target/gn-datahub-integration-4.4.7-SNAPSHOT.zip`` archive for distribution.

## Node

The ``node`` build environment is covered by ``.gitignore`` to prevent accidental commit.

The ``mvn clean:clean@reset`` target is availale to reset the node build environment.
