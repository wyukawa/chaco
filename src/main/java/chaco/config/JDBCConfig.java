package chaco.config;

import lombok.Data;

@Data
public class JDBCConfig {
	private String driver;
	private String url;
	private String catalog;
	private String username;
	private String password;
}
