package com.mooo.amksoft.amkmcauth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
//import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
//import org.bukkit.plugin.Plugin;

import com.google.common.io.PatternFilenameFilter;

public class PConfManager extends YamlConfiguration {
	
    private static final Map<UUID, PConfManager> pcms = new HashMap<>();
    private final Object saveLock = new Object();
    private File pconfl = null;
    private static String VipPlayers = "";
    private static int PlayerCount = 0;
    //private static String IpAdresses[] = {};
    private static List<String> IpAdresses = new ArrayList<String>();
    private static List<Integer> IpAdressesCnt = new ArrayList<Integer>();
    

    /**
     * Player configuration manager
     *
     * @param p Player to manage
     */
    PConfManager(OfflinePlayer p) {
        super();
        File dataFolder = AmkMcAuth.dataFolder;
        this.pconfl = new File(dataFolder + File.separator + "userdata" + File.separator + p.getUniqueId() + ".yml");
        try {
            load(this.pconfl);
        } catch (Exception ignored) {
        }
    }

    /**
     * Player configuration manager.
     *
     * @param u Player to manage
     */
    PConfManager(UUID u) {
        super();
        File dataFolder = AmkMcAuth.dataFolder;
        this.pconfl = new File(dataFolder + File.separator + "userdata" + File.separator + u + ".yml");
        try {
            load(this.pconfl);
        } catch (Exception ignored) {
        }
    }

    /**
     * No outside construction, please.
     */
    //@SuppressWarnings("unused")
    PConfManager() {
    }

    public static PConfManager getPConfManager(Player p) {
        return PConfManager.getPConfManager(p.getUniqueId());
    }

    public static PConfManager getPConfManager(UUID u) {
        synchronized (PConfManager.pcms) {
            if (PConfManager.pcms.containsKey(u)) return PConfManager.pcms.get(u);
            final PConfManager pcm = new PConfManager(u);
            PConfManager.pcms.put(u, pcm);
            return pcm;
        }
    }

    public static void saveAllManagers() {
        synchronized (PConfManager.pcms) {
        	// pcm only exists if player has joined.. 
            for (PConfManager pcm : PConfManager.pcms.values()) {
            	// Skip Save if "login.password" NOT set (=null/removed)
            	if(pcm.isSet("login.password")) {
            		pcm.forceSave();
            		// Bukkit.getLogger for Debugging 
            		// Bukkit.getLogger().log(Level.INFO, "Saving: " + pcm.getString("login.username") + ":"); // Debug
            	}
            }
        }
    }

