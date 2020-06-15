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

Submodules
----------

GeoNetwork use submodules, these were initiziled by the ``--recursive`` option when cloning the repository.

If you missed using ``--recursive`` run the following:

.. code-block:: shell

  cd core-geonetwork
  git submodule init
  git submodule update

Submodules are used to keep track of externals dependencies. It is necessary to init and update them after a branch change:


.. code-block:: shell

  git submodule update --init


Remember to rebuild the application after updating external dependencies.
