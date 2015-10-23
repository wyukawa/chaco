package chaco.service;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This is a chaco service class.
 */
public class NetezzaService {
	private final Connection connection;

	@Inject
	public NetezzaService(Connection connection) {
		this.connection = connection;
	}

	public int getJDBCMajorVersion() throws SQLException {
		return this.connection.getMetaData().getJDBCMajorVersion();
	}
}
