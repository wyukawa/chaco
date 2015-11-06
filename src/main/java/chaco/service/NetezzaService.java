package chaco.service;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a chaco service class.
 */
public class NetezzaService {
    private final Connection connection;

    @Inject
    public NetezzaService(Connection connection) {
        this.connection = connection;
    }

    public List<List<String>> getResultRows(String query) throws SQLException{
        ImmutableList.Builder<List<String>> resultRows = ImmutableList.builder();
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
        return resultRows.build();
    }

    public List<String> getTableNames(String catalog, String schemaPattern) {
        ImmutableList.Builder<String> tableNamestableNames = ImmutableList.builder();
        try (ResultSet resultSet = this.connection.getMetaData().getTables(catalog, schemaPattern, "%", null)) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                tableNamestableNames.add(tableName);
            }
            return tableNamestableNames.build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getSchemaNames() {
        ImmutableList.Builder<String> schemaNames = ImmutableList.builder();
        try (ResultSet resultSet = this.connection.getMetaData().getSchemas()) {
            while (resultSet.next()) {
                String schemaName = resultSet.getString("TABLE_SCHEM");
                schemaNames.add(schemaName);
            }
            return schemaNames.build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getCatalogNames() {
        ImmutableList.Builder<String> catalogNames = ImmutableList.builder();
        try (ResultSet resultSet = this.connection.getMetaData().getCatalogs()) {
            while (resultSet.next()) {
                String catalogName = resultSet.getString("TABLE_CAT");
                catalogNames.add(catalogName);
            }
            return catalogNames.build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
