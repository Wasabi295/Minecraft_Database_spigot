package bd_proiect.bd_project_gr5;

import bd_proiect.bd_project_gr5.Listeners.Listeners;
import bd_proiect.bd_project_gr5.db.DatadeBaze;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Bd_project_gr5 extends JavaPlugin {

    private DatadeBaze db;

    @Override
    public void onEnable() {
        try {
            this.db = new DatadeBaze();
            db.initializareDateDeBaze(); // Creează tabelele la pornire
            db.logServerEvent("Server Start", "Server has started successfully.");
        } catch (SQLException ex) {
            getLogger().severe("Nu s-a putut conecta la baza de date și crea tabelele!");
            ex.printStackTrace();
        }

        // Înregistrează evenimentele listener-ului
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
    }

    @Override
    public void onDisable() {
        if (this.db != null) {
            this.db.closeConnection(); // Închide conexiunea la baza de date
        }
    }

    // Metodă pentru a obține conexiunea la baza de date
    public DatadeBaze getDb() {
        return db;
    }
}
