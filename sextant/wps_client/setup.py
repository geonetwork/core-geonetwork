#-*- coding: utf-8 -*-

from setuptools import setup, find_packages

setup(
    zip_safe=False,
    name="wps_client",
    version_config=True,
    setup_requires=['setuptools-git-versioning'],
    author="Mickaël TREGUER <mtreguer@ifremer.fr>",
    author_email="Mickaël TREGUER <mtreguer@ifremer.fr>",
    packages=find_packages('.', exclude=['tests']),
    entry_points={
        'console_scripts': [
            'process_wps_execute = wps_client.client_wps:process_wps_execute'
        ],
    },
    install_requires=[
      "urllib3",
      "OWSLib"
    ],
)

