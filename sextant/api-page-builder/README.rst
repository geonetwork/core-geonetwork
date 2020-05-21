Sextant API page builder
========================

This simple utility fetches live pages using Sextant in API mode,
and process their HTML so that they can be used locally to check
for CSS/JS conflicts with the HTML surrounding Sextant.

A configuration file has to be provided on the following model:

.. code:: yaml

  # dict of live API sites from which test pages will be built
  sites:
    sextant: https://sextant.ifremer.fr/Donnees/Catalogue
    milieumarin: https://sar.milieumarinfrance.fr/Nos-rubriques/Referentiels-geographiques
    etc.

  # Sextant host used in the above sites; this should include a scheme (https:// or http://) or start with //
  live_host: //sextant.ifremer.fr

  # Sextant host to be used for the generated test pages; this should include a scheme (https:// or http://) or start with //
  test_host: http://localhost:8080

Note that a sample file is available at `conf/sample.yaml`.

Install dependencies
--------------------

.. code::

  pip3 install -r requirements.txt


Run
---

This will create the destination directory if needed and generate the pages in it.

.. code::

  ./run.py --input conf/sample.yaml --output 'dest_dir/'


Test
-----

To run the tests:

.. code::

  python3 -m pytest tests

This requires having the pytest package installed locally.
