# Introduction

This composition is meant to make runtime testing the CAS integration of
GeoNetwork easier.

This composition also integrates a LDAP, so that testing the
config-spring-cas-ldap configuration is also possible.

# Prerequisites

It requires the GeoNetwork webapp to be built first:

```
$ mvn clean package -DskipTests
```

Then it can be launched:

```
$ docker-compose up

```

# Accessing the CAS login page from GeoNetwork

The default GeoNetwork UI does not provide an URL for the login which will redirect onto CAS, you will need to manually browse the following one:

```
http://localhost:8080/geonetwork/srv/eng/casRedirect
```

