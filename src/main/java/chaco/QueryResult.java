package chaco;

import lombok.Data;

import java.util.List;

@Data
public class QueryResult {

    private String queryId;

    private List<List<String>> rows;

    private List<String> columnNames;

    private String warningMessage;

    public QueryResult(String queryId, List<List<String>> rows, List<String> columnNames) {
        this.queryId = queryId;
        this.rows = rows;
        this.columnNames = columnNames;
    }
}
