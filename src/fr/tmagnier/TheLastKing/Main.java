package fr.tmagnier.TheLastKing;

import java.lang.reflect.Field;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_11_R1.block.CraftChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.tmagnier.TheLastKing.Team.TeamColor;
import net.minecraft.server.v1_11_R1.TileEntityChest;

public class Main extends JavaPlugin {
	
	public static Plugin plugin;
	public final static String prefix = ChatColor.GOLD+"["+ChatColor.YELLOW+"TheLastKing"+ChatColor.GOLD+"] ";
	public final static int TICKS_PER_SECOND = 20;
	public static boolean debug = true;
	private Game game;
	
	public static void sendDebugMessage(String message)
	{
		if(debug)
		{
			getInstance().getLogger().info("[DEBUG] " + message);
		}	
	}
	
	public static Plugin getInstance()
	{
		return plugin;
	}
	
	public void onEnable()
	{
		Main.plugin = this;
		Main.sendDebugMessage("Modification de la recette de craft de la table d'enchantement");
		Main.sendDebugMessage("Suppressionn de la recette de craft du cristal de l'end");
		Iterator<Recipe> recipes = Bukkit.recipeIterator();
        Recipe r;
        while (recipes.hasNext()) 
        {
            r = recipes.next();
            if (r != null && (r.getResult().getType().equals(Material.ENCHANTMENT_TABLE) || r.getResult().getType().equals(Material.END_CRYSTAL)))
                recipes.remove();
        }
        ShapedRecipe sr = new ShapedRecipe(new ItemStack(Material.ENCHANTMENT_TABLE));
        sr.shape("xbx", "eoe", "ooo");
        sr.setIngredient('b', Material.BOOK);
        sr.setIngredient('e', Material.EYE_OF_ENDER);
        sr.setIngredient('o', Material.OBSIDIAN);
        Bukkit.addRecipe(sr);
         
        Main.sendDebugMessage("Initialisation des limites de la carte");
        WorldBorder wb = getServer().getWorld("world").getWorldBorder();
        wb.setCenter(0, 0);
        wb.setSize(2000);
        wb.setWarningDistance(10);
         
        Main.sendDebugMessage("Initialisation du jeu");
        game = new Game();
         
        Bukkit.getPluginManager().registerEvents(game, this);
        
        for(Player player : Bukkit.getOnlinePlayers())
        {
        	initTabHeaderFooter(player);
        }
        
        getLogger().info("TheLastKing loaded.");
         
	}
	
	public static void initTabHeaderFooter(Player player)
	{
		Utils.sendTabHeaderFooter(player, ChatColor.YELLOW +""+ ChatColor.BOLD + "TheLastKing", ChatColor.GRAY + "version " + Main.getInstance().getDescription().getVersion());
	}
	
	public void onDisable()
	{
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("Vous n'avez pas la permission : vous devez être un joueur pour executer cette commande");
			return false;
		}
		
