package chaco.cli;

import java.sql.SQLException;

import org.junit.Test;

import chaco.TestBase;

public class SampleCLITest extends TestBase {
	@Test
	public void test() throws SQLException {
		SampleCLI.main(new String[]{});
	}

}
