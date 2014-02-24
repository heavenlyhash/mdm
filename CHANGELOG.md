CHANGELOG
=========

v2.14.0
-------

- Perceive the version of a dependency module from branch name.  Previously this was parsed from tags on release commits; these tags are now ignored.
- Fix problems operating on repositories with a releases repo link but missing a fully constructed releases repo.
- Various bugfixes; additional testing; see git log for details.

- Note: this will be the last version of mdm that produces tags on release commits!



v2.13.0
-------

- Executable permission bits are now preserved and committed during `mdm release`.
- Empty directories are no longer carried along when copying files into a releases repo during `mdm release`.  This leaves the release repo in a state more consistent with fresh clones.
- Hidden files and directories are now carried along when `mdm release` is used on a directory.
- Releases of mdm are now packed with ProGuard, resulting in smaller binaries.
- `mdm release` command can now be used without being located in a repository root.
- `mdm release` command now rejects repositories with any uncommitted changes.
- Fix username not being picked up from the system for use in commit messages.
- Various bugfixes; additional testing; see git log for details.



v2.12.0
-------

- Releases of mdm now include two files: 'mdm.jar', and a shell script called 'mdm'.  (Previously, only the jar file was included in the release, and it was named 'mdm'.  This worked well on systems where the default binfmt recognized jars, but was irritating on other systems, and thus we now include a wrapper shell script in the release.)
- Fix bug in handling submodules that aren't managed by mdm.



v2.11.0
-------

- Upgrade jgit dependency from an unofficial build to version '3.0.0.201306101825-r'.
- `mdm release-init` now accepts existing empty directories as clean target locations for creating a releases repo.
- Fix bug when updating project repo's link to release repo if the path is non-default.
- Unexpected errors are now logged to a file instead of vomiting stack traces at the end user.
- Various bugfixes; significant refactoring to module handling; additional testing; see git log for details.



v2.10.0
-------

- Complete rewrite of mdm.
  - mdm is now implemented in java, and internally uses jgit.
  - mdm no longer performs any exec'ing of a system install of git, which dramatically increases its reliability, and also means it can be distributed statically without any further dependencies aside from a jvm (version 1.6 is sufficient).
  - mdm is now cross platform.

Note that despite being a complete rewrite of mdm, the major version number is *not* incremented.
The mdm release repository layout is *completely unchanged*.
The python implementation of mdm may continue to be used; in fact the java and python implementations may be used interchangeably, side by side.



v2.1.2
------

- Final released version of the python implementation of mdm.
- The fog of history grows thick here: consult the git log for detailed information about this and earlier versions.

