package bd_proiect.bd_project_gr5.Listeners;

import bd_proiect.bd_project_gr5.Bd_project_gr5;
import bd_proiect.bd_project_gr5.Player_stats;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class Listeners implements Listener {

    private final Bd_project_gr5 plugin;

    public Listeners(Bd_project_gr5 plugin) {
        this.plugin = plugin;
    }



    @EventHandler
    public void onPlayerJoin2(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        try {
            plugin.getDb().addOrUpdatePlayer(p.getUniqueId().toString(), p.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @EventHandler
    public void onPlayerQuit2(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        try {
            plugin.getDb().updatePlayerLogout(p.getUniqueId().toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) throws SQLException {
        Player p = e.getPlayer();
        Location newLocation = p.getLocation();

        // Actualizăm locația jucătorului în baza de date
        plugin.getDb().updatePlayerLocation(p.getUniqueId().toString(),
                newLocation.getX(),
                newLocation.getY(),
                newLocation.getZ(),
                newLocation.getWorld().getName());
    }



    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Material blockType = e.getBlock().getType();
        String blockName = blockType.toString();

        int slot = 0;

        if (blockType == Material.OAK_LOG) {
            slot = 1;
        } else if (blockType == Material.DIRT) {
            slot = 2;
        } else {
            slot = 0;
        }

        try {
            Player_stats stats = getPlayerStatsFromDatabase(p);
            stats.setBlocksBroken(stats.getBlocksBroken() + 1);
            stats.setBalance(stats.getBalance() + 0.5);

            String itemId = blockType.name();
            int quantity = 1;

            Material material = Material.matchMaterial(itemId);
            if (material != null) {
                this.plugin.getDb().addItemToInventory(p.getUniqueId().toString(), itemId, quantity, slot);
            } else {
                p.sendMessage("Itemul nu este valid sau nu există în baza de date.");
            }

            this.plugin.getDb().updatePlayerStats(stats);

            p.sendMessage("Ai spart un bloc de tipul " + blockName + " și îți sunt actualizate statisticile!");
        } catch (SQLException exception) {
            exception.printStackTrace();
            p.sendMessage("A apărut o eroare la actualizarea inventarului și statisticilor.");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        try {
            Player_stats playerStats = getPlayerStatsFromDatabase(p);
            if (playerStats == null) {
                playerStats = new Player_stats(p.getUniqueId().toString(), 0, 0, 0, 0.0, new Date(), null);
                plugin.getDb().createPlayerStats(playerStats);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        try {
            Player_stats stats = getPlayerStatsFromDatabase(p);
            stats.setLastLogout(new Date());
            plugin.getDb().updatePlayerStats(stats);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        Player p = e.getEntity();



        if(killer == null)
        {
            return;
        }

        try{

            Player_stats pStats = getPlayerStatsFromDatabase(p);
            pStats.setDeaths(pStats.getDeaths() + 1);
            pStats.setBalance(pStats.getBalance() - 1.0);
            this.plugin.getDb().updatePlayerStats(pStats);

            Player_stats killerStats = getPlayerStatsFromDatabase(killer);
            killerStats.setKills(killerStats.getKills() + 1);
            killerStats.setBalance(killerStats.getBalance() + 1.0);


            this.plugin.getDb().updatePlayerStats(killerStats);


        }catch(SQLException ex){
            ex.printStackTrace();
        }




    }




    public void unlockAchievement(String playerUUID, String achievementName) throws SQLException {
        // Obține player_id din Players
        String getPlayerIdQuery = "SELECT player_id FROM Players WHERE player_uuid = ?";
        try (PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement(getPlayerIdQuery)) {
            stmt.setString(1, playerUUID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int playerId = rs.getInt("player_id");

                // Obține achievement_id din Achievements folosind achievement_name
                String getAchievementIdQuery = "SELECT achievement_id FROM Achievements WHERE achievement_name = ?";
                try (PreparedStatement achievementStmt = plugin.getDb().getConnection().prepareStatement(getAchievementIdQuery)) {
                    achievementStmt.setString(1, achievementName);
                    ResultSet achievementRs = achievementStmt.executeQuery();
                    if (achievementRs.next()) {
                        int achievementId = achievementRs.getInt("achievement_id");

                        // Verifică dacă realizarea există deja pentru jucător
                        String checkQuery = "SELECT * FROM PlayerAchievements WHERE player_id = ? AND achievement_id = ?";
                        try (PreparedStatement checkStmt = plugin.getDb().getConnection().prepareStatement(checkQuery)) {
                            checkStmt.setInt(1, playerId);
                            checkStmt.setInt(2, achievementId);
                            ResultSet checkRs = checkStmt.executeQuery();
                            if (!checkRs.next()) {
                                // Dacă nu există, adaugă realizarea
                                String insertQuery = "INSERT INTO PlayerAchievements (player_id, achievement_id) VALUES (?, ?)";
                                try (PreparedStatement insertStmt = plugin.getDb().getConnection().prepareStatement(insertQuery)) {
                                    insertStmt.setInt(1, playerId);
                                    insertStmt.setInt(2, achievementId);
                                    insertStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        }
    }








    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            String playerUUID = killer.getUniqueId().toString();
            try {
                unlockAchievement(playerUUID, "Primul Kill");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private Player_stats getPlayerStatsFromDatabase(Player p) throws SQLException {
        return plugin.getDb().findPlayerStatsBYUUID(p.getUniqueId().toString());
    }
}
