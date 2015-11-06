package chaco.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.Param;
import me.geso.webscrew.response.WebResponse;

import chaco.service.NetezzaService;

@Slf4j
public class RootController extends BaseController {

    @Inject
    private NetezzaService netezzaService;

    @GET("/")
    public WebResponse index() throws IOException, TemplateException {
        return this.freemarker("index.html.ftl")
                .param("schemaNames", netezzaService.getSchemaNames())
                .param("query", "")
                .param("rows", ImmutableList.of())
                .param("error", "")
                .render();
    }

    @GET("/catalogNames")
    public WebResponse getCatalogNames() {
        return this.renderJSON(ImmutableMap.builder().put("catalogs", netezzaService.getCatalogNames()).build());
    }

    @GET("/schemaNames")
    public WebResponse getSchemaNames() {
        return this.renderJSON(ImmutableMap.builder().put("schemaNames", netezzaService.getSchemaNames()).build());
    }

    @GET("/tableNames")
    public WebResponse getTableNames(@Param("catalog") Optional<String> catalogOptional, @Param("schema") Optional<String> schemaOptinal) {

        if (!catalogOptional.isPresent() || !schemaOptinal.isPresent()) {
            return this.renderJSON(ImmutableMap.builder().put("error", "catalog and schema parameters are required").build());
        }

        return this.renderJSON(ImmutableMap.builder().put("tableNames", netezzaService.getTableNames(catalogOptional.get(), schemaOptinal.get())).build());

    }

    @POST("/query")
    public WebResponse query(@Param("query") Optional<String> queryOptional) throws IOException, TemplateException {

        try {
            return this.freemarker("index.html.ftl")
                    .param("query", queryOptional.orElse(""))
                    .param("rows", netezzaService.getResultRows(queryOptional.orElse("")))
                    .param("error", "")
                    .render();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return this.freemarker("index.html.ftl")
                    .param("query", queryOptional.orElse(""))
                    .param("rows", ImmutableList.of())
                    .param("error", e.getMessage())
                    .render();
        }

    }

}

