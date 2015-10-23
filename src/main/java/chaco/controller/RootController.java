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
                .param("rows", new ArrayList<>())
                .render();
    }

    @POST("/query")
    public WebResponse select(@Param("sql") Optional<String> sql) throws IOException, TemplateException, SQLException {
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(sql.orElse(""))) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    List<List<String>> rows = new ArrayList<>();
                    int columnCount = stmt.getMetaData().getColumnCount();
                    while (resultSet.next()) {
                        List<String> columns = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String column = resultSet.getString(i);
                            columns.add(column);
                        }
                        rows.add(columns);
                    }

                    return this.freemarker("index.html.ftl")
                            .param("rows", rows)
                            .render();
                }
            }
        }
    }
}

