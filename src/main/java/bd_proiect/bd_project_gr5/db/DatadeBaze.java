package bd_proiect.bd_project_gr5.db;

import bd_proiect.bd_project_gr5.Player_stats;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatadeBaze {
    private Connection connection;

    public Connection getConnection() throws SQLException {
        if (connection != null) {
            return connection;
        }

        String url = "jdbc:mysql://localhost:3306/minecraft";
        String user = "root";
        String password = "Energosos1991-";

        this.connection = DriverManager.getConnection(url, user, password);
        return this.connection;
    }

    public void initializareDateDeBaze() throws SQLException {
        Statement statement = getConnection().createStatement();

        String sqlPlayerStats = """
                CREATE TABLE IF NOT EXISTS player_stats (
                    uuid VARCHAR(36) PRIMARY KEY,
                    deaths INT,
                    kills INT,
                    block_broken BIGINT,
                    balance DOUBLE,
                    last_login DATE,
                    last_logout DATE
                )
                """;
        statement.execute(sqlPlayerStats);

        String sqlPlayerInventory = """
                CREATE TABLE IF NOT EXISTS player_inventory (
                    uuid VARCHAR(36),
                    item_id VARCHAR(255),
                    quantity INT,
                    slot INT,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (uuid, item_id, slot)
                )
                """;
        statement.execute(sqlPlayerInventory);


        statement.close();
        System.out.println("Tabelele au fost create sau deja există.");
    }

    public Player_stats findPlayerStatsBYUUID(String uuid) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(
                "SELECT * FROM player_stats WHERE uuid = ?"
        );
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return new Player_stats(
                    uuid,
                    resultSet.getInt("deaths"),
                    resultSet.getInt("kills"),
                    resultSet.getLong("block_broken"),
                    resultSet.getDouble("balance"),
                    resultSet.getDate("last_login"),
                    resultSet.getDate("last_logout")
            );
        }

        statement.close();
        return null;
    }

    public void createPlayerStats(Player_stats stats) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(
                "INSERT INTO player_stats(uuid, deaths, kills, block_broken, balance, last_login, last_logout) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)"
        );

        statement.setString(1, stats.getUuid());
        statement.setInt(2, stats.getDeaths());
        statement.setInt(3, stats.getKills());
        statement.setLong(4, stats.getBlocksBroken());
        statement.setDouble(5, stats.getBalance());
        statement.setDate(6, new java.sql.Date(stats.getLastLogin().getTime()));
        statement.setDate(7, stats.getLastLogout() != null ? new java.sql.Date(stats.getLastLogout().getTime()) : null);

        statement.executeUpdate();
        statement.close();
    }

    public void updatePlayerStats(Player_stats stats) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(
                "UPDATE player_stats SET deaths = ?, kills = ?, block_broken = ?, balance = ?, last_login = ?, last_logout = ? WHERE uuid = ?"
        );

        statement.setInt(1, stats.getDeaths());
        statement.setInt(2, stats.getKills());
        statement.setLong(3, stats.getBlocksBroken());
        statement.setDouble(4, stats.getBalance());
        statement.setDate(5, new java.sql.Date(stats.getLastLogin().getTime()));
        statement.setDate(6, stats.getLastLogout() != null ? new java.sql.Date(stats.getLastLogout().getTime()) : null);
        statement.setString(7, stats.getUuid());

        statement.executeUpdate();
        statement.close();
    }

    // Modificare: Previne duplicarea item-urilor
    public void addItemToInventory(String playerUUID, String itemId, int quantity, int slot) throws SQLException {
        // Verifică dacă item-ul există deja în inventar
        String checkQuery = "SELECT quantity FROM player_inventory WHERE uuid = ? AND item_id = ? AND slot = ?";

        try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
            checkStatement.setString(1, playerUUID);
            checkStatement.setString(2, itemId);
            checkStatement.setInt(3, slot);

            ResultSet resultSet = checkStatement.executeQuery();
            if (resultSet.next()) {
                // Dacă există deja, actualizează cantitatea
                int currentQuantity = resultSet.getInt("quantity");
                int newQuantity = currentQuantity + quantity;

                String updateQuery = "UPDATE player_inventory SET quantity = ? WHERE uuid = ? AND item_id = ? AND slot = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setInt(1, newQuantity);
                    updateStatement.setString(2, playerUUID);
                    updateStatement.setString(3, itemId);
                    updateStatement.setInt(4, slot);
                    updateStatement.executeUpdate();
                    System.out.println("Cantitatea item-ului a fost actualizată.");
                }
            } else {
                // Dacă nu există, adaugă un nou item
                String insertQuery = "INSERT INTO player_inventory (uuid, item_id, quantity, slot) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                    insertStatement.setString(1, playerUUID);
                    insertStatement.setString(2, itemId);
                    insertStatement.setInt(3, quantity);
                    insertStatement.setInt(4, slot);
                    insertStatement.executeUpdate();
                    System.out.println("Item adăugat în inventar.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Eroare la adăugarea item-ului în inventar.");
        }
    }

    public List<ItemStack> getPlayerInventory(String uuid) throws SQLException {
        String query = "SELECT item_id, quantity, slot FROM player_inventory WHERE uuid = ?";
        PreparedStatement statement = getConnection().prepareStatement(query);
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();
        List<ItemStack> inventory = new ArrayList<>();
        while (resultSet.next()) {
            String itemId = resultSet.getString("item_id");
            int quantity = resultSet.getInt("quantity");

            Material material = Material.matchMaterial(itemId);
            if (material != null) {
                inventory.add(new ItemStack(material, quantity));
            }
        }

        statement.close();
        return inventory;
    }


    public void updatePlayerLocation(String uuid, double x, double y, double z, String world) throws SQLException {
        String sql = "{CALL updatePlayerLocation(?, ?, ?, ?, ?)}";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setDouble(2, x);
            statement.setDouble(3, y);
            statement.setDouble(4, z);
            statement.setString(5, world);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Eroare la actualizarea locației jucătorului.");
        }
    }


    public void logServerEvent(String eventType, String eventDescription) {
        String sql = "CALL addServerEvent(?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, eventType);
            stmt.setString(2, eventDescription);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // Adaugă sau actualizează un jucător în tabela Players
    public void addOrUpdatePlayer(String playerUUID, String playerName) throws SQLException {
        String sql = """
        INSERT INTO Players (player_uuid, player_name, last_login) 
        VALUES (?, ?, CURRENT_TIMESTAMP)
        ON DUPLICATE KEY UPDATE
        player_name = VALUES(player_name),
        last_login = CURRENT_TIMESTAMP
    """;
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, playerUUID);
            stmt.setString(2, playerName);
            stmt.executeUpdate();
        }
    }

    // Actualizează ultima deconectare a unui jucător
    public void updatePlayerLogout(String playerUUID) throws SQLException {
        String sql = "UPDATE Players SET last_logout = CURRENT_TIMESTAMP WHERE player_uuid = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, playerUUID);
            stmt.executeUpdate();
        }
    }


    public void addAchievement(String achievementName, String description) throws SQLException {
        String sql = "INSERT INTO Achievements (achievement_name, description) VALUES (?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, achievementName);
            stmt.setString(2, description);
            stmt.executeUpdate();
        }
    }





    public void closeConnection() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
