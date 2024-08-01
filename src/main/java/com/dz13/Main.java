package com.dz13;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DB_NAME = "dz13";
    private static final String USER = "myuser";
    private static final String PASSWORD = "mypassword";

    public static void main(String[] args) throws Exception {
        // Run the postgresql container
        // docker run --name my_postgres_container -e POSTGRES_USER=myuser -e POSTGRES_PASSWORD=mypassword
        // -e POSTGRES_DB=mydatabase -p 5432:5432 -d postgres

        if (!databaseExists()) {
            createDatabase();
        }
        if (!tableExists("users")) {
            createUsersTable();
        }

        createUser("Bob Smith", 30);
        readUser(1);
        updateUser(1, "John Smith", 31);
        deleteUser(1);
    }

    private static Connection getConnection() throws Exception {
        return DriverManager.getConnection(DB_URL + DB_NAME, USER, PASSWORD);
    }

    private static boolean databaseExists() {
        try (Connection conn = getConnection()) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void createDatabase() throws Exception {
        String sql = "CREATE DATABASE " + DB_NAME;
        Connection conn = DriverManager.getConnection(DB_URL + "postgres", USER, PASSWORD);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
    }

    private static boolean tableExists(String tableName) throws Exception {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables" +
                " WHERE table_schema = 'public' AND table_name = '" + tableName + "')";
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            return rs.getBoolean(1);
        }

        return false;
    }

    private static void createUsersTable() throws Exception {
        String sql = "CREATE TABLE users (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "age INT NOT NULL" +
                ");";
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        System.out.println("users table was created.");
    }

    private static void createUser(String name, int age) throws Exception {
        String sql = "INSERT INTO users (name, age) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.executeUpdate();
            System.out.println("User created successfully.");
        }
    }

    private static void readUser(int id) throws Exception {
        String sql = "SELECT id, name, age FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Age: " + rs.getInt("age"));
            }
        }
    }

    private static void updateUser(int id, String name, int age) throws Exception {
        String sql = "UPDATE users SET name = ?, age = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
            System.out.println("User updated successfully.");
        }
    }

    private static void deleteUser(int id) throws Exception {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("User deleted successfully.");
        }
    }
}
