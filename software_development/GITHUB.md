# GitHub

## Forks, Pull requests and branches

If you want to contribute back to GeoNetwork you create a Github account, fork the GeoNetwork repository and work on your fork. This is a huge benefit because you can push your changes to your repository as much as you want and when a feature is complete you can make a 'Pull Request'.  Pull requests are the recommended method of contributing back to GeoNetwork because Github has code review tools and merges are much easier than trying to apply a patch attached to a ticket.

The GeoNetwork Repository is at: https://github.com/geonetwork/core-geonetwork.

Follow the instructions on the Github website to get started (make accounts, how to fork etc...) 

* http://help.github.com/

If you cloned the GeoNetwork Repository earlier, you set you can now set your fork up as a remote and begin to work.

Rename the GeoNetwork repository as ``upstream``:

     git remote rename origin upstream

Add your fork as origin (the URL provided by GitHub CLONE or DOWNLOAD button):

     git remote add origin https://github.com/USERNAME/core-geonetwork.git

List remotes showing ``origin`` and ``upstream``:

     git remote -v
     
To checkout a branch from upstream::

     git checkout -t upstream/3.6.x

## Pull Requests

GeoNetwork uses feature branches for development, and a pull-request workflow for review:

* [Contributing](../CONTRIBUTING.md).
* [Making a pull request](https://docs.geonetwork-opensource.org/latest/contributing/making-a-pull-request/).

## Automation

### Quality Assurance

A number of [workflows](../.github/workflows/) are setup to ensure each PR compiles, passes tests and so forth.

* [linux.yml](../.github/workflows/linux.yml): build and QA, including -Drelease check
* [docs.yml](../.github/workflows/docs.yml): publish docs to gh-pages branch
* [backport.yml](../.github/workflows/backport.yml): backport tagged pull requests

### Tags

Use backport tags to take advantage of the [backport.yml](https://github.com/m-kuhn/backport) automation:

> Backport is a JavaScript GitHub Action to backport a pull request by simply adding a label to it.
> 
> It can backport rebased and merged pull requests with a single commit and squashed and merged pull requests. I

This agrees with our CONTRIBUTING policy of using rebase and squash and merge.
