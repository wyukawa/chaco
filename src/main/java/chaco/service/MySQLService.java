package chaco.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public List<List<String>> getResultRows(String query) throws SQLException {
        List<List<String>> resultRows = new ArrayList<>();
        try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                int columnCount = stmt.getMetaData().getColumnCount();
                while (resultSet.next()) {
                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String column = resultSet.getString(i);
                        columns.add(column);
                    }
                    resultRows.add(columns);
                }
            }
        }
        return resultRows;
    }
}