		if(cmd.getName().equalsIgnoreCase("lastking"))
		{
			if(args.length == 0)
			{
				
				//OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString("4979cf64-410d-3829-a48a-0e18bb27241c"));
				//System.out.println(op.getName());
				//((Player) op.sendMessage(Main.prefix + "/" + cmd.getName() + " help, pour plus d'informations");			
			}
			else if(args.length == 1)
			{
				if(args[0].equalsIgnoreCase("start"))
				{ 
					if(!game.isRunning())
					{
						this.game.start();
					}
					else
					{
						sender.sendMessage(Main.prefix + "Jeu déjà en cours");
					}
				}
				else if(args[0].equalsIgnoreCase("stop"))
				{
					if(game.isRunning())
					{
						game.stop(); 
					}
					else
					{
						sender.sendMessage(Main.prefix + "Aucun jeu en cours");
					}
					
				}
				else if(args[0].equalsIgnoreCase("status"))
				{
					if(game.isRunning())
					{
						int k = 0;
						for(Team team : game.getTeams())
						{
							k += team.getSize();
						}
						sender.sendMessage(Main.prefix + "Jeu en cours : " + k + " joueur(s) restant(s).");
					}
					else
					{
						sender.sendMessage(Main.prefix + "Aucun jeu en cours");
					}
					
				}
				else if(args[0].equalsIgnoreCase("cc"))
				{
					System.out.println("Coffres crochetables:");
					for(LockedChest cc : game.getLockedChests())
					{
						cc.setLocked(true);
						System.out.println("- " + cc.chest.getLocation().getX() + " " + cc.chest.getLocation().getY() + " " + cc.chest.getLocation().getZ());
					}
				}
				else if(args[0].equalsIgnoreCase("chest"))
				{
					((Player)sender).getLocation().getBlock().setType(Material.CHEST);
					CraftChest chest = (CraftChest) ((Player)sender).getLocation().getBlock().getState();
					try
					{
					    Field inventoryField = chest.getClass().getDeclaredField("chest");
					    inventoryField.setAccessible(true);
					    TileEntityChest teChest = ((TileEntityChest) inventoryField.get(chest));
					    teChest.a("Coffre de l'équipe " + sender.getName());
					}
					catch (Exception e)
					{
					     e.printStackTrace();
					}
					ItemStack[] content = new ItemStack[27];
					for(int i = 0; i < 27; i++)
					{
						if(i == 13) // C'est le cristal
						{
							content[i] = game.getCrystal(game.getTeam(TeamColor.GREEN));
						}
						else // C'est du verre
						{
							byte color = 13;
							content[i] = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) color);
							ItemMeta itemMeta = content[i].getItemMeta();
							itemMeta.setDisplayName(game.getTeam(TeamColor.GREEN).getDisplayName());
							itemMeta.addEnchant(Enchantment.FIRE_ASPECT, 1, false);
							itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
							content[i].setItemMeta(itemMeta);
						}
						chest.getBlockInventory().setItem(i, content[i]);
					}
					
					LockedChest crochetableChest = new LockedChest(chest, game.getTeam(TeamColor.GREEN));
					game.getLockedChests().add(crochetableChest);
				}
				else if(args[0].equalsIgnoreCase("help"))
				{
					sender.sendMessage(Main.prefix + "/" + cmd.getName() + " start, pour lancer la partie");
					sender.sendMessage(Main.prefix + "/" + cmd.getName() + " stop, pour arrêter la partie");
					sender.sendMessage(Main.prefix + "/" + cmd.getName() + " status, pour savoir si une partie est cours");
					sender.sendMessage(Main.prefix + "/" + cmd.getName() + " redteam|blueteam|greenteam|purpleteam add [Player], pour ajouter un joueur à une équipe");
				}
				else if(args[0].equalsIgnoreCase("test"))
				{
					new BukkitRunnable() {
						int i = 0;
						@Override
						public void run() 
						{
							Main.sendDebugMessage(String.valueOf(i));
							i++;
							if(i == 2)
							{
								((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_PLING, 5f, 2f);
							}
							else if(i == 4)
							{
								((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BASEDRUM, 5f, 2f);
							}
							else if(i == 6)
							{
								((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BASS, 5f, 2f);
							}
							else if(i == 8)
							{
								((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_HARP, 5f, 2f);
							}
							else if(i == 10)
							{
								((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_HAT, 5f, 2f);
							}
							else if(i == 10)
							{
								((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_SNARE, 5f, 2f);
							}
							else if(i == 15)
							{
								this.cancel();
							}
							
							
							
						}
					}.runTaskTimer(this, 0, TICKS_PER_SECOND * 2);
				}
				else if(args[0].equalsIgnoreCase("test2"))
				{
					Utils.sendActionBar((Player) sender, "test2");
					Utils.sendTitle((Player) sender, "titre", "sous-titre", 2, 3, 2);
				}
			}
			else if(args.length == 2)
			{
				Team team = null;
				if(args[0].equalsIgnoreCase("redteam"))
				{
					team = game.getTeam(TeamColor.RED);
				}
				else if(args[0].equalsIgnoreCase("blueteam"))
				{
					team = game.getTeam(TeamColor.BLUE);
				}
				else if(args[0].equalsIgnoreCase("greenteam"))
				{
					team = game.getTeam(TeamColor.GREEN);
				}
				else if(args[0].equalsIgnoreCase("purpleteam"))
				{
					team = game.getTeam(TeamColor.PURPLE);
				}
				else if(args[0].equalsIgnoreCase("episode"))
				{
					game.setEpisode(Integer.valueOf(args[1]));
					Bukkit.broadcastMessage("Jour " + game.getEpisode());
					game.setMinutesLeft(20);
					game.setSecondsLeft(1);
				}
				
				if(team != null)
				{
					if(args[1].equalsIgnoreCase("list"))
					{
						String msg = "Team " + team.toString() + " : [";
						boolean first = true;
						for(OfflinePlayer player : team.getPlayers())
						{
							if(first)
							{
								msg+= player.getName();
								first = false;
							}
							else
							{
								msg+= "," + player.getName();
							}
							
						}
						msg += "]";
						sender.sendMessage(msg);
					}
					else if(args[1].equalsIgnoreCase("pvp"))
					{
						
						Bukkit.broadcastMessage(Main.prefix + "Le Pvp est désormais " + ChatColor.RED + "activé" + ChatColor.GOLD + " pour l'équipe " + team.getDisplayName());
						team.setPvpActive(true);
					}
					else if(args[1].equalsIgnoreCase("nopvp"))
					{
						team.setPvpActive(false);
						Bukkit.broadcastMessage(Main.prefix + "Le Pvp est désormais " + ChatColor.RED + "désactivé" + ChatColor.GOLD + " pour l'équipe " + team.getDisplayName());
					}
				}
			}
			
			else if(args.length == 3)
			{
				Team team = null;
				if(args[0].equalsIgnoreCase("redteam"))
				{
					team = game.getTeam(TeamColor.RED);
				}
				else if(args[0].equalsIgnoreCase("blueteam"))
				{
					team = game.getTeam(TeamColor.BLUE);
				}
				else if(args[0].equalsIgnoreCase("greenteam"))
				{
					team = game.getTeam(TeamColor.GREEN);
				}
				else if(args[0].equalsIgnoreCase("purpleteam"))
				{
					team = game.getTeam(TeamColor.PURPLE);
				}
				
				if(team != null)
				{
					if(args[1].equalsIgnoreCase("add"))
					{
						sender.sendMessage(args[2] + " a été ajouté à l'équipe " + team.getColor().toString());
						game.AddPlayer(team, Bukkit.getPlayer(args[2]));
					}
					else if(args[1].equalsIgnoreCase("remove"))
					{
						sender.sendMessage(args[2] + " a été retiré de l'équipe " + team.getColor().toString());
					}
				}
			}
		}
		else if(cmd.getName().equalsIgnoreCase("objectifs"))
		{
			Team team = game.getTeam((Player)sender);
			((Player)sender).openInventory(team.getObjectivesInventory());
		}
		
		return false;
	}
}