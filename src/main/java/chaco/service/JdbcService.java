package chaco.service;

import chaco.QueryResult;
import chaco.config.Config;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a chaco service class.
 */
public class JdbcService {
    private final Connection connection;

    @Inject
    private Config config;

    @Inject
    public JdbcService(Connection connection) {
        this.connection = connection;
    }

    public QueryResult getQueryResult(String query) throws SQLException{
        ImmutableList.Builder<List<String>> rows = ImmutableList.builder();
        ImmutableList.Builder<String> columnNames = ImmutableList.builder();
        try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                ResultSetMetaData metaData = stmt.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    columnNames.add(columnName);
                }
                int rowSize = 0;
                while (resultSet.next()) {
                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String column = resultSet.getString(i);
                        columns.add(column);
                    }
                    rows.add(columns);
                    rowSize++;

                    if(rowSize >= config.getLimit()) {
                        QueryResult queryResult = new QueryResult(rows.build(), columnNames.build());
                        queryResult.setWarningMessage(String.format("row size is more than %d. So, fetch operation stopped.", config.getLimit()));
                        return queryResult;
                    }
                }
            }
        }
        return new QueryResult(rows.build(), columnNames.build());
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

    public List<String> getColumnNames(String catalog, String schemaPattern, String tableNamePattern) {
        ImmutableList.Builder<String> columnNames = ImmutableList.builder();
        try (ResultSet resultSet = this.connection.getMetaData().getColumns(catalog, schemaPattern, tableNamePattern, "%")) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                columnNames.add(columnName);
            }
            return columnNames.build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int update(String query) throws SQLException {
        try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
            return stmt.executeUpdate();
        }
    }
}
