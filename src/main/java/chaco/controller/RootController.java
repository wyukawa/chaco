package chaco.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import javax.inject.Inject;

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
                .param("schemaNames", jdbcService.getSchemaNames())
                .param("query", "")
                .param("rows", ImmutableList.of())
                .param("error", "")
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

    @POST("/query")
    public WebResponse query(@Param("query") Optional<String> queryOptional) throws IOException, TemplateException {

        try {
            return this.freemarker("index.html.ftl")
                    .param("schemaNames", jdbcService.getSchemaNames())
                    .param("query", queryOptional.orElse(""))
                    .param("rows", jdbcService.getResultRows(queryOptional.orElse("")))
                    .param("error", "")
                    .render();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return this.freemarker("index.html.ftl")
                    .param("schemaNames", jdbcService.getSchemaNames())
                    .param("query", queryOptional.orElse(""))
                    .param("rows", ImmutableList.of())
                    .param("error", e.getMessage())
                    .render();
        }

    }

}

