/*
 * Copyright 2012, 2013 Eric Myhre <http://exultant.us>
 *
 * This file is part of mdm <https://github.com/heavenlyhash/mdm/>.
 *
 * mdm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package us.exultant.mdm;

import java.io.*;
import java.util.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.errors.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.submodule.*;
import org.eclipse.jgit.treewalk.filter.*;

public class MdmModule {
	public MdmModule(Repository parent, SubmoduleWalk generator, Config gitmodulesCfg) throws IOException, IsntOne {
		this(parent, generator.getPath(), gitmodulesCfg, generator.getObjectId());
	}

	public MdmModule(Repository parent, String handle, Config gitmodulesCfg) throws IOException, IsntOne {
		this(parent, handle, gitmodulesCfg, null);
	}

	private MdmModule(Repository parent, String handle, Config gitmodulesCfg, ObjectId objectId) throws IOException, IsntOne {
		this.handle = handle;
		path = gitmodulesCfg.getString(ConfigConstants.CONFIG_SUBMODULE_SECTION, handle, ConfigConstants.CONFIG_KEY_PATH);

		type = MdmModuleType.fromString(gitmodulesCfg.getString(ConfigConstants.CONFIG_SUBMODULE_SECTION, path, MdmConfigConstants.Module.MODULE_TYPE.toString()));
		if (type == null) throw new IsntOne("no recognized type of mdm module listed in gitmodules file.");
		versionName = gitmodulesCfg.getString(ConfigConstants.CONFIG_SUBMODULE_SECTION, path, MdmConfigConstants.Module.DEPENDENCY_VERSION.toString());

		if (objectId != null) {
			indexId = objectId;
		} else {
			SubmoduleWalk generator = SubmoduleWalk.forIndex(parent).setFilter(PathFilter.create(path));
			indexId = generator.next() ? generator.getObjectId() : null;
		}

		repo = SubmoduleWalk.getSubmoduleRepository(parent,  path);

		if (repo == null) {
			headId = null;
			versionActual = null;
			dirtyFiles = false;
		} else {
			headId = repo.resolve(Constants.HEAD);

			String versionActual = null;
			try {
				List<Ref> tags = new Git(repo).tagList().call();
				for (Ref tag : tags) {
					if (tag.getObjectId().equals(headId)) {
						String[] tagChunks = tag.getName().split("/");
						// for all tags, index 0 is 'refs', and 1 is 'tags'.
						if (tagChunks[2].equals("release") && tagChunks.length > 2) {
							versionActual = "";
							for (int i = 3; i < tagChunks.length; i++)
								versionActual += tagChunks[i];
							break;
						}
					}
				}
			} catch (GitAPIException e) {}
			this.versionActual = versionActual;

			boolean dirtyFiles;
			try {
				dirtyFiles = !new Git(repo).status().call().isClean();
			} catch (NoWorkTreeException e) {
				throw new RuntimeException("wat", e);
			} catch (GitAPIException e) {
				dirtyFiles = false;
			}
			this.dirtyFiles = dirtyFiles;
		}

		urlHistoric = gitmodulesCfg.getString(ConfigConstants.CONFIG_SUBMODULE_SECTION, path, ConfigConstants.CONFIG_KEY_URL);
		urlLocal = parent.getConfig().getString(ConfigConstants.CONFIG_SUBMODULE_SECTION, path, ConfigConstants.CONFIG_KEY_URL);


		SubmoduleStatusType statusType;
		if (path == null)
			// jgit report SubmoduleStatusType.MISSING if no path in .gitmodules file, but I don't even want to deal with that.
			throw new IsntOne("no path for module listed in gitmodules file.");
		else if (urlLocal == null)
			// Report uninitialized if no URL in config file
			statusType = SubmoduleStatusType.UNINITIALIZED;
		else if (repo == null)
			// Report uninitialized if no submodule repository
			statusType = SubmoduleStatusType.UNINITIALIZED;
		else if (headId == null)
			// Report uninitialized if no HEAD commit in submodule repository
			statusType = SubmoduleStatusType.UNINITIALIZED;
		else if (!headId.equals(indexId))
			// Report checked out if HEAD commit is different than index commit
			statusType = SubmoduleStatusType.REV_CHECKED_OUT;
		else
			// Report initialized if HEAD commit is the same as the index commit
			statusType = SubmoduleStatusType.INITIALIZED;
		status = new SubmoduleStatus(statusType, path, indexId, headId);
	}

	public static class IsntOne extends Exception {
		public IsntOne() { super(); }
		public IsntOne(String message, Throwable cause) { super(message, cause); }
		public IsntOne(String message) { super(message); }
		public IsntOne(Throwable cause) { super(cause); }
	}


	/**
	 * Module's name in the config. Almost certainly ought to be the same as path for
	 * simplicity. And in fact even the jgit API seems to be a little confused about
	 * the distinction. And in fact even the cgit commands seem a little confused
	 * about the disinction! Still. We'll maintain it here, just in case the ground
	 * ever changes.
	 */
	private final String handle;

	/** 'path' key in config. */
	private final String path;

	/** The submodule repository. */
	Repository repo;

	/** The purpose of this module to mdm as specified in .gitmodules. */
	private final MdmModuleType type;

	/** The version named in the .gitmodules file, if this is a dependeny module. */
	private final String versionName;

	/** A version name parsed from the tags of the currently checked out head of the module. */
	private final String versionActual;

	/** The ID the parent repo says this submodule should be at. */
	private final ObjectId indexId;

	/** The ID the module repo is actually at. */
	private final ObjectId headId;

	/** Remote url as listed in the present gitmodules file. */
	private final String urlHistoric;

	/** Remote url as listed in the local .git/config file. */
	String urlLocal;

	/** Are there uncommitted for changed files in the module? */
	private final boolean dirtyFiles;

	private final SubmoduleStatus status;

	public String getHandle() {
		return this.handle;
	}

	public String getPath() {
		return this.path;
	}

	public SubmoduleStatus getStatus() {
		return status;
	}

	public Repository getRepo() {
		return this.repo;
	}

	public MdmModuleType getType() {
		return this.type;
	}

	public String getVersionName() {
		return this.versionName;
	}

	public String getVersionActual() {
		return this.versionActual;
	}

	public ObjectId getIndexId() {
		return this.indexId;
	}

	public ObjectId getHeadId() {
		return this.headId;
	}

	public String getUrlHistoric() {
		return this.urlHistoric;
	}

	public String getUrlLocal() {
		return this.urlLocal;
	}

	public boolean hasDirtyFiles() {
		return this.dirtyFiles;
	}

	public String toString() {
		return new StringBuilder()
			.append("MdmModule{")
			.append("\n         handle =\t").append(this.handle)
			.append("\n           path =\t").append(this.path)
			.append("\n         status =\t").append(this.status.getType())
			.append("\n           repo =\t").append(this.repo)
			.append("\n           type =\t").append(this.type)
			.append("\n    versionName =\t").append(this.versionName)
			.append("\n  versionActual =\t").append(this.versionActual)
			.append("\n        indexId =\t").append(this.indexId)
			.append("\n         headId =\t").append(this.headId)
			.append("\n    urlHistoric =\t").append(this.urlHistoric)
			.append("\n       urlLocal =\t").append(this.urlLocal)
			.append("\n     dirtyFiles =\t").append(this.dirtyFiles)
			.append("\n}").toString();
	}
}