# Git Flow

We will be using Git to version control our project, to ensure that we
keep our repository clean and manageable we will use `Git Flow`, a
widely recognised branching/release strategy based on two trunk
branches `main` and `develop`.

TL;DR: essentially it's the same as what we're used to doing, but
instead of branching from and merging into `main`, we branch from and
merge into `develop`. We then have a separate release process to merge
`develop` into `main` which gives us a bit of a guarantee that `main`
always stays in a good state.

## The trunk branches

The `main` branch always contains code that is _currently deployed
right now_, it is always kept in a production ready state. No one can
commit to `main`, and it can only be updated by performing a `release`
(see below). In addition we do not branch off from `main` for feature
work, only in extreme circumstance for `hotfixes` (see below).

The `develop` branch contains the code that will be in the next
release. We branch off from `develop` to create feature branches and
we merge these back into `develop` when the feature is complete.

## Releases

When we are happy with the code that is in `develop` we create a
release branch named `release/x.y.z` where `x.y.z` is the new version
number. We test this branch until we are happy that it is working (any
bug fixes on this branch can be continuously merged back into
develop). Once our release branch is good we can merge it into `main`
and we tag `main` with the new version number. This can trigger
whatever deployment pipelines we have in place as we are sure that
this code is working and should be deployed. The release branch must
_also_ be merged back into `develop` so develop remains up to date.

## Hotfixes

In the hopefully rare case that some bug sneaks past into production
we can create a `hotfix` branch named `hotfix/<something>`. This is a
branch off of `main` and is the only time we are allowed to do
this. We must fix the bug making as few changes as possible, and then
merge the `hotfix` branch into `main` and `develop`.

## Further reading

Check out the original Git Flow article it goes into a lot more depth:

https://nvie.com/posts/a-successful-git-branching-model/

![GitFlow Model](https://nvie.com/img/git-model@2x.png)
