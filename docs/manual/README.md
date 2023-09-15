# Geonetwork-opensource Manual

Documentation for GeoNetwork opensource is available via https://geonetwork-opensource.org.

This documentation is written under the creative commons license [Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0)](LICENSE.md).

Reference:

* [Documentation Writing Guide](docs/devel/docs/docs.md)

## Communication

The [project issue tracker](https://github.com/geonetwork/core-geonetwork/issues) is used for communication, with ongoing topics tagged [documentation](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+label%3Adocumenation).


## Maven Integration

1. Build documentation with ``compile`` phase:
   ```
   mvn compile
   ```

2. Assemble ``zip`` with ``package`` phase:
   ```bash
   mvn compile
   ```

3. Both ``install`` and ``deploy`` are skipped (so ``mvn clean install`` is fine).

4. Use profile to include specific language:
   ```
   mvn install -Pfrench
   ```

   Or flag for all of them:
   ```
   mvn install -Dall
   ```

## Material for MkDocs

Documentation is [mkdocs-material](https://squidfunk.github.io/mkdocs-material/) which is a Markdown documentation framework written on top of [MkDocs](https://www.mkdocs.org/).

If you are familiar with python:

1. Install using ``pip3`` and build:

   ```bash
   pip3 install -r requirements.txt
   ```

2. Use ***mkdocs** to preview locally:

   ```bash
   mkdocs serve
   ```

3. Preview: http://localhost:8000

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

If you are not familiar with python the mkdocs-material website has instructions for docker:

1. Run mkdocs in Docker environment:

   ```
   docker pull squidfunk/mkdocs-material
   docker run --rm -it -p 8000:8000 -v ${PWD}:/docs squidfunk/mkdocs-material
   ```
   
2. Preview: http://localhost:8000

