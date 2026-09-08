package com.tiffin.system.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Slf4j
@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Value("${SPRING_DATASOURCE_URL:#{null}}")
    private String springDatasourceUrl;

    @Value("${spring.datasource.username:sa}")
    private String defaultUsername;

    @Value("${spring.datasource.password:}")
    private String defaultPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String effectiveUrl = springDatasourceUrl != null ? springDatasourceUrl : databaseUrl;

        if (effectiveUrl != null && (effectiveUrl.startsWith("postgres://") || effectiveUrl.startsWith("postgresql://"))) {
            try {
                URI uri = new URI(effectiveUrl);
                String userInfo = uri.getUserInfo();
                String username = defaultUsername;
                String password = defaultPassword;

                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }

                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                if (uri.getQuery() != null) {
                    jdbcUrl += "?" + uri.getQuery();
                }

                log.info("Configured PostgreSQL DataSource from DATABASE_URL: jdbc:postgresql://{}:{}{}", host, port, path);

                HikariDataSource dataSource = new HikariDataSource();
                dataSource.setJdbcUrl(jdbcUrl);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
                dataSource.setDriverClassName("org.postgresql.Driver");
                dataSource.setMaximumPoolSize(10);
                dataSource.setMinimumIdle(2);
                return dataSource;
            } catch (Exception e) {
                log.warn("Failed to parse DATABASE_URL as URI, fallback to standard datasource: {}", e.getMessage());
            }
        }

        HikariDataSource dataSource = new HikariDataSource();
        String url = effectiveUrl != null ? effectiveUrl : "jdbc:h2:file:./data/tiffindb;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE";
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(defaultUsername);
        dataSource.setPassword(defaultPassword);
        if (url.contains("jdbc:h2:")) {
            dataSource.setDriverClassName("org.h2.Driver");
        } else if (url.contains("jdbc:postgresql:")) {
            dataSource.setDriverClassName("org.postgresql.Driver");
        } else if (url.contains("jdbc:mysql:")) {
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        return dataSource;
    }
}