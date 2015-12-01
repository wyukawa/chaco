package chaco.config;

import lombok.Data;

@Data
public class Config {
	private boolean development;
	private JDBCConfig jdbc;
	private DataSourceConfig dataSource;
	private int limit;
}
