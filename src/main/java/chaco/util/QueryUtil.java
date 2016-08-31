package chaco.util;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZonedDateTime;

public class QueryUtil {

    public static String store(String query) {
        final String now = ZonedDateTime.now().toString();
        HashFunction hf = Hashing.md5();
        HashCode hc = hf.newHasher().putString(query + ";" + now, Charsets.UTF_8).hash();
        String queryId = hc.toString();

        String insertQuery = String.format("INSERT INTO query VALUES('%s', '%s', '%s')", queryId, now, query.replace("'", "''"));

        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:data/chaco.db")) {
            try(PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                final int updateCount = statement.executeUpdate();
                return queryId;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
