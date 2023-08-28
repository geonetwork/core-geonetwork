# Online Help

Online-help provided for end-users. The user manual explores catalog use using ISO19139 examples.

This is an end-user supliment to the far more technical [GeoNetwork User and Developer Manuals](https://geonetwork-opensource.org/manuals/trunk/en/index.html).

Online help is intended for local installation with each instance of core-geonetwork.

## Communication

The [project issue tracker](https://github.com/geonetwork/core-geonetwork/issues) is used for communication, with ongoing topics tagged [online-help](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+label%3Aonline-help).

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

Reference:

* [Documentation Writing Guide](../manual/docs/devel/docs/docs.md)

