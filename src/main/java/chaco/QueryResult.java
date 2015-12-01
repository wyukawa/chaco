package chaco;

import lombok.Data;

import java.util.List;

@Data
public class QueryResult {

    private List<List<String>> rows;

    private List<String> columnNames;

    private String warningMessage;

    public QueryResult(List<List<String>> rows, List<String> columnNames) {
        this.rows = rows;
        this.columnNames = columnNames;
    }
}
