package vn.chiendt.haimuoi3.common.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Parses {@code postgresql://} / {@code postgres://} URLs (Railway, Heroku) into JDBC parts.
 */
public final class JdbcDatabaseUrlParser {

    private JdbcDatabaseUrlParser() {
    }

    public record Parsed(String jdbcUrl, String username, String password) {
    }

    public static Parsed parsePostgres(String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalArgumentException("DATABASE_URL must not be blank");
        }
        String normalized = databaseUrl.trim();
        if (normalized.startsWith("jdbc:")) {
            return new Parsed(normalized, null, null);
        }
        URI uri = URI.create(normalized.replace("postgres://", "postgresql://"));
        String userInfo = uri.getUserInfo();
        String username = null;
        String password = null;
        if (userInfo != null && !userInfo.isBlank()) {
            String[] parts = userInfo.split(":", 2);
            username = decode(parts[0]);
            if (parts.length > 1) {
                password = decode(parts[1]);
            }
        }
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getPath() == null || uri.getPath().isBlank() ? "/railway" : uri.getPath();
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + path;
        if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
            jdbcUrl += "?" + uri.getQuery();
        }
        return new Parsed(jdbcUrl, username, password);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
