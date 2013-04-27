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

package us.exultant.mdm.commands;

import java.util.concurrent.*;
import org.eclipse.jgit.lib.*;
import us.exultant.mdm.*;

public abstract class MdmCommand implements Callable<MdmExitMessage> {
	/** The repository this command is working with */
	final protected Repository repo;

	protected MdmCommand(Repository repo) {
		this.repo = repo;
	}

	public Repository getRepository() {
		return repo;
	}
}
