# Introduction

Cette documentation fournit les étapes à suivre afin d'obtenir une composition
docker fonctionnelle comportant les éléments suivants:

* Sextant (GN fork)
* PostGreSQL / PostGIS
* Elasticsearch

# Compilation

Il est nécessaire de compiler la webapp au préalable, à la racine du dépot, via
la commande suivante:

```
$ mvn clean install -U -DskipTests -Dmaven.test.skip=true -Dsxt.properties=ifr-test -Pes
```

Puis de copier la webapp résultante dans le répertoire courant (`docker`):

```
$ cp ../web/target/geonetwork.war .
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

