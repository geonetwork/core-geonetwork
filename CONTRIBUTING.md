# Contributing

Thank you for contributing to GeoNetwork!

* Free-software: GeoNetwork is free-software, using the [GNU GENERAL PUBLIC LICENSE](LICENSE.md).
* Contributions provided by you, or your employer, are required to be compatible with this free-software license. You will therefor be asked to sign the [Contributor License Agreement](https://cla-assistant.io/geonetwork/) when you are contributing to the repositories. This process is automatically enabled when you create your first pull request via https://cla-assistant.io/.
* Pull-request: GeoNetwork uses a pull-request workflow to review and accept changes. Pull-requests must be submitted against the *main* branch first, and may be back ported as required.

# Pull requests

* Pull request is required, even if you have commit access, so the tests are run and another developer can check your code.

* Pull requests must be applied to `main`, before being backported.

* Before merging a pull request, the following properties should be defined:

  - Milestone to include the change.
  - Add the label `changelog` when the change is relevant to be added to the release changelog file.
  - Add `backport <branch>` to indicate when the change is a bug fix or when it is a small improvement that is relevant to be backported.

* Good housekeeping: Anytime you commit, try and clean the code around it according to the latest style guide. If you improve a function without comments: _add comments!!_ If you modify functionality that does not have tests: _write a test!!_ If you fix functionality without documentation: _add documentation!!_
  
* History: Clean commit messages and history. Avoid big commits with hundreds of files, break commits up into understandable chunks. Longer, verbose commit messages are encouraged. Avoid reformatting and needless whitespace changes.

* Draft: Use pull request **Draft** (or even the text "WIP") to identify _Work In Progress_.
  
* Rebase / Squash and merge: Do not merge commits with the current branch, use Rebase or Squash and merge!
  
* API Changes: Please identify any API change or behavior changes in commit messages. Also make sure that a [process for deprecation](PROCESS_FOR_DEPRECATION.md) of a feature is carefully dealt with.

* Review: Review is required by another person, or more than one! Don't be shy asking for help or reviewing.

* Testing: All new features or enhancements require automatic tests (see [testing](software_development/TESTING.md))

* User documentation: All new features or enhancements require documentation (see [doc](https://github.com/geonetwork/doc))

* Build documentation: All build and development instructions managed in repository `README.md` files.

* New libraries: Do not commit jars, use maven `pom.xml` to declare dependencies where needed, and `src/pom.xml` dependency management to manage version numbers. Use build documentation to share what the library does in GeoNetwork, how it interacts or extends GeoNetwork, with references to official library tutorials or documentation.

For more information see [How to contribute](https://github.com/geonetwork/core-geonetwork/wiki/How-to-contribute) (wiki).
