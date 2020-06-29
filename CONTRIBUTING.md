# Contributing

Thank you for contributing to GeoNetwork:

* Free-software: GeoNetwork is free-software, using the [GNU GENERAL PUBLIC LICENSE](LICENSE.md). Contributions provided by you, or your employer, are required to be compatible with this free-software license.
* Pull-request: GeoNetwork uses a pull-request workflow to review and accept changes. Pull-requests must be submitted against the *master* branch first, and may be back ported as required.

# Pull requests

* Pull request is required, even if you have commit access, so the tests are run and other developer can check your code.

* Pull requests must be applied to `master`, before being backported.

* Good housekeeping: Anytime you commit, try and clean the code around it to latest style guide. If you improve a function without comments: add comments. If you modify functionality that does not have tests: write a test. If you fix functionality without documentation: add documentation.
  
* History: Clean commit messages and history: avoid big commits with hundreds of files, break commits commits up into understandable chunks, longer verbose commit messages are encouraged. Beware of reformatting and needless whitespace changes.

* Draft: Use pull request *Draft** (or even the text "WIP") to identify work in progress.
  
* Rebase: No merge commits with current branch: use rebase!
  
* API Change: Please identify any API change or behavior changes in commit messages.

* Review: Review is required by another person, or more than one! Don't be shy asking for help or reviewing.

* Testing: All new features or enhancements require automatic tests (see [testing](software_development/TESTING.md))

* User documentation: All new features or enhancements require documentation (see [doc](https://github.com/geonetwork/doc))

* Build documentation: All build and development instructions managed in repository `README.md` files.

* New libraries: Do not commit jars, use maven `pom.xml` to declare dependencies where needed, and `src/pom.xml` dependency management to manage version numbers. Document what the library does in GeoNetwork, how it interacts or extends GeoNetwork, with references to official library tutorials or documentation.

For more information see [How to contribute](https://github.com/geonetwork/core-geonetwork/wiki/How-to-contribute) (wiki).
