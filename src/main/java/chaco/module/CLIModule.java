package chaco.module;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import chaco.provider.ConnectionProvider;
import chaco.provider.TinyORMProvider;
import me.geso.tinyorm.TinyORM;

public class CLIModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(Connection.class)
			.toProvider(ConnectionProvider.class)
			.in(Scopes.SINGLETON);
		bind(TinyORM.class)
			.toProvider(TinyORMProvider.class);
	}
}
