package chaco.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.Param;
//import chaco.service.NetezzaService;
import chaco.service.MySQLService;
import me.geso.tinyorm.TinyORM;
import me.geso.webscrew.response.WebResponse;

@Slf4j
public class RootController extends BaseController {
    @Inject
    private TinyORM db;
    @Inject
    private MySQLService mySQLService;
    //private NetezzaService netezzaService;

    @GET("/")
    public WebResponse index() throws IOException, TemplateException, SQLException {
        return this.freemarker("index.html.ftl")
                .param("query", "")
                .param("rows", new ArrayList<>())
                .param("error", "")
                .render();
    }

    @POST("/query")
    public WebResponse select(@Param("query") Optional<String> queryOptional) throws IOException, TemplateException {

        if (queryOptional.isPresent()) {
            List<List<String>> rows = new ArrayList<>();
            String error = "";
            try {
                rows = mySQLService.getResultRows(queryOptional.get());
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                error = e.getMessage();
            }
            return this.freemarker("index.html.ftl")
                    .param("query", queryOptional.orElse(""))
                    .param("rows", rows)
                    .param("error", error)
                    .render();
        } else {
            return this.freemarker("index.html.ftl")
                    .param("query", "")
                    .param("rows", new ArrayList<>())
                    .param("error", "")
                    .render();
        }

    }


}

