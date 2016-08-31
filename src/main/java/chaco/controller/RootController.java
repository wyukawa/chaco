package chaco.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import chaco.QueryResult;
import chaco.config.Config;
import chaco.util.QueryUtil;
import chaco.util.PathUtil;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.Param;
import me.geso.webscrew.response.WebResponse;

import chaco.service.JdbcService;
import org.apache.openjpa.lib.jdbc.SQLFormatter;

@Slf4j
public class RootController extends BaseController {

    @Inject
    private Config config;

    @Inject
    private JdbcService jdbcService;

    @GET("/")
    public WebResponse index() throws IOException, TemplateException {
        return this.freemarker("index.html.ftl")
                .render();
    }

    @GET("/schemaNames")
    public WebResponse getSchemaNames() {
        return this.renderJSON(ImmutableMap.builder().put("schemaNames", jdbcService.getSchemaNames()).build());
    }

    @GET("/tableNames")
    public WebResponse getTableNames(@Param("schema") Optional<String> schemaOptinal) {

        if (!schemaOptinal.isPresent()) {
            return this.renderJSON(ImmutableMap.builder().put("error", "schema parameter is required").build());
        }

        return this.renderJSON(ImmutableMap.builder().put("tableNames", jdbcService.getTableNames(config.getJdbc().getCatalog(), schemaOptinal.get())).build());

    }

    @GET("/columnNames")
    public WebResponse getTableNames(@Param("schema") Optional<String> schemaOptinal, @Param("table") Optional<String> tableOptinal) {

        if (schemaOptinal.isPresent() && tableOptinal.isPresent()) {
            return this.renderJSON(ImmutableMap.builder().put("columnNames", jdbcService.getColumnNames(config.getJdbc().getCatalog(), schemaOptinal.get(), tableOptinal.get())).build());
        } else {
            return this.renderJSON(ImmutableMap.builder().put("error", "schema/table parameter is required").build());
        }

    }

