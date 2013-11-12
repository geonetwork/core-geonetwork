/**
 * All *Repository objects are Data access objects for search and loading entities in the domain packages.
 * <p>
 * The objects are created using spring-data-jpa and thus have the same design.
 * </p><p>
 * The objects *RepositoryCustom are the interfaces for custom query methods for a repository. In
 * other words, queries that cannot be defined via the normal spring-data query mechanism.
 * </p>
 * The objects *RepositoryImpl are the implementations of *RepositoryCustom.  spring-data wires together
 * *Repository, *RepositoryCustom and *RepositoryImpl together so as an end user you only need to concern
 * yourself with the *Repository interface.
 * </p><p>
 * For more information on implementation details see the spring-data-jpa documentation.
 * </p>
 *
 * @author Jesse
 */
package org.fao.geonet.repository;