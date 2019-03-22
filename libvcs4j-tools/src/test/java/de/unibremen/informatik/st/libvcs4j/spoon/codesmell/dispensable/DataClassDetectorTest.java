package de.unibremen.informatik.st.libvcs4j.spoon.codesmell.dispensable;

import de.unibremen.informatik.st.libvcs4j.spoon.codesmell.CodeSmell;
import de.unibremen.informatik.st.libvcs4j.spoon.codesmell.RevisionMock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import spoon.Launcher;
import spoon.reflect.CtModel;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DataClassDetectorTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void tripleDataClass() throws IOException {
		RevisionMock revision = new RevisionMock(folder);
		revision.addFile(Paths.get("dataclass", "Triple.java"));

		Launcher launcher = new Launcher();
		launcher.addInputResource(folder.getRoot().getAbsolutePath());
		CtModel model = launcher.buildModel();

		DataClassDetector dcDetector = new DataClassDetector(revision);
		dcDetector.scan(model);
		List<CodeSmell> codeSmells = dcDetector.getCodeSmells();

		assertThat(codeSmells).hasSize(1);
		CodeSmell codeSmell = codeSmells.get(0);
		assertThat(codeSmell.getRanges()).hasSize(1).first()
				.matches(range -> range.getBegin().getLine() == 1)
				.matches(range -> range.getBegin().getColumn() == 1)
				.matches(range -> range.getEnd().getLine() == 30)
				.matches(range -> range.getEnd().getColumn() == 1);
	}
}