    @POST("/query")
    public WebResponse query(@Param("query") Optional<String> queryOptional) {

        try {
            String query = queryOptional.orElse("");
            QueryResult queryResult = jdbcService.getQueryResult(query, true);
            Optional<String> warningMessageOptinal = Optional.ofNullable(queryResult.getWarningMessage());
            if(warningMessageOptinal.isPresent()) {
                return this.renderJSON(ImmutableMap.builder().put("columnNames", queryResult.getColumnNames()).put("rows", queryResult.getRows()).put("queryid", queryResult.getQueryId()).put("warn", warningMessageOptinal.get()).build());
            } else {
                return this.renderJSON(ImmutableMap.builder().put("columnNames", queryResult.getColumnNames()).put("rows", queryResult.getRows()).put("queryid", queryResult.getQueryId()).build());
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return this.renderJSON(ImmutableMap.builder().put("error", e.getMessage()).build());
        }

    }

    @GET("/donequery")
    public WebResponse getDoneQuery() {

        try {
            if(config.getJdbc().getDriver().equals("org.netezza.Driver")) {
                String query = "SELECT * FROM _v_qryhist  WHERE QH_DATABASE='" + config.getJdbc().getCatalog() + "' ORDER BY QH_TSUBMIT DESC LIMIT 100";
                QueryResult queryResult = jdbcService.getQueryResult(query, false);
                return this.renderJSON(ImmutableMap.builder().put("columnNames", queryResult.getColumnNames()).put("rows", queryResult.getRows()).build());
            } else {
                return this.renderJSON(ImmutableMap.builder().put("columnNames", ImmutableList.builder().build()).put("rows", ImmutableList.builder().build()).build());
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return this.renderJSON(ImmutableMap.builder().put("error", e.getMessage()).build());
        }

    }

    @GET("/runningquery")
    public WebResponse getRunningQuery() {

        try {
            if(config.getJdbc().getDriver().equals("org.netezza.Driver")) {
                String query = "SELECT * FROM _v_qrystat ORDER BY QS_TSUBMIT DESC";
                QueryResult queryResult = jdbcService.getQueryResult(query, false);
                return this.renderJSON(ImmutableMap.builder().put("columnNames", queryResult.getColumnNames()).put("rows", queryResult.getRows()).build());
            } else {
                return this.renderJSON(ImmutableMap.builder().put("columnNames", ImmutableList.builder().build()).put("rows", ImmutableList.builder().build()).build());
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return this.renderJSON(ImmutableMap.builder().put("error", e.getMessage()).build());
        }

    }

    @POST("/update")
    public WebResponse update(@Param("query") Optional<String> queryOptional) {

        try {
            String query = queryOptional.orElse("");
            String queryId = QueryUtil.store(query);
            int updateCount = jdbcService.update(query);
            return this.renderJSON(ImmutableMap.builder().put("columnNames", ImmutableList.builder().add("update count").build()).put("rows", ImmutableList.builder().add(updateCount).build()).put("queryid", queryId).build());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return this.renderJSON(ImmutableMap.builder().put("error", e.getMessage()).build());
        }

    }

    @GET("/history")
    public WebResponse getHistory(@Param("queryid") Optional<String> queryidOptional) {

        if(!queryidOptional.isPresent()) {
            return this.renderJSON(ImmutableMap.builder().build());
        }

        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:data/chaco.db")) {
            String query = "SELECT query_id, fetch_result_time_string, query_string FROM query WHERE query_id=?";
            try(PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, queryidOptional.get());
                try(ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    String queryString = rs.getString("query_string");
                    String warningMessage = null;
                    List<String> columnNames = new ArrayList<>();
                    List<List<String>> rows = new ArrayList<List<String>>();
                    try (BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(queryidOptional.get()), StandardCharsets.UTF_8)) {
                        String line = br.readLine();
                        int lineNumber = 0;
                        int limit = config.getLimit();
                        while (line != null) {
                            if (lineNumber == 0) {
                                String[] columns = line.split("\t");
                                columnNames = Arrays.asList(columns);
                            } else {
                                if (lineNumber <= limit) {
                                    String[] row = line.split("\t");
                                    rows.add(Arrays.asList(row));
                                } else {
                                    warningMessage = String.format("now fetch size is %d. This is more than %d. So, fetch operation stopped.", rows.size(), limit);
                                }
                            }
                            lineNumber++;
                            line = br.readLine();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if(warningMessage == null) {
                        return this.renderJSON(ImmutableMap.builder().put("queryString", queryString).put("columnNames", columnNames).put("rows", rows).build());
                    } else {
                        return this.renderJSON(ImmutableMap.builder().put("queryString", queryString).put("columnNames", columnNames).put("rows", rows).put("warn", warningMessage).build());
                    }

                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @GET("/showviewddl")
    public WebResponse showViewDdl(@Param("schema") Optional<String> schemaOptinal, @Param("table") Optional<String> tableOptinal) {

        if (schemaOptinal.isPresent() && tableOptinal.isPresent()) {
            if(config.getJdbc().getDriver().equals("org.netezza.Driver")) {
                String showViewDdlQuery = "SELECT DEFINITION FROM _v_view WHERE DATABASE='" + config.getJdbc().getCatalog() + "' AND SCHEMA='" + schemaOptinal.get() + "' AND VIEWNAME='" + tableOptinal.get() + "'";
                try {
                    QueryResult queryResult = jdbcService.getQueryResult(showViewDdlQuery, false);
                    if(queryResult.getRows().size() == 1) {
                        String viewDDL = queryResult.getRows().get(0).get(0);
                        Object formattedViewDDL = new SQLFormatter().prettyPrint(viewDDL);
                        return this.renderJSON(ImmutableMap.builder().put("columnNames", queryResult.getColumnNames()).put("rows", ImmutableList.builder().add(formattedViewDDL).build()).build());
                    } else {
                        return this.renderJSON(ImmutableMap.builder().put("columnNames", queryResult.getColumnNames()).put("rows", ImmutableList.builder().build()).build());
                    }
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                    return this.renderJSON(ImmutableMap.builder().put("error", e.getMessage()).build());
                }
            } else {
                return this.renderJSON(ImmutableMap.builder().put("columnNames", ImmutableList.builder().build()).put("rows", ImmutableList.builder().build()).build());
            }
        } else {
            return this.renderJSON(ImmutableMap.builder().put("error", "schema/table parameter is required").build());
        }

    }

}

