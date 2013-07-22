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

package us.exultant.mdm.util;

import java.io.*;
import org.apache.commons.io.*;
import org.apache.commons.io.filefilter.*;

/**
 * Differs from {@link WildcardFileFilter} in that it matches the wildcard against
 * the whole path rather than just the terminal name.
 */
public class WildcardPathFilter implements IOFileFilter {
	public WildcardPathFilter(String wildcard) {
		this.wildcard = wildcard;
	}

	private final String wildcard;

	public boolean accept(File arg0) {
		return FilenameUtils.wildcardMatch(arg0.getPath(), wildcard);
	}

	public boolean accept(File arg0, String arg1) {
		return accept(new File(arg0, arg1));
	}
}