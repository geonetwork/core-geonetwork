# Auditable Module

The auditable module contains the classes that allow auditing changes in user information.

Support for new auditable entities can be added, for example to audit changes in group information. See as an example, [UserAuditable](src/main/java/org/fao/geonet/auditable/model/UserAuditable.java).

The current auditable service [AuditableService](src/main/java/org/fao/geonet/auditable/AuditableService.java) uses [Javers](https://javers.org/),
but new auditable services can be implemented using other technologies.

An example of using the auditable service can be found in [UserApi](../services/src/main/java/org/fao/geonet/api/users/UsersApi.java).




