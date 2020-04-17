import setuptools
import os

here = os.path.abspath(os.path.dirname(__file__))

# with open("README.md", "r") as fh:
#    long_description = fh.read()

with open(os.path.join(here, "requirements.txt")) as f:
    requires = f.read().splitlines()

setuptools.setup(
    name="sextant-api-validator",  # Replace with your own username
    version="0.0.1",
    author="Camptocamp",
    author_email="julien.waddle@camptocamp.com",
    description="Site Api validation for sextant",
    long_description="",
    long_description_content_type="text/markdown",
    url="https://github.com/julsbreakdown/ndjson4addok",
    packages=setuptools.find_packages(),
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    python_requires=">=3.6",
    install_reqs=requires,
)
