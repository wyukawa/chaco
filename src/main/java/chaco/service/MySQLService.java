package chaco.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;

/**
 * This is a chaco service class.
 */
public class MySQLService {
	private final Connection connection;

	@Inject
	public MySQLService(Connection connection) {
		this.connection = connection;
	}

	public int getJDBCMajorVersion() throws SQLException {
		return this.connection.getMetaData().getJDBCMajorVersion();
	}
}
