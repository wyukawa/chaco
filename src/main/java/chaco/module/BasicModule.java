package chaco.module;

import com.google.inject.AbstractModule;

import chaco.config.Config;
import chaco.provider.ConfigProvider;

public class BasicModule extends AbstractModule {
	private final Config config;

	public BasicModule() {
		this.config = null;
	}

	public BasicModule(final Config config) {
		this.config = config;
	}

	@Override
	protected void configure() {
		if (config != null) {
			bind(Config.class)
				.toInstance(config);
		} else {
			bind(Config.class)
				.toProvider(ConfigProvider.class)
				.asEagerSingleton();
		}
	}
}
