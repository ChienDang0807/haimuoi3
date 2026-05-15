package vn.chiendt.haimuoi3.common.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * When Railway (or Heroku) sets {@code DATABASE_URL}, build the primary DataSource from it.
 */
@Configuration
@Profile("prod")
@ConditionalOnProperty(name = "DATABASE_URL")
public class RailwayDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource railwayDataSource(@Value("${DATABASE_URL}") String databaseUrl) {
        JdbcDatabaseUrlParser.Parsed parsed = JdbcDatabaseUrlParser.parsePostgres(databaseUrl);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(parsed.jdbcUrl());
        if (parsed.username() != null) {
            dataSource.setUsername(parsed.username());
        }
        if (parsed.password() != null) {
            dataSource.setPassword(parsed.password());
        }
        return dataSource;
    }
}
