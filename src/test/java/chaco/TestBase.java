package chaco;

import org.junit.BeforeClass;

import com.google.inject.Guice;
import com.google.inject.Injector;

import chaco.config.Config;
import chaco.module.BasicModule;
import chaco.module.CLIModule;
import chaco.provider.ConfigProvider;

public class TestBase {
	protected static Config config;

	@BeforeClass
	public static void setupClass() {
		String env = System.getProperty("chaco.env");
		if (env == null) {
			System.setProperty("chaco.env", "test");
		}
		env = System.getProperty("chaco.env");
		if (!(env.equals("test"))) {
			throw new RuntimeException("Do not run test case on non-test environment");
		}

		config = new ConfigProvider().get();
	}

	protected Injector getInjector() {
		return Guice.createInjector(
			new BasicModule(config),
			new CLIModule());
	}
}
