package vn.chiendt.haimuoi3.common.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcDatabaseUrlParserTest {

    @Test
    void parsePostgres_convertsRailwayStyleUrl() {
        JdbcDatabaseUrlParser.Parsed parsed = JdbcDatabaseUrlParser.parsePostgres(
                "postgresql://user:secret@containers-us-west-123.railway.app:5432/railway");

        assertThat(parsed.jdbcUrl())
                .isEqualTo("jdbc:postgresql://containers-us-west-123.railway.app:5432/railway");
        assertThat(parsed.username()).isEqualTo("user");
        assertThat(parsed.password()).isEqualTo("secret");
    }
}
