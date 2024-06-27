# Auditable Module

The auditable module contains the classes that allow auditing changes in user information using [Hibernate Envers](https://hibernate.org/orm/envers/).

Support for new auditable entities can be added, for example to audit changes in group information. For users auditing:

- Entity with the information to audit: [UserAuditable](../domain/src/main/java/org/fao/geonet/domain/auditable/UserAuditable.java).
- Related JPA repository: [UserAuditableRepository](../domain/src/main/java/org/fao/geonet/repository/UserAuditableRepository.java).
- The auditable service: [UserAuditableService](src/main/java/org/fao/geonet/auditable/UserAuditableService.java).
- The users API updated to use the auditable service: [UserApi](../services/src/main/java/org/fao/geonet/api/users/UsersApi.java).




