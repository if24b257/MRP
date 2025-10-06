package org.SalimMRP.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    //Konfigurationsparameter – passen zu deinem docker-compose Setup
    private static final String URL = "jdbc:postgresql://localhost:5433/mrp_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    //Statische Initialisierung – optional, um sicherzugehen, dass der Treiber geladen ist
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found. Include it in your classpath!");
        }
    }

    //Methode für neue DB-Verbindungen
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    //Optional: Methode zum Testen
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Database connection established successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to the database: " + e.getMessage());
        }
    }
}
