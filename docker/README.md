# Introduction

Cette documentation fournit les étapes à suivre afin d'obtenir une composition
docker fonctionnelle comportant les éléments suivants:

* Sextant (GN fork)
* CAS (version 6.2.2)
* LDAP (contenant les schémas ifremer)
* PostGreSQL / PostGIS
* Elasticsearch

# Compilation

Il est nécessaire de compiler la webapp au préalable, à la racine du dépot, via
la commande suivante:

```
$ mvn clean install -U -DskipTests -Dsxt.properties=docker
```

Puis de copier la webapp résultante dans le répertoire geonetwork (`docker/geonetwork`):

```
$ cd docker
$ cp ../web/target/geonetwork.war geonetwork/geonetwork.war
```

# Docker / Docker-compose

## Docker

Afin de construire l'image docker, il suffit de lancer la commande suivante
(toujours dans le répertoire `docker/`):

```
$ docker-compose build
```

L'image obtenue aura alors le tag suivant: `sextant-geonetwork:elastic`.

## Docker-compose

Un exemple d'utilisation de l'image docker précédemment construite est fourni
via le fichier `docker-compose.yaml`.

Le service `geonetwork` (dans la section `environment` du fichier YAML) indique
quelles variables d'environnement peuvent être positionnées pour adapter la
configuration de l'image docker.


# Utilisateurs

Des utilisateurs de test sont créés dans le LDAP. Vous pouvez retrouver la
specification LDIF dans le répertoire suivant:
`core/src/test/docker/ldap/ldif`.

Les mots de passe sont en général les mêmes que l'identifiant `uid` (par
exemple l'utilisateur 'pmauduit' a pour mot de passe 'pmauduit', l'utilisateur
admin a pour mot de passe 'admin', ...).

# Remote debug

Le conteneur geonetwork écoute sur le port 5005, sur lequel il est possible de
connecter une socket de debug (JDWP). Ce port est mappé avec le port 5006 de
l'hôte.

Sur le même principe, le conteneur CAS est connecté sur le port 5005 de l'hôte
(vers le port 5005 du conteneur).
