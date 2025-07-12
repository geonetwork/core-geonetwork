# Author {#authors}

GeoNetwork was started by Jeroen Ticheler in 2001 as project of the Food and Agriculture Organization of the United Nations (FAO-UN), United Nations World Food Programme (WFP) and United Nations Environment Programme (UNEP).

The open-source project was initially managed by Jeroen Ticheler and then donated to the Open Source Geospatial Foundation. GeoNetwork [graduated](https://www.osgeo.org/foundation-news/geonetwork-opensource-graduates-osgeo-incubation/) as full Open Source Geospatial project in 2008 and has been managed by a project steering committee and active committers in subsequent years.

## Leadership team

The GeoNetwork project roadmap is managed using a proposal process, with the GeoNetwork leadership team voting on proposals made by members of our community. This process has moved from mailing list to a [project board](https://github.com/orgs/geonetwork/projects/2).

Active maintainers in GeoNetwork project have taken on responsibility for reviewing community contributions, making releases, handling security vulnerability reports.

This dedication is reflected in community trust, and a Leadership role with oppertunity to vote on roadmap proposals:

* [David Blasby](https://github.com/davidblasby): [Active Maintainer](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+is%3Aclosed+reviewed-by%3Adavidblasby)
* [Jose García](https://github.com/josegar74): [Active Maintainer](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+is%3Aclosed+reviewed-by%3Ajosegar74)
* [Jody Garnett](https://github.com/jodygarnett): [Active Maintainer](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+is%3Aclosed+reviewed-by%3Ajodygarnett)
* [François Prunayre](https://github.com/fxprunayre): [Active Maintainer](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+is%3Aclosed++reviewed-by%3Afxprunayre)
* [Juan Luis Rodríguez Ponce](https://github.com/juanluisrp): [Active Maintainer](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+is%3Aclosed+reviewed-by%3Ajuanluisrp)

Chair:

* [Jeroen Ticheler](https://github.com/ticheler) - [OSGeo Project Officer](https://www.osgeo.org/about/board/)

!!! note

    Jeroen Ticheler is our Open Source Geospatial Foundation ["project officer"](https://www.osgeo.org/about/board/) representative. As acting chair, Jeroen is available to cast a tie breaking vote if required.

!!! note

    Maintainer activity are determined over six months:
    
    Using GitHub API for reviewers in the last 6 months, avoiding double counting, and sorting the results: [maintainers.sh](maintainers.sh){:download}
    ```
    Reviews since 2025-01-01
     65 josegar74
     29 fxprunayre
     15 jodygarnett
     15 ianwallen
     12 davidblasby
     11 juanluisrp
      7 tylerjmchugh
      6 wangf1122
      6 joachimnielandt
      5 jahow
      3 xiechangning20
      3 PascalLike
      3 github-advanced-security[bot]
      2 Guillaume-d-o
      1 sebr72
      1 cmangeat
    ```

    We aim for an effective leadership team around 5 individuals.
    
!!! note

    To check your own activity:
    
    * GN4: [is:pr is:closed  reviewed-by:@me](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+is%3Aclosed++reviewed-by%3A%40me+base%3Amain+merged%3A%3E%3D2025-01-01)
    * GN5: [is:pr is:closed  reviewed-by:@me](https://github.com/geonetwork/geonetwork/pulls?q=is%3Apr+is%3Aclosed++reviewed-by%3A%40me+base%3Amain+merged%3A%3E%3D2025-01-01)
    
    Using the `gh` command line:
    ```bash
    gh pr list --repo geonetwork/core-geonetwork --state closed --search "merged:>=$(date -v -6m +%Y-%m-01) reviewed-by:@me base:main"
    gh pr list --repo geonetwork/geonetwork --state closed --search "merged:>=$(date -v -6m +%Y-%m-01) reviewed-by:@me base:main"
    ```

## Developer Roles

Developer roles are based on trust given the level of responsibility required. If you are interested in participating please contact the [developer forum](https://discourse.osgeo.org/c/geonetwork/developer/58).

Developers are nominated for a role, self nomination is fine, reflecting ongoing commitment and community trust.

Roles reflect increased project access as indicated by the GitHub Teams below:

| GitHub Team | Responsibility | Trust | Project Access | Roadmap |
| ----- | -------------- | ----- | -------------- | ------- |
| [Wiki](https://github.com/orgs/geonetwork/teams/wiki) | edit wiki | Low | Enough commit permission to edit wiki | Non-voting |
| [Build](https://github.com/orgs/geonetwork/teams/build) | build, automations, and release | Low | Access to manage workflows, branches and package releases | Non-voting |
| [Project](https://github.com/orgs/geonetwork/teams/project) | project board | Low | Project board and issue triage | Non-voting |
| [Security](https://github.com/orgs/geonetwork/teams/security) | Review security vulnerabilities | High | Security advisories | Non-voting |
| [Maintainer](https://github.com/orgs/geonetwork/teams/maintainer) | Review pull-requests | Highest | Merge pull requests | Voting |

!!! note
   
    Developers are expected remain active, meeting responsibilities to review pull requests, address security vulnerabilities, and vote on roadmap proposals.
    
    We understand that life can get in the way of community particpation – you are welcome to rejoin as your availability permits you to meet the responsibilities.

## Contributors

GeoNetwork uses a pull-request workflow to review and accept changes. Pull-requests must be submitted against the main branch first, and may be back ported as required.

For larger changes we ask that a proposal be submitted to the roadmap for discussion.

Reference:

- [Contributors](https://github.com/geonetwork/core-geonetwork/graphs/contributors) (GitHub Insights)
- [CONTRIBUTING.md](https://github.com/geonetwork/core-geonetwork/blob/main/CONTRIBUTING.md) (GitHub Repository)
- [Contributing Guide](../contributing/index.md) (GeoNetwork Documentation)

## Thanks

We would like to thank prior contributors, committers, and leadership team for their role in making GeoNetwork a success:

* Andrea Carboni
* Antonio Cerciello
* Jo Cook
* Jesse Eichar
* Michel Gabriel
* Florent Gravin
* Craig Jones
* Pierre Mauduit
* Patrizia Monteduro
* Simon Pigot
* Emanuele Tajariol
* Archie Warnock
* Maria de Reyna Arias
* Paul van Genuchten
