# Writing documentation

!!! warning

    Warning "Deprecated"
    
    The Sphinx writing guidelines have been replaced by Markdown Guidelines


This section provides some guidelines for writing consistent documentation for GeoNetwork.

!!! info "See Also"

    The quickest and easiest way to contribute to the documentation is to sign up for a [GitHub account](https://github.com/) and edit the documentation pages by clicking the "Edit on GitHub" link at the top of the page. (See `doc`{.interpreted-text role="repo"}).


## Building the docs

The following tools are required to build the documentation from source:

-   **Java JDK 1.8**, **Python**
-   **Maven 3.1.0+**: Once installed [Maven](https://maven.apache.org) should be available in your command shell as `mvn`.
-   **make**: Once installed [make](https://www.gnu.org/software/make/) should be available in your command shell as `make`.
-   **Sphinx**: See [Sphinx](https://www.sphinx-doc.org/en/master/usage/installation.html) for details. Once installed confirm it's available by running `sphinx-build --version`.
-   **sphinx-bootstrap-theme**.

To install the build tools:

``` shell
sudo apt install make
sudo apt install python3-pip
sudo pip install sphinx
sudo pip3 install sphinx-bootstrap-theme
sudo pip3 install sphinx_rtd_theme
```

Then build the documentation in English using the following commands:

``` shell
git clone https://github.com/geonetwork/doc
cd doc
mvn clean install
```

Once the documentation has built without errors, access the html files from `doc\target\en\index.html`.

To build all documentation in several languages (right now: es,fr,ge,it,ko,nl,cz,ca,fi,is), based on Transifex translations:

``` shell
mvn clean install -Pall
```

If you want to get the latest translations for your build, run:

``` shell
mvn clean install -Platest,all
```

## Building the standards docs

!!! info "Important"

    Documentation about the standards and the editor configuration is built from the GeoNetwork source code using the `schema-doc` plugin. So do not manually edit the following files:


-   docs/manuals/source/annexes/standards/*
-   docs/manuals/source/customizing-application/editor-ui/creating-custom-editor.rst

Do not translate those files in Transifex. Translation must be done in the plugin itself. To update those files, clone the GeoNetwork repository and use the following commands:

``` shell
cd docs/schema-doc
mvn clean install
```

Check the updated files and commit to the doc repository.

## Editing the reStructuredText files

To update the documentation, use a text editor to edit `.rst` files. Ensure you are using the correct terminology by checking [style-guide](style-guide.md). Save your changes, build the documentation and open the HTML files to preview the changes. When your changes are ready to be submitted to the project, follow the steps in [making-a-pull-request](making-a-pull-request.md).

## Sphinx

This section gives some useful tips about using Sphinx.

### Don't introduce any new warnings

When building the docs, Sphinx prints out warnings about broken links, syntax errors and so on. Don't introduce new ones.

It's best to delete the build directory and completely rebuild the docs, to check for any warnings:

``` shell
mvn clean install
```

### Links

#### Images

Place images in an `img` folder in the directory where the rst file is located. Use images with:

``` rst
.. figure:: img/thumbprint.png
```

#### Code block

Use the following directive to highlight code block:

``` rst
.. code-block:: xml
```

#### Reference to a section within a file

When creating a new page, add a reference on top of the file:

``` rst
.. _writing-documentation:
```

This reference could then be used to link to that page or section:

> :ref:`writing_documentation`

#### Link to GitHub resources

The conf.py contains a set of [external links definition](http://sphinx-doc.org/latest/ext/extlinks.html).

``` rst
* :issue:`123` to link to an issue
* :pr:`123` to link to a pull request
* :code:`web/pom.xml` to link to a file in the source code
* :repo:`schema_plugins` to link to a repository
* :wiki:`Meeting2015Bern` to link to a wiki page
```

Example, link to the Bern User Meeting (See `Meeting2015Bern`{.interpreted-text role="wiki"}).

### Substitutions

[Substitutions](http://sphinx-doc.org/rest.html#substitutions) are useful to define a value that's needed in many places (eg. the location of a file, etc.).

The values are defined in `rst_epilog` in conf.py:

``` rst
.. |jdbc.properties| replace:: WEB-INF/config-db/jdbc.properties
```

Use them when appropriate:

``` rst
Configure the database in |jdbc.properties| ...


After installation look to |install.homepage|_ on your web browser.
```

### versionadded, versionchanged and deprecated

Use Sphinx's `versionadded` and `versionchanged` directives to mark new or changed features. For example:

``` rst
Creating overview from WMS
==========================

.. versionadded:: 3.0

In the *add overview panel*, select the *add from WMS* link to create
an image from the WMS referenced in the metadata record to illustrate
the dataset in a specific area.

...
```

When using the `versionchanged` directive, a sentence explaining what changed is usually relevant:

``` rst
Configuring LDAP
================

.. versionchanged:: 2.10.0
   Previous versions was setting LDAP parameters from the administration
   panel.

...
```

Use `deprecated` directive when a feature is no longer available.

### seealso

Many sections include a list of references to module documentation or external documents. These lists are created using the `seealso` directive typically placed in a section just before any subsections.

## Translating the doc

[Github doc repository](https://github.com/geonetwork/doc) contains the English version of the documentation. All translations should be done on Transifex web interface. No properties files should be committed to this repository.

If you add some new section or update the text on an existing section, you have to update the Transifex fields to make sure this change is spread to all languages. To achieve this, execute:

To download the translations from Transifex, you will need the Transifex command line client: <https://docs.transifex.com/client/installing-the-client>. The Transifex Client is written in Python, so it runs on most systems. The easiest way to install it is with pip.

To install the build tools:

``` shell
sudo pip install sphinx-intl
sudo pip install transifex-client
```

Once installed, you need to configure your Transifex user: <https://docs.transifex.com/client/client-configuration> in ``\~/.transifexrc``. This config file is unique per user, and it is stored in your home directory.

``` none
[https://www.transifex.com]
username = your_username/api
token =
password = p@ssw0rd/api_token
hostname = https://www.transifex.com
```

Update translations on Transifex:

``` shell
make update_translations
```

If you want to add a new language to the build, you will have to edit the file <https://github.com/geonetwork/doc/blob/develop/Makefile#L65> and add the languages you want to build the documentation for.

If you want it to be publicly available on <https://geonetwork-opensource.org> webpage, make sure you make a PR with the change and ask for advice on the <https://github.com/geonetwork/website> project.
