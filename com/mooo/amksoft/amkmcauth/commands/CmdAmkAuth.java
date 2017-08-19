package com.mooo.amksoft.amkmcauth.commands;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Charsets;
import com.mooo.amksoft.amkmcauth.AuthPlayer;
import com.mooo.amksoft.amkmcauth.Config;
import com.mooo.amksoft.amkmcauth.Language;
import com.mooo.amksoft.amkmcauth.PConfManager;
import com.mooo.amksoft.amkmcauth.AmkMcAuth;
import com.mooo.amksoft.amkmcauth.AmkAUtils;

public class CmdAmkAuth implements CommandExecutor {

    private final AmkMcAuth plugin;
    public static File dataFolder; // DirectoryNaam of  DataDirectory

    public CmdAmkAuth(AmkMcAuth instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if ( !cmd.getName().equalsIgnoreCase("")) {
            if (!cs.hasPermission("amkauth.")) {
                AmkAUtils.dispNoPerms(cs);
                return true;
            }
            if (args.length < 1) {
                cs.sendMessage(cmd.getDescription());
                return false;
            }
            if (cs instanceof Player) {
                AuthPlayer ap = AuthPlayer.getAuthPlayer(((Player) cs).getUniqueId());
                if (!ap.isLoggedIn()) {
                    cs.sendMessage(ChatColor.RED + Language.YOU_MUST_LOGIN.toString());
                    return true;
                }
            }
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "help":
                    cs.sendMessage(ChatColor.BLUE + Language.ADMIN_HELP.toString());
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " changepassword [player] [newpassword]" + ChatColor.BLUE + " - " + Language.HELP_CHANGEPASSWORD);
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " login [player]" + ChatColor.BLUE + " - " + Language.HELP_LOGIN);
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " logout [player]" + ChatColor.BLUE + " - " + Language.HELP_LOGOUT);
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " register [player] [password]" + ChatColor.BLUE + " - " + Language.HELP_REGISTER);
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " unregister [player]" + ChatColor.BLUE + " - " + Language.HELP_UNREGISTER);
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " getuuid [player]" + ChatColor.BLUE + " - " + Language.HELP_GETUUID);
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " nlplist " + ChatColor.BLUE + " - " + Language.HELP_NLPLIST);
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " nlpadd [player]" + ChatColor.BLUE + " - " + Language.HELP_NLPADD);
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " nlprem [player]" + ChatColor.BLUE + " - " + Language.HELP_NLPREM);
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " ipcount" + ChatColor.BLUE + " - (debug) Show total Login Ip-Adresses");
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " iplist" + ChatColor.BLUE + " - (debug) list out Login Ip-Adresses");
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " reload" + ChatColor.BLUE + " - " + Language.HELP_RELOAD);
                    cs.sendMessage(ChatColor.GRAY + "  /" + label + " help" + ChatColor.BLUE + " - " + Language.HELP_HELP);
                    break;
                case "changepassword": {
                    //if (cs instanceof Player) ((Player) cs).getLocation().getWorld().setGameRuleValue("logAdminCommands", "true");
                    if (args.length < 3) {
                        cs.sendMessage(ChatColor.RED + Language.NOT_ENOUGH_ARGUMENTS.toString() + " " + Language.TRY + " " + ChatColor.GRAY + "/" + label + " help" + ChatColor.RED + ".");
                        return true;
                    }
                    AuthPlayer ap = AuthPlayer.getAuthPlayer(args[1]);
                    
                    if (ap == null) {
                        cs.sendMessage(ChatColor.RED + Language.ERROR_OCCURRED.toString());
                        return true;
                    }
                    if (!ap.isRegistered()) {
                        cs.sendMessage(ChatColor.RED + Language.PLAYER_NOT_REGISTERED.toString());
                        return true;
                    }
                    if (ap.setPassword(args[2], Config.passwordHashType))
                        cs.sendMessage(ChatColor.BLUE + Language.PASSWORD_CHANGED.toString());
                    else cs.sendMessage(ChatColor.RED + Language.PASSWORD_COULD_NOT_BE_CHANGED.toString());

                    break;
                }
                case "login": {
                    //if (cs instanceof Player) ((Player) cs).getLocation().getWorld().setGameRuleValue("logAdminCommands", "true");
                    if (args.length < 2) {
                        cs.sendMessage(ChatColor.RED + Language.NOT_ENOUGH_ARGUMENTS.toString() + " " + Language.TRY + " " + ChatColor.GRAY + "/" + label + " help" + ChatColor.RED + ".");
                        return true;
                    }
                    AuthPlayer ap = AuthPlayer.getAuthPlayer(args[1]);
                    if (ap == null) {
                        cs.sendMessage(ChatColor.RED + Language.ERROR_OCCURRED.toString());
                        return true;
                    }
                    Player p = ap.getPlayer();
                    if (p == null) {
                        cs.sendMessage(ChatColor.RED + Language.PLAYER_NOT_ONLINE.toString());
                        return true;
                    }
                    ap.login();
                    this.plugin.getLogger().info(p.getName() + " " + Language.HAS_LOGGED_IN);
                    cs.sendMessage(ChatColor.BLUE + Language.PLAYER_LOGGED_IN.toString());
                    break;
                }
                case "logout": {
                    if (args.length < 2) {
                        cs.sendMessage(ChatColor.RED + Language.NOT_ENOUGH_ARGUMENTS.toString() + " " + Language.TRY + " " + ChatColor.GRAY + "/" + label + " help" + ChatColor.RED + ".");
                        return true;
                    }
                    AuthPlayer ap = AuthPlayer.getAuthPlayer(args[1]);
                    if (ap == null) {
                        cs.sendMessage(ChatColor.RED + Language.ERROR_OCCURRED.toString());
                        return true;
                    }
                    Player p = ap.getPlayer();
                    if (p == null) {
                        cs.sendMessage(ChatColor.RED + Language.PLAYER_NOT_ONLINE.toString());
                        return true;
                    }
                    if (!ap.isLoggedIn()) {
                        cs.sendMessage(ChatColor.RED + Language.PLAYER_NOT_LOGGED_IN.toString());
                        return true;
                    }
                    ap.logout(this.plugin);
                    cs.sendMessage(ChatColor.BLUE + Language.PLAYER_LOGGED_OUT.toString());
                    break;
                }
                case "register": {
                    //if (cs instanceof Player) ((Player) cs).getLocation().getWorld().setGameRuleValue("logAdminCommands", "true");
                    if (args.length < 3) {
                        cs.sendMessage(ChatColor.RED + Language.NOT_ENOUGH_ARGUMENTS.toString() + " " + Language.TRY + " " + ChatColor.GRAY + "/" + label + " help" + ChatColor.RED + ".");
                        return true;
                    }
                    AuthPlayer ap = AuthPlayer.getAuthPlayer(args[1]);
                    if (ap == null) {
                        cs.sendMessage(ChatColor.RED + Language.ERROR_OCCURRED.toString());
                        return true;
                    }
                    if (ap.isRegistered()) {
                        cs.sendMessage(ChatColor.RED + Language.PLAYER_ALREADY_REGISTERED.toString());
                        return true;
                    }
                    String rawPassword = args[2];
                    for (String disallowed : Config.disallowedPasswords) {
                        if (!rawPassword.equalsIgnoreCase(disallowed)) continue;
                        cs.sendMessage(ChatColor.RED + Language.DISALLOWED_PASSWORD.toString());
                        return true;
                    }
                    final String name = AmkAUtils.forceGetName(ap.getUniqueId());
                    if (ap.setPassword(rawPassword, Config.passwordHashType)) {
                        //cs.sendMessage(ChatColor.BLUE + String.format(Language.REGISTERED_SUCCESSFULLY.toString(), ChatColor.GRAY + name + ChatColor.BLUE));
                        if(name!=args[1]) ap.setUserName(args[1]); //name not set?, set it!
                    	cs.sendMessage(ChatColor.BLUE + String.format(Language.REGISTERED_SUCCESSFULLY.toString(), ChatColor.GRAY + args[1] + ChatColor.BLUE));
                        PConfManager.addPlayerToIp(ap.getCurrentIPAddress());
                    }
                    else
                        //cs.sendMessage(ChatColor.RED + String.format(Language.COULD_NOT_REGISTER.toString(), ChatColor.GRAY + name + ChatColor.RED));
                    	cs.sendMessage(ChatColor.RED + String.format(Language.COULD_NOT_REGISTER.toString(), ChatColor.GRAY + args[1] + ChatColor.RED));
                    break;
                }
                case "unregister": {
                    //if (cs instanceof Player) ((Player) cs).getLocation().getWorld().setGameRuleValue("logAdminCommands", "true");
                    if (args.length < 2) {
                        cs.sendMessage(ChatColor.RED + Language.NOT_ENOUGH_ARGUMENTS.toString() + " " + Language.TRY + " " + ChatColor.GRAY + "/" + label + " help" + ChatColor.RED + ".");
                        return true;
                    }
                    AuthPlayer ap = AuthPlayer.getAuthPlayer(args[1]);
                    if (ap == null) {
                        cs.sendMessage(ChatColor.RED + Language.ERROR_OCCURRED.toString());
                        return true;
                    }
                    if (!ap.isRegistered()) {
                        cs.sendMessage(ChatColor.RED + Language.PLAYER_NOT_REGISTERED.toString());
                        return true;
                    }
                    PConfManager.removePlayer(ap.getUniqueId());
                    if (ap.isLoggedIn()) ap.logout(this.plugin);
                    ap.removeThisPlayer();
                    PConfManager.removePlayerFromIp(ap.getCurrentIPAddress());
                	cs.sendMessage(ChatColor.BLUE + Language.PLAYER_REMOVED.toString());
                    break;
                }
                case "getuuid": {
                    if (args.length < 2) {
                        cs.sendMessage(ChatColor.RED + Language.NOT_ENOUGH_ARGUMENTS.toString() + " " + Language.TRY + " " + ChatColor.GRAY + "/" + label + " help" + ChatColor.RED + ".");
                        return true;
                    }
                    
                    boolean Online=true;
                    UUID u;
                    
                	if(Bukkit.getOnlineMode()!= Online) {
                		// Server runs 'OffLine' AmkMcAuth calculates the UUID for this player...
                	    u = UUID.nameUUIDFromBytes(("OfflinePlayer:" + args[1]).getBytes(Charsets.UTF_8));    		
            		}
                	else
                		{
                		try {
                			u = AmkAUtils.getUUID(args[1]);
                		} catch (Exception ex) {
                			//ex.printStackTrace();
                            u = null;        		
                    	}
                    }
                	if (u == null) {
                        cs.sendMessage(ChatColor.RED + Language.ERROR_OCCURRED.toString());
                        return true;
                    }
                    cs.sendMessage(ChatColor.BLUE + "UUID of Player: " + ChatColor.GRAY + args[1]+ ChatColor.BLUE + " is: " + ChatColor.GRAY + u + ChatColor.BLUE);
                    break;
                }
                case "nlplist": {
                    // https://bukkit.org/threads/get-a-players-minecraft-language.172468/
                    
                    //final File userdataFolder = new File(AmkMcAuth.dataFolder, "userdata");
                    //if (!userdataFolder.exists() || !userdataFolder.isDirectory()) break;

                    //String VipPlayers = "" ;
                    //UUID p; // Global Definition
                    
                    //for (String fileName : userdataFolder.list(new PatternFilenameFilter("(?i)^.+\\.yml$"))) {
                    //    String playerName = fileName.substring(0, fileName.length() - 4); // ".yml" = 4
                    //    try {
                    //        //noinspection ResultOfMethodCallIgnored
                    //        p = UUID.fromString(playerName);
                    //    } catch (IllegalArgumentException ignored) {
                    //    	continue; // Skip this file
                    //    }

                    //    AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
                    //    if (ap.isVIP()) VipPlayers = VipPlayers + ap.getUserName() + " ";
                    //}
                    
                    String VipPlayers = PConfManager.getVipPlayers();                    
                    if(VipPlayers.trim().length()>0){
                        cs.sendMessage(Language.NLP_LIST_PLAYERS.toString());
                        cs.sendMessage(VipPlayers);
                    }
                    else
                        cs.sendMessage(Language.NLP_LIST_PLAYERS_NONE.toString());

                    break;
                }
                case "nlpadd": {
                    if (args.length < 2) {
                        cs.sendMessage(ChatColor.RED + Language.NOT_ENOUGH_ARGUMENTS.toString() + " " + Language.TRY + " " + ChatColor.GRAY + "/" + label + " help" + ChatColor.RED + ".");
                        return true;
                    }
                    AuthPlayer ap = AuthPlayer.getAuthPlayer(args[1]);
                    if (ap == null) {
                        cs.sendMessage(ChatColor.RED + Language.ERROR_OCCURRED.toString());
                        return true;
                    }
                    final String name = AmkAUtils.forceGetName(ap.getUniqueId());
                    if (!ap.isRegistered()) {
                        cs.sendMessage(ChatColor.RED + Language.PLAYER_NOT_REGISTERED.toString());
                        return true;
                    }
                    ap.setVIP(true);
                    cs.sendMessage(ChatColor.BLUE + String.format(Language.NLP_SET_UPDATED.toString(), ChatColor.GRAY + name + ChatColor.BLUE));
                    break;
                }
                case "nlprem": {
                    if (args.length < 2) {
                        cs.sendMessage(ChatColor.RED + Language.NOT_ENOUGH_ARGUMENTS.toString() + " " + Language.TRY + " " + ChatColor.GRAY + "/" + label + " help" + ChatColor.RED + ".");
                        return true;
                    }
                    AuthPlayer ap = AuthPlayer.getAuthPlayer(args[1]);
                    if (ap == null) {
                        cs.sendMessage(ChatColor.RED + Language.ERROR_OCCURRED.toString());
                        return true;
                    }
                    final String name = AmkAUtils.forceGetName(ap.getUniqueId());
                    if (!ap.isRegistered()) {
                        cs.sendMessage(ChatColor.RED + Language.PLAYER_NOT_REGISTERED.toString());
                        return true;
                    }
                    ap.setVIP(false);
                    cs.sendMessage(ChatColor.BLUE + String.format(Language.NLP_SET_UPDATED.toString(), ChatColor.GRAY + name + ChatColor.BLUE));
                    break;
                }
                
                case "ipcount": {
                    if (Config.maxUsersPerIpaddress==0) {
                    	this.plugin.getLogger().info("Maximum allowed registered player counting from one Ip-Address is disabled.");            		
                	}
                	else
                	{
                		int Count = PConfManager.getIpaddressCount();
                		cs.sendMessage(ChatColor.BLUE + "A total of: " + Count + " Registered Ip-Adresses found " );
                		break;
                    }
                }
                case "iplist": {
                    if (Config.maxUsersPerIpaddress==0) {
                    	this.plugin.getLogger().info("Maximum allowed registered player counting from one Ip-Address is disabled.");            		
                	}
                	else
                	{
                		for(int i=0; i<PConfManager.getIpaddressCount();i++) {
                        cs.sendMessage(ChatColor.BLUE + "Registered player-count from Ip-Address: " + PConfManager.listIpaddressesInfo(i) );
                		}
                		break;
                	}
                }
                
                case "reload": {
                    //this.plugin.c.reloadConfiguration();
                    Bukkit.getPluginManager().getPlugin("AmkMcAuth").reloadConfig();
                    cs.sendMessage(ChatColor.BLUE + Language.CONFIGURATION_RELOADED.toString());
                    break;
                }
                default: {
                    cs.sendMessage(ChatColor.RED + Language.INVALID_SUBCOMMAND.toString() + " " + Language.TRY + " " + ChatColor.GRAY + "/" + label + " help" + ChatColor.RED + ".");
                    break;
                }
            }
            return true;
        }
        return false;
    }

}
