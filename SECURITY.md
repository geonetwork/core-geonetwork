# Security Policy

The GeoNetwork community takes the security of the software and all services based on the software product seriously. On this page you can find the versions for which the community provides security patches. 

If you believe you have found a security vulnerability in the software or an implementation of the software, please report it [here](https://github.com/geonetwork/core-geonetwork/security/advisories/new) as described below. Do not publish the vulnerability in any public forums (such as social media, user forum, or issue tracker).

## Supported Versions

Each GeoNetwork release is supported with bug fixes for a limited period, with patch releases made approximately every three to six  months. 

- We recommend to update to latest incremental release as soon as possible to address security vulnerabilities.
- Some overlap is provided when major versions are announced with both a current version and a maintenance version being made available to provide time for organizations to upgrade.

| Version | Supported          | Comment                                     |
|---------|--------------------|---------------------------------------------|
| 4.4.x   | :white_check_mark: | Latest version                              |
| 4.2.x   | :white_check_mark: | Stable version                              |
| 3.12.x  | ❌  | End Of Life 2024-03-31 |

If your organisation is making use of a GeoNetwork version that is no longer in use by the community all is not lost. You can volunteer on the developer list to make additional releases, or engage with one of our [Commercial Support](https://www.osgeo.org/service-providers/?p=geonetwork) providers. 

## Reporting a Vulnerability

If you encounter a security vulnerability in GeoNetwork please take care to report in a responsible fashion:

* Keep exploit details out of mailing list and issue tracker (instead provide details to the Project Steering Committee via the GitHub [Report a vulnerability](https://github.com/geonetwork/core-geonetwork/security/advisories/new) option link at the top of this page or send an email to geonetwork@osgeo.org)
* Be prepared to work with community members on a solution
* Keep in mind that community members are volunteers and an extensive fix may require fundraising / resources

For more information see [How to contribute](https://github.com/geonetwork/core-geonetwork/wiki/How-to-contribute).

## Coordinated vulnerability disclosure

Disclosure workflow:

1. GitHub [security advisory](https://github.com/geonetwork/core-geonetwork/security) used to reserve a CVE number.
2. Vulnerability addressed and backported to "latest" and "stable" branches, allowing origional reporter to verify nightly build.
3. Fix available in published release for all "supported versions" identified above, providing an opportunity for everyone to update.
4. The CVE vulnerability is published with mitigation and patch instructions.

This approach provides everyone a chance to update prior to public disclosure.

Those seeking greater transparency are encouraged to [volunteer as a committer](CONTRIBUTING.md#core-commit-access), or work with one of the [commercial support provides](https://www.osgeo.org/service-providers/?p=geonetwork) to particiapte on your behalf.
