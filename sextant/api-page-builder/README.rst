Api Validator
======================

Build
-----

.. code::

  sudo pip3 install -e api_validator


Run
---

.. code::

  ./run.py -i data/sites.yaml -o 'dest_dir/' --psxl http://localhost:8080 --psxr http://sextantapi:8080 --psl "/" --psr https://www.seadatanet.org/

..


Test
-----

.. code::

  make build && make test
