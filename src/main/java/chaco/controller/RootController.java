package chaco.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.Param;
import me.geso.tinyorm.TinyORM;
import me.geso.webscrew.response.WebResponse;

import chaco.service.NetezzaService;

@Slf4j
public class RootController extends BaseController {
    @Inject
    private TinyORM db;
    @Inject
    private NetezzaService netezzaService;

    @GET("/")
    public WebResponse index() throws IOException, TemplateException, SQLException {
        List<String> tableNames = netezzaService.getTableNames("test", "test_schema");
        return this.freemarker("index.html.ftl")
                .param("query", "")
                .param("rows", new ArrayList<>())
                .param("tableNames", tableNames)
                .param("error", "")
                .render();
    }

    @GET("/catalogNames")
    public WebResponse getCatalogNames() {
        HashMap<String, Object> retVal = new HashMap<String, Object>();
        List<String> catalogNames = null;
        try {
            catalogNames = netezzaService.getCatalogNames();
            retVal.put("catalogs", catalogNames);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }
        return this.renderJSON(retVal);
    }

    @GET("/schemaNames")
    public WebResponse getSchemaNames() {
        HashMap<String, Object> retVal = new HashMap<String, Object>();
        List<String> schemaNames = null;
        try {
            schemaNames = netezzaService.getSchemaNames();
            retVal.put("schemaNames", schemaNames);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }
        return this.renderJSON(retVal);
    }

    @GET("/tableNames")
    public WebResponse getSchemaNames(@Param("catalog") Optional<String> catalogOptional, @Param("schema") Optional<String> schemaOptinal) {
        HashMap<String, Object> retVal = new HashMap<String, Object>();
        List<String> tableNames = null;
        try {
            tableNames = netezzaService.getTableNames(catalogOptional.orElse(""), schemaOptinal.orElse(""));
            retVal.put("tableNames", tableNames);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }
        return this.renderJSON(retVal);
    }

    @POST("/query")
    public WebResponse select(@Param("query") Optional<String> queryOptional) throws IOException, TemplateException {
        List<String> tableNames = null;
        try {
            tableNames = netezzaService.getTableNames("test", "test_schema");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (queryOptional.isPresent()) {
            List<List<String>> rows = new ArrayList<>();
            String error = "";
            try {
                rows = netezzaService.getResultRows(queryOptional.get());
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                error = e.getMessage();
            }
            return this.freemarker("index.html.ftl")
                    .param("query", queryOptional.orElse(""))
                    .param("rows", rows)
                    .param("tableNames", tableNames)
                    .param("error", error)
                    .render();
        } else {
            return this.freemarker("index.html.ftl")
                    .param("query", "")
                    .param("rows", new ArrayList<>())
                    .param("tableNames", tableNames)
                    .param("error", "")
                    .render();
        }

    }


}

