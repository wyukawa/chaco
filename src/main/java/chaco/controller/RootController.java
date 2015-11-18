package chaco.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import javax.inject.Inject;

import chaco.QueryResult;
import chaco.config.Config;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.Param;
import me.geso.webscrew.response.WebResponse;

import chaco.service.JdbcService;

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
    public WebResponse query(@Param("query") Optional<String> queryOptional) throws IOException, TemplateException {

        try {
            QueryResult queryResult = jdbcService.getQueryResult(queryOptional.orElse(""));
            return this.renderJSON(ImmutableMap.builder().put("columnNames", queryResult.getColumnNames()).put("rows", queryResult.getRows()).build());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return this.renderJSON(ImmutableMap.builder().put("error", e.getMessage()).build());
        }

    }

    @POST("/queryexecutions")
    public WebResponse queryexecutions() {

        try {
            String query = null;
            if(config.getJdbc().getDriver().equals("org.netezza.Driver")) {
                query = "SELECT * FROM _v_qryhist  WHERE QH_DATABASE='" + config.getJdbc().getCatalog() + "' ORDER BY QH_TSUBMIT DESC LIMIT 100";
            } else {
                query = "SELECT * FROM test_schema.test_table";
            }
            QueryResult queryResult = jdbcService.getQueryResult(query);
            return this.renderJSON(ImmutableMap.builder().put("columnNames", queryResult.getColumnNames()).put("rows", queryResult.getRows()).build());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return this.renderJSON(ImmutableMap.builder().put("error", e.getMessage()).build());
        }

    }

}

