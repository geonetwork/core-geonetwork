# GeoNetwork Manual and Help

Documentation for GeoNetwork opensource is available via https://docs.geonetwork-opensource.org.

This documentation is written under the Creative Commons license [Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0)](LICENSE).

Reference:

* [Documentation Writing Guide](docs/devel/docs/index.md)

## Communication

The [project issue tracker](https://github.com/geonetwork/core-geonetwork/issues) is used for communication, with ongoing topics tagged [documentation](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+label%3Adocumenation).

## Material for MkDocs

Documentation is [MkDocs-material](https://squidfunk.github.io/mkdocs-material/) which is a Markdown documentation framework written on top of [MkDocs](https://www.mkdocs.org/).

If you are using Python3:

1. Install using ``pip3`` and build:

   ```bash
   pip3 install -r requirements.txt
   ```
   
   In the future you can update using:
   
   ```bash
   pip3 install -r requirements.txt -U 
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

If you use a Python virtual environment:

1. Activate virtual environment:

   ```bash
   virtualenv venv
   source venv/bin/activate
   pip install -r requirements.txt
   ```
   
   In the future you can update with:
   
   ```bash
   source venv/bin/activate
   pip3 install -r requirements.txt
   ```
   
2. Use ***mkdocs*** to preview from virtual environment:

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

4. Use default profile to only build the default English docs:

   ```
   mvn install -Pdefault
   ```
   
## Publish Documentation

We use ``mike`` for publishing (from the `gh-pages` branch). Docs are published by the ``.github/workflows/docs.yml`` automation each time pull-request is merged.

If you wish to preview using your own `gh-pages` branch:

1. To deploy documentation for 3.12 branch:
   
   ```bash
   mike deploy --push 3.12 
   ```
   
2. To show published versions:

   ```bash
   
   mike list
   ```

3. To preview things locally (uses your local ``gh-pages`` branch):
   
   ```bash
   mike serve
   ```

Reference:

* https://squidfunk.github.io/mkdocs-material/setup/setting-up-versioning/
* https://github.com/squidfunk/mkdocs-material-example-versioning
* https://github.com/jimporter/mike
