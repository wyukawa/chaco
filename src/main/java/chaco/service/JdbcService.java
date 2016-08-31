package chaco.service;

import chaco.QueryResult;
import chaco.config.Config;
import chaco.util.PathUtil;
import chaco.util.QueryUtil;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.ZonedDateTime;
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

    public QueryResult getQueryResult(String query, boolean storeFlag) throws SQLException{
        String queryId = QueryUtil.store(query);
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
                int limit = config.getLimit();
                if(storeFlag == true) {
                    Path dst = PathUtil.getResultFilePath(queryId);
                    try (BufferedWriter bw = Files.newBufferedWriter(dst, StandardCharsets.UTF_8)) {
                        bw.write(String.join("\t", columnNames.build()));
                        bw.write("\n");
                        while (resultSet.next()) {
                            List<String> columns = new ArrayList<>();
                            for (int i = 1; i <= columnCount; i++) {
                                String column = resultSet.getString(i);
                                columns.add(column);
                            }
                            if(rowSize < limit) {
                                rows.add(columns);
                                rowSize++;
                                bw.write(String.join("\t", columns));
                                bw.write("\n");
                            } else {
                                QueryResult queryResult = new QueryResult(queryId, rows.build(), columnNames.build());
                                queryResult.setWarningMessage(String.format("row size is more than %d. So, fetch operation stopped.", limit));
                                return queryResult;
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    while (resultSet.next()) {
                        List<String> columns = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String column = resultSet.getString(i);
                            columns.add(column);
                        }
                        if(rowSize < limit) {
                            rows.add(columns);
                            rowSize++;
                        } else {
                            QueryResult queryResult = new QueryResult(queryId, rows.build(), columnNames.build());
                            queryResult.setWarningMessage(String.format("row size is more than %d. So, fetch operation stopped.", limit));
                            return queryResult;
                        }
                    }
                }

            }
        }
        return new QueryResult(queryId, rows.build(), columnNames.build());
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
