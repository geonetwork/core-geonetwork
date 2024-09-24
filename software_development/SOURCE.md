# Source Code

## Check out source code

Clone the repository:

```
git clone --recursive https://github.com/geonetwork/core-geonetwork.git
```

And build:

```
cd core-geonetwork
mvn clean install -DskipTests
```

## Submodules

GeoNetwork use submodules, these were initialized by the ``--recursive`` option when cloning the repository.

If you missed using ``--recursive`` run the following:

```
cd core-geonetwork
git submodule init
git submodule update
```

Submodules are used to keep track of external dependencies. It is necessary to init and update them after a branch change:


```
  git submodule update --init
```

Remember to rebuild the application after updating external dependencies.
