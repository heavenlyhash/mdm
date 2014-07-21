/*
 * Copyright 2012 - 2014 Eric Myhre <http://exultant.us>
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

package net.polydawn.mdm.jgit;

import static org.junit.Assert.*;
import java.io.*;
import net.polydawn.mdm.fixture.*;
import net.polydawn.mdm.test.*;
import org.junit.*;
import org.junit.runner.*;
import us.exultant.ahs.iob.*;

@RunWith(OrderedJUnit4ClassRunner.class)
public class SubrepoWalkTest extends TestCaseUsingRepository {
	@Test
	public void should_see_a_dir_with_dotgit_child_as_subrepo() throws Exception {
		// setup
		Fixture project = new ProjectAlpha("projectAlpha");
		WithCwd wd = new WithCwd(project.getRepo().getWorkTree()); {
			new File("submodule").getCanonicalFile().mkdir();
			IOForge.saveFile("faking it out", new File("./submodule/.git").getCanonicalFile());
		} wd.close();

		// test we find it
		SubrepoWalk generator = new SubrepoWalk(project.getRepo());
		assertEquals("should find the so-called submodule path", "submodule", generator.next());
		assertEquals("should find nothing else", null, generator.next());
	}
}
