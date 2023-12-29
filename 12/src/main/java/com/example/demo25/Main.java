package com.example.demo25;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Game {
    private int rank;
    private String name;
    private String platform;
    private int year;
    private String genre;
    private String publisher;
    private double naSales;
    private double euSales;
    private double jpSales;
    private double otherSales;
    private double globalSales;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public double getNaSales() {
        return naSales;
    }

    public void setNaSales(double naSales) {
        this.naSales = naSales;
    }

    public double getEuSales() {
        return euSales;
    }

    public void setEuSales(double euSales) {
        this.euSales = euSales;
    }

    public double getJpSales() {
        return jpSales;
    }

    public void setJpSales(double jpSales) {
        this.jpSales = jpSales;
    }

    public double getOtherSales() {
        return otherSales;
    }

    public void setOtherSales(double otherSales) {
        this.otherSales = otherSales;
    }

    public double getGlobalSales() {
        return globalSales;
    }

    public void setGlobalSales(double globalSales) {
        this.globalSales = globalSales;
    }
}

public class Main {
    public static void main(String[] args) {
        List<Game> gameList = parseCSV("C:\\Users\\Пользователь\\Desktop\\12\\src\\main\\java\\com\\example\\demo25\\game.csv");

        if (gameList != null && !gameList.isEmpty()) {
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:games.db")) {
                Statement statement = connection.createStatement();

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS Games (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "platform TEXT," +
                        "year INTEGER," +
                        "genre TEXT," +
                        "publisher TEXT," +
                        "naSales REAL," +
                        "euSales REAL," +
                        "jpSales REAL," +
                        "otherSales REAL," +
                        "globalSales REAL)");

                for (Game game : gameList) {
                    String sql = "INSERT INTO Games (name, platform, year, genre, publisher, naSales, euSales, jpSales, otherSales, globalSales) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, game.getName());
                    preparedStatement.setString(2, game.getPlatform());
                    preparedStatement.setInt(3, game.getYear());
                    preparedStatement.setString(4, game.getGenre());
                    preparedStatement.setString(5, game.getPublisher());
                    preparedStatement.setDouble(6, game.getNaSales());
                    preparedStatement.setDouble(7, game.getEuSales());
                    preparedStatement.setDouble(8, game.getJpSales());
                    preparedStatement.setDouble(9, game.getOtherSales());
                    preparedStatement.setDouble(10, game.getGlobalSales());
                    preparedStatement.executeUpdate();
                }

                String query1 = "SELECT platform, AVG(globalSales) AS avgSales FROM Games GROUP BY platform";
                ResultSet resultSet = statement.executeQuery(query1);

                while (resultSet.next()) {
                    String platform = resultSet.getString("platform");
                    double avgSales = resultSet.getDouble("avgSales");
                    System.out.println("Platform: " + platform + ", Average Global Sales: " + avgSales);
                }

                String query2 = "SELECT name FROM Games WHERE year = 2000 ORDER BY euSales DESC LIMIT 1";
                ResultSet resultSet2 = statement.executeQuery(query2);

                if (resultSet2.next()) {
                    String gameName = resultSet2.getString("name");
                    System.out.println("Game with the highest EU sales in 2000: " + gameName);
                }

                String query3 = "SELECT name FROM Games WHERE year BETWEEN 2000 AND 2006 AND genre = 'Sports' ORDER BY jpSales DESC LIMIT 1";
                ResultSet resultSet3 = statement.executeQuery(query3);

                if (resultSet3.next()) {
                    String gameName = resultSet3.getString("name");
                    System.out.println("Sports game with the highest JP sales between 2000 and 2006: " + gameName);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<Game> parseCSV(String filename) {
        List<Game> gameList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String[] data = line.split(",");

                Game game = new Game();
                try {
                    game.setRank(Integer.parseInt(data[0]));
                    game.setName(data[1]);
                    game.setPlatform(data[2]);
                    game.setYear(parseYear(data[3]));
                    game.setGenre(data[4]);
                    game.setPublisher(data[5]);
                    game.setNaSales(parseDoubleOrZero(data[6]));
                    game.setEuSales(parseDoubleOrZero(data[7]));
                    game.setJpSales(parseDoubleOrZero(data[8]));
                    game.setOtherSales(parseDoubleOrZero(data[9]));
                    game.setGlobalSales(parseDoubleOrZero(data[10]));

                    gameList.add(game);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid data: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return gameList;
    }

    private static int parseYear(String value) {
        if ("N/A".equals(value)) {
            return 0; // Заменяем "N/A" на 0
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0; // Если не удается преобразовать, возвращаем 0
        }
    }

    private static double parseDoubleOrZero(String value) {
        if ("N/A".equals(value)) {
            return 0.0; // Заменяем "N/A" на 0.0
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0; // Если не удается преобразовать, возвращаем 0.0
        }
    }
    }