    public void forceSave() {
        synchronized (this.saveLock) {
            try {
                save(this.pconfl);
            } catch (IOException ignored) {
            }
        }
    }

    
    /** 
     * Get all PlayerProfile Files and parse info in it.
     * Remembers IP-Addresses and VIP players. Call from onEnabled.
     */
    public static void countPlayersFromIpAndGetVipPlayers() {    	
    	
		String PlayerFound;
		PlayerCount=0;
		
		boolean Aanwezig=false;
        final File userdataFolder = new File(AmkMcAuth.dataFolder, "userdata");
        if (!userdataFolder.exists() || !userdataFolder.isDirectory()) return;
        for (String fileName : userdataFolder.list(new PatternFilenameFilter("(?i)^.+\\.yml$"))) {
        	Scanner in;
        	Aanwezig=false;
        	PlayerFound="";
        	PlayerCount++;
			try {
				in = new Scanner(new File(userdataFolder + File.separator + fileName));
	        	//while (in.hasNextLine()) { // iterates each line in the file
		        while (in.hasNext()) { // 1 more character?: iterates each line in the file
	        	    String line = in.nextLine();
	        	    if(line.contains("username:")) {
	        	    	PlayerFound = line.substring(line.lastIndexOf(" ")+1) + " ";
	                	Aanwezig=true;
	        	    }
	        	    if(line.contains("vip:") && Aanwezig){
	        	    	VipPlayers = VipPlayers + PlayerFound;
	        	    }
	        	    if(line.contains("ipaddress:") && Config.maxUsersPerIpaddress>0 ){
	        	    	addPlayerToIp(line.substring(line.lastIndexOf(" ")+1));
	        	    }
		        }
	        	in.close(); // don't forget to close resource leaks
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }        
    }
    
    /** 
     * return number of Player Profiles (Registered Players).
     * Used only in startup to count for bStats statistics.
     */
    public static int getPlayerCount() {
    	return PlayerCount;
    }

    /** 
     * return number of unique IP-adresses.
     * Used in amka command (Only for Debug purposes).
     */
    public static int getIpaddressCount() {
    	return IpAdresses.size();
    }

    /** 
     * return selected record from IP-adress.
     * Used in amka command (Only for Debug purposes).
     */
    public static String listIpaddressesInfo(int i) {
		return IpAdresses.get(i) + "  " + IpAdressesCnt.get(i);
    }

    /** 
     * return total playercount from 1 IP-adress.
     * Used in register to check for maximum.
     */
    public static int countPlayersFromIp(String IpAddress) {
    	int PlIdx = IpAdresses.indexOf(IpAddress);
    	if(PlIdx==-1) return 0;
    	return IpAdressesCnt.get(PlIdx);
    }

    /** 
     * Add 1 player to the IP-adress playercount.
     * Used in register 
     */
    public static void addPlayerToIp(String IpAddress) {
    	if(Config.maxUsersPerIpaddress==0) return; // No Player-Counting
    	int PlIdx = IpAdresses.indexOf(IpAddress);
    	if(PlIdx==-1) {
			IpAdresses.add(IpAddress);
			IpAdressesCnt.add(1);
    	}
    	else {
			IpAdressesCnt.set(PlIdx,IpAdressesCnt.get(PlIdx)+1);
    	}
    }

    /** 
     * Reduce playercount from 1 IP-Adress with 1.
     * Used in unregister 
     */
    public static void removePlayerFromIp(String IpAddress) {
    	if(Config.maxUsersPerIpaddress==0) return; // No Player-Counting
    	int PlIdx = IpAdresses.indexOf(IpAddress);
    	if(PlIdx>=0) {
    		int Cntr = IpAdressesCnt.get(PlIdx);
    		if(Cntr>0) IpAdressesCnt.set(PlIdx,Cntr-1);
    	}
    }

    /** 
     * Show Players on the VIP-Player list 
     */
    public static String getVipPlayers() {
    	return VipPlayers;
    }

    /** 
     * Show Players on the VIP-Player list 
     */
    public static int getVipPlayerCount() {
    	if (VipPlayers.isEmpty())
    	    return 0;
    	return VipPlayers.split("\\s+").length; // separate string around spaces
    }

    /** 
     * Add a Player to the VIP-Player list. 
     * Used in nlpadd
     */
    public static void addVipPlayer(String NewPlayer) {
	    if(!VipPlayers.contains(" " + NewPlayer + " ")){
	    	VipPlayers = VipPlayers + NewPlayer + " ";
	    }
    }    

    /** 
     * Remove a Player from the VIP-Player list. 
     * Used in nlprem
     */
    public static void removeVipPlayer(String PlayerToRemove) {
	    if(!VipPlayers.contains(" " + PlayerToRemove + " ")){
	    	VipPlayers.replace(" " + PlayerToRemove + " ", " ");
	    }
    }

    
    public static void purge() {
        synchronized (PConfManager.pcms) {
            PConfManager.pcms.clear();
        }
    }

    public boolean exists() {
        return this.pconfl.exists();
    }

    public boolean createFile() {
        try {
            return this.pconfl.createNewFile();
        } catch (IOException ignored) {
            return false;
        }
    }

    public static void removePlayer(UUID u) {
        synchronized (PConfManager.pcms) {
            if (PConfManager.pcms.containsKey(u)) {
            	PConfManager.pcms.clear();
            	File dataFolder = AmkMcAuth.dataFolder;
            	File rfile = new File(dataFolder + File.separator + "userdata" + File.separator + u + ".yml");
                if (rfile.exists()) rfile.delete();  // Als bestaat dan verwijderen..
            }            
        }
    }

    /**
     * Gets a Location from config
     * <p/>
     * This <strong>will</strong> throw an exception if the saved Location is invalid or has missing parts.
     *
     * @param path Path in the yml to fetch from
     * @return Location or null if path does not exist or if config doesn't exist
     */
    public Location getLocation(String path) {
        if (this.get(path) == null) return null;
        String world = this.getString(path + ".w");
        double x = this.getDouble(path + ".x");
        double y = this.getDouble(path + ".y");
        double z = this.getDouble(path + ".z");
        float pitch = this.getFloat(path + ".pitch");
        float yaw = this.getFloat(path + ".yaw");
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    /**
     * Sets a location in config
     *
     * @param value Location to set
     * @param path  Path in the yml to set
     */
    public void setLocation(String path, Location value) {
        this.set(path + ".w", value.getWorld().getName());
        this.set(path + ".x", value.getX());
        this.set(path + ".y", value.getY());
        this.set(path + ".z", value.getZ());
        this.set(path + ".pitch", value.getPitch());
        this.set(path + ".yaw", value.getYaw());
    }

    public float getFloat(String path) {
        return (float) this.getDouble(path);
    }
}
