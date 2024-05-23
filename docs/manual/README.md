# Geonetwork Manual and Help

Documentation for GeoNetwork opensource is available via https://geonetwork-opensource.org.

This documentation is written under the creative commons license [Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0)](LICENSE.md).

Reference:

* [Documentation Writing Guide](docs/devel/docs/docs.md)

## Communication

The [project issue tracker](https://github.com/geonetwork/core-geonetwork/issues) is used for communication, with ongoing topics tagged [documentation](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+label%3Adocumenation).

## Material for MkDocs

Documentation is [mkdocs-material](https://squidfunk.github.io/mkdocs-material/) which is a Markdown documentation framework written on top of [MkDocs](https://www.mkdocs.org/).

If you are using python3:

1. Install using ``pip3`` and build:

   ```bash
   pip3 install -r requirements.txt
   ```

2. Use ***mkdocs** to preview locally:

   ```bash
   mkdocs serve
   ```
      
3. Preview: http://localhost:8000

   Preview uses a single version, so expect some warnings from version chooser:
   ```
   "GET /versions.json HTTP/1.1" code 404
   ```

4. Optional: Preview online help:
   
   ```bash
   mkdocs serve --config-file help.yml  
   ```

### VirtualEnv

If you use a python virtual environment:

1. Activate virtual environment:

   ```bash
   virtualenv venv
   source venv/bin/activate
   pip install -r requirements.txt
   ```
   
2. Use ***mkdocs*** to preview from virtual environment:

   ```bash
   mkdocs serve
   ```

3. Preview: http://localhost:8000

### Docker

If you are not familiar with python the mkdocs-material website has instructions for docker:

1. Run mkdocs in Docker environment:

   ```
   docker pull squidfunk/mkdocs-material
   docker run --rm -it -p 8000:8000 -v ${PWD}:/docs squidfunk/mkdocs-material
   ```
   
2. Preview: http://localhost:8000

## Maven Integration

1. Build documentation with ``compile`` phase:
   ```
   mvn compile
   ```

2. Assemble ``zip`` with ``package`` phase:
   ```bash
   mvn package
   ```

3. Both ``install`` and ``deploy`` are skipped (so ``mvn clean install`` is fine).

4. Use default profile to only build the default english docs:

   ```
   mvn install -Pdefault
   ```
   
## Publish Documentation

We use ``mike`` for publishing (from the `gh-pages` branch). Docs are published by the ``.github/workflows/docs.yml`` automation each time pull-request is merged.

If you wish to preview using your own `gh-pages` branch:

1. To deploy 4.4 docs as latest from the `main` branch to website `gh-pages` branch:

   ```bash
   mike deploy --title "4.4 Latest" --alias-type=copy --update-aliases 4.4 latest
   ```

2. To deploy documentation for a new release:
   
   ```bash
   mike deploy --push --alias-type=copy 4.2 stable
   ```
   
3. When starting a new branch you can make it the default:
   
   ```bash
   mike set-default --push 4.6
   ```
    
   Hint: When starting a new branch update `overview/changelog/history/index.md` headings for latest, maintenance, stable (for prior branches also).

4. To publish documentation for a maintenance release:

   ```bash
   mike deploy --push --alias-type=copy 3.12 maintenance
   ```

5. To show published versions:

   ```bash
   
   mike list
   ```

6. To preview things locally (uses your local ``gh-pages`` branch):
   
   ```bash
   mike serve
   ```

Reference:

* https://squidfunk.github.io/mkdocs-material/setup/setting-up-versioning/
* https://github.com/squidfunk/mkdocs-material-example-versioning
* https://github.com/jimporter/mike
