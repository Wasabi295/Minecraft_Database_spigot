package bd_proiect.bd_project_gr5;

import java.util.Date;

public class Player_stats {

    private String uuid;
    private int deaths;
    private int kills;
    private long blocksBroken;
    private Date lastLogin;
    private Date lastLogout;
    private double  balance;

    public Player_stats(String uuid, int deaths, int kills, long blocksBroken,double balance , Date lastLogin, Date lastLogout) {
        this.uuid = uuid;
        this.deaths = deaths;
        this.kills = kills;
        this.blocksBroken = blocksBroken;
        this.balance = balance;
        this.lastLogin = lastLogin;
        this.lastLogout = lastLogout;

    }


    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public long getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(long blocksBroken) {
        this.blocksBroken = blocksBroken;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getLastLogout() {
        return lastLogout;
    }

    public void setLastLogout(Date lastLogout) {
        this.lastLogout = lastLogout;
    }
}
