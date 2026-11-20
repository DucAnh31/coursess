package dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Lớp cha dùng chung cho các DAO; mỗi đối tượng quản lý một kết nối. */
public class DAO implements AutoCloseable {
    protected final Connection con;

    public DAO() throws SQLException {
        Properties config = loadConfiguration();
        con = DriverManager.getConnection(
                required(config, "DB_URL"),
                required(config, "DB_USERNAME"),
                setting(config, "DB_PASSWORD", ""));
    }

    private static Properties loadConfiguration() throws SQLException {
        Properties config = new Properties();
        Path envFile = Path.of(".env");
        if (Files.exists(envFile)) {
            try {
                int lineNumber = 0;
                for (String line : Files.readAllLines(envFile)) {
                    lineNumber++;
                    line = line.strip();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int separator = line.indexOf('=');
                    if (separator <= 0) {
                        throw new SQLException("Dòng " + lineNumber + " trong .env không đúng dạng KEY=VALUE.");
                    }
                    String key = line.substring(0, separator).strip();
                    String value = line.substring(separator + 1).strip();
                    if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\""))
                            || (value.startsWith("'") && value.endsWith("'")))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    config.setProperty(key, value);
                }
            } catch (IOException e) {
                throw new SQLException("Không đọc được file .env.", e);
            }
        }
        return config;
    }

    private static String setting(Properties config, String key, String fallback) {
        String value = System.getenv(key);
        return value != null ? value : config.getProperty(key, fallback);
    }

    private static String required(Properties config, String key) throws SQLException {
        String value = setting(config, key, "");
        if (value.isBlank()) {
            throw new SQLException("Thiếu " + key + ". Sao chép .env.example thành .env và cấu hình MySQL.");
        }
        return value;
    }

    public Connection getConnection() {
        return con;
    }

    @Override
    public void close() throws SQLException {
        con.close();
    }
}
