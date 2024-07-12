# Making a pull request

GeoNetwork uses a pull-request workflow allowing changes to be managed and reviewed. All work is done on branches and merged back. When developing start with the branch you want changed, create a new feature branch from there, make your changes on the feature branch, publish your feature branch to your GitHub repo, make a Pull Request asking your changes to be reviewed and merged.

Occasionally core GeoNetwork developers will setup a feature branch on upstream to explore a specific topic. These shared feature-branch are subject to review when submitted as a pull-request against `master`.

There are many great guides (See the links above) but here is a quick sequence illustrating how to make a change and commit the change.

``` shell
$ git checkout master
   # master is the 'trunk' and main development branch
   # the checkout command "checks out" the requested branch

$ git checkout -b myfeature
   # the -b requests that the branch be created
   # ``git branch`` will list all the branches you have checked out locally at some point
   # ``git branch -a`` will list all branches in repository (checked out or not)

# work work work

$ git status
   # See what files have been modified or added

$ git add <new or modified files>
   # Add all files to be committed ``git add -u`` will add all modified (but not untracked)

$ git commit -m "<a short message describing change>"
   # Commit often.  it is VERY fast to commit
   # NOTE: doing a commit is a local operation.  It does not push the change to Github

# more work
# another commit

$ git push origin myfeature

   # this pushed your new branch to Github
   # now you are ready to make a Pull Request to get the new feature added to GeoNetwork

# revise pull request based on review feedback
# another commit
# another push to update pull request
# Success!!
```

GeoNetwork uses git submodules in order to keep track of externals dependencies. It is necessary to init and update them after a branch change:

``` shell
git submodule update --init
```
