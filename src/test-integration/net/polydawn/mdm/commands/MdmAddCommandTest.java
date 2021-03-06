package net.polydawn.mdm.commands;

import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import net.polydawn.mdm.*;
import net.polydawn.mdm.fixture.*;
import net.polydawn.mdm.test.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.lib.*;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import us.exultant.ahs.iob.*;

@RunWith(OrderedJUnit4ClassRunner.class)
public class MdmAddCommandTest extends TestCaseUsingRepository {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testAddFromLocalRelrepWithSingleVersion() throws Exception {
		Fixture project = new ProjectAlpha("projectRepo");
		Fixture releases = new ProjectAlphaReleases("projectRepo-releases");

		WithCwd wd = new WithCwd(project.getRepo().getWorkTree()); {
			MdmAddCommand cmd = new MdmAddCommand(project.getRepo());
			cmd.url = releases.getRepo().getWorkTree().toString();
			cmd.name = "depname";
			cmd.pathLibs = new File("lib");
			cmd.version = "v1";
			cmd.validate();
			assertJoy(cmd.call());
		} wd.close();

		File depWorkTreePath = new File(project.getRepo().getWorkTree()+"/lib/depname").getCanonicalFile();
		File depGitDataPath = new File(project.getRepo().getDirectory()+"/modules/lib/depname").getCanonicalFile();

		// i do hope there's a filesystem there now
		assertTrue("dependency module path exists on fs", depWorkTreePath.exists());
		assertTrue("dependency module path is dir", depWorkTreePath.isDirectory());

		// assert on the refs in the dependency module we added to the project repo
		Collection<Ref> refs = new Git(project.getRepo()).lsRemote()
				.setRemote(depGitDataPath.toString())
				.call();
		List<String> refNames = new ArrayList<String>(refs.size());
		for (Ref r : refs) refNames.add(r.getName());
		assertTrue("head ref present in dependency module", refNames.contains("HEAD"));
		assertTrue("release branch present in dependency module", refNames.contains("refs/heads/mdm/release/v1"));
		assertTrue("release tag present in dependency module", refNames.contains("refs/tags/release/v1"));
		assertEquals("exactly these three refs present in dependency module", 3, refNames.size());

		// check the actual desired artifacts are inside the release module location
		assertEquals("exactly two files exist (.git and the artifact)", 2, depWorkTreePath.listFiles().length);
		assertEquals("content of artifact is correct", "alpha release", IOForge.readFileAsString(new File(depWorkTreePath, "alpha")));
	}

	@Test
	public void testAddNonexistingVersionFails() throws Exception {
		Fixture project = new ProjectAlpha("projectRepo");
		Fixture releases = new ProjectAlphaReleases("projectRepo-releases");

		WithCwd wd = new WithCwd(project.getRepo().getWorkTree());
		MdmAddCommand cmd = new MdmAddCommand(project.getRepo());
		cmd.url = releases.getRepo().getWorkTree().toString();
		cmd.name = "depname";
		cmd.pathLibs = new File("lib");
		cmd.version = "notaversion";
		cmd.validate();
		try {
			cmd.call();
			fail("adding should have failed, the requested version does not exist");
		} catch (MdmExitMessage expected) {}
		wd.close();
	}

	@Test
	public void testAddFromNonexistingRepoFails() throws Exception {
		Fixture project = new ProjectAlpha("projectRepo");

		WithCwd wd = new WithCwd(project.getRepo().getWorkTree());
		MdmAddCommand cmd = new MdmAddCommand(project.getRepo());
		cmd.url = "/most/certainly/not/a/release/repo/of/any/kind";
		cmd.name = "depname";
		cmd.pathLibs = new File("lib");
		cmd.version = "v1";
		cmd.validate();
		try {
			cmd.call();
			fail("adding should have failed, the no release repo exists there");
		} catch (MdmExitMessage expected) {}
		wd.close();
	}

	@Test
	public void testAddFromLocalRelrepWithMultipleVersions() throws Exception {
		Fixture project = new ProjectAlpha("projectRepo");
		Fixture releases = new ProjectBetaReleases("projectRepo-releases");

		WithCwd wd = new WithCwd(project.getRepo().getWorkTree()); {
			MdmAddCommand cmd = new MdmAddCommand(project.getRepo());
			cmd.url = releases.getRepo().getWorkTree().toString();
			cmd.name = "depname";
			cmd.pathLibs = new File("lib");
			cmd.version = "v1.1";
			cmd.validate();
			assertJoy(cmd.call());
		} wd.close();

		File depWorkTreePath = new File(project.getRepo().getWorkTree()+"/lib/depname").getCanonicalFile();
		File depGitDataPath = new File(project.getRepo().getDirectory()+"/modules/lib/depname").getCanonicalFile();

		// i do hope there's a filesystem there now
		assertTrue("dependency module path exists on fs", depWorkTreePath.exists());
		assertTrue("dependency module path is dir", depWorkTreePath.isDirectory());

		// assert on the refs in the dependency module we added to the project repo
		Collection<Ref> refs = new Git(project.getRepo()).lsRemote()
				.setRemote(depGitDataPath.toString())
				.call();
		List<String> refNames = new ArrayList<String>(refs.size());
		for (Ref r : refs) refNames.add(r.getName());
		assertTrue("head ref present in dependency module", refNames.contains("HEAD"));
		assertTrue("release branch present in dependency module", refNames.contains("refs/heads/mdm/release/v1.1"));
		assertTrue("release tag present in dependency module", refNames.contains("refs/tags/release/v1.1"));
		assertEquals("exactly these three refs present in dependency module", 3, refNames.size());

		// check the actual desired artifacts are inside the release module location
		assertEquals("exactly two files exist (.git and the artifact)", 2, depWorkTreePath.listFiles().length);
		assertEquals("content of artifact is correct", "beta release 1.1", IOForge.readFileAsString(new File(depWorkTreePath, "beta")));
	}
}
