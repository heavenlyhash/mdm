
from mdm.imp import *;


def getMdmSubmodules(kind=None, name=None, gmFilename=None):
	if (not gmFilename):
		try: gmFilename = git("rev-parse", "--show-toplevel").strip()+"/.gitmodules";
		except ErrorReturnCode: return None;
	dConf = cgw.getConfig(gmFilename);
	if (dConf is None): return None;
	if (not 'submodule' in dConf): return None;
	dSubm = dConf['submodule'];
	if (name):
		if (not name in dSubm): return None;
		subm = dSubm[name];
		if (not 'mdm' in subm): return None;
		if (kind and not subm['mdm'] == kind): return None;
		return subm;
	else:
		for submName, submDat in dSubm.items():
			if (not 'mdm' in submDat): del dSubm[submName]; continue;
			if (kind and not submDat['mdm'] == kind): del dSubm[submName]; continue;
		return dSubm;



def doDependencyAdd(name, url, version):
	git.submodule("add", join(url, version+".git"), name);				# add us a submodule for great good!
	git.submodule("init", name);							# i would've thought `git submodule add` would have already done this, but it seems sometimes it does not.  anyway, at worst, this is a redunant no-op.
	git.config("-f", ".gitmodules", "submodule."+name+".mdm", "dependency");	# put a marker in the submodules config that this submodule is a dependency managed by mdm.
	# git.config("-f", ".gitmodules", "submodule."+name+".mdm-version", version);	# we could add another marker to make the version name an explicit property, but what would be the point?  our purposes are served well enough by making the pathname have an extremely explicit connection to the version name.
	git.add(".gitmodules");								# have to `git add` the gitmodules file again since otherwise the marker we just appended doesn't get staged
	pass;



def doDependencyRemove(name):
	try: git.config("-f", ".gitmodules", "--remove-section", "submodule."+name);	# remove config lines for this submodule currently in gitmodules file.  also, note i'm assuming we're already at the pwd of the repo top here.
	except: pass;									# errors because there was already no such config lines aren't really errors.
	git.add(".gitmodules");								# stage the gitmodule file change into the index.
	git.rm("--cached", name);							# mark submodule for removal in the index.  have to use the cached option and rm-rf it ourselves or git has a beef, seems silly to me but eh.
	rm("-rf", name);								# clear out the actual files
	rm("-rf", join(".git/modules",name));						# if this is one of the newer version of git (specifically, 1.7.8 or newer) that stores the submodule's data in the parent projects .git dir, clear that out forcefully as well or else git does some very silly things (you end up with the url changed but it recreates the old files and doesn't change the object id like it should).
	try: git.config("-f", ".git/config", "--remove-section", "submodule."+name);	# remove conflig lines for this submodule currently in .git/config.	# environmental $GIT_DIR is not supported.	# i'm a little unhappy about doing this before trying to commit anything else for smooth error recovery reasons... but on the other hand, we want to use this function to compose with other things in the same commit, so.
	except: pass;									# errors because there was already no such config lines aren't really errors.
	pass;



def getVersionManifest(releasesUrl):
	# grab the gitmodules file (which may be either local, or remote over raw http transport!) and store as string
	try:
		with closing(urllib.urlopen(releasesUrl+"/.gitmodules")) as f:
			remoteModulesStr = f.read();
	except:
		return None;
	
	# hand the gitmodules contents through `git-config` (via cgw.getConfig via getMdmSubmodules), get a proper dict of the conf back
	dConf = mdm.plumbing.getMdmSubmodules("release-snapshot", None, (remoteModulesStr,));
	
	# we only really need an array of the names back
	return sorted(filter(lambda pythonYUNoHaveATrueFunctionAlready : True, dConf), key=fn_version_sort);


