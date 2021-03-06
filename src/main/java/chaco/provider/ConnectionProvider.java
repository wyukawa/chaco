package chaco.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.extern.slf4j.Slf4j;
import chaco.config.Config;

/**
 * This is a JDBC connection provider.
 */
@Slf4j
public class ConnectionProvider implements Provider<Connection> {

	@Inject
	private Config config;

	private Connection connection;

	@Override
	public Connection get() {
		try {
			if (connection == null) {
				log.debug("Creating new JDBC connection: {}", config.getJdbc().getUrl());

				try {
					Class.forName(config.getJdbc().getDriver());
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}

				connection = DriverManager.getConnection(
					config.getJdbc().getUrl(),
					config.getJdbc().getUsername(),
					config.getJdbc().getPassword());
			}

			return connection;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
