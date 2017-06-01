package fr.tmagnier.TheLastKing;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_11_R1.block.CraftChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import fr.tmagnier.TheLastKing.Objective.ObjectiveId;
import net.minecraft.server.v1_11_R1.TileEntityChest;

public class Team 
{
	private Game game;
	private String name;
	private LinkedHashMap<UUID, Boolean> players;
	private UUID king;
	private Area area;
	private TeamColor color;
	private boolean pvpActive;
	private boolean firstKingDead;
	
	public Location lockedChestLocation;
	
	private ArrayList<Objective> objectives;
	private Inventory objectivesInventory;
	
	enum TeamColor { BLUE, RED, GREEN, PURPLE };
	
	public Team(Game game, TeamColor color)
	{		
		this.game = game;
		this.players = new LinkedHashMap<UUID, Boolean>();
		this.color = color;
		this.pvpActive = false;
		this.firstKingDead = false;
		 
		int tailleZone = 300;
		switch(this.color)
		{
			case RED:
				this.name = "Rouge";
				Location locationSudOuest = new Location(Bukkit.getWorlds().get(0),-1000,0,1000);
				this.area = new Area(locationSudOuest, locationSudOuest.clone().add(tailleZone, 0, -tailleZone));
				break;
			case BLUE:
				this.name = "Bleu";
				Location locationNordEst = new Location(Bukkit.getWorlds().get(0), 1000, 0, -1000);
				this.area = new Area(locationNordEst, locationNordEst.clone().add(-tailleZone, 0, tailleZone));
				break;
			case GREEN:
				this.name = "Verte";
				Location locationSudEst = new Location(Bukkit.getWorlds().get(0), 1000, 0, 1000);
				this.area = new Area(locationSudEst, locationSudEst.clone().add(-tailleZone, 0, -tailleZone));
				break;
			case PURPLE:
				this.name = "Violette";
				Location locationNordOuest = new Location(Bukkit.getWorlds().get(0), -1000, 0, -1000);
				this.area = new Area(locationNordOuest, locationNordOuest.clone().add(tailleZone, 0, tailleZone));
				break;
			default:
				break;
		}	
		
		// Initialisation des objectifs
		this.objectives = new ArrayList<Objective>();
		this.objectives.add(new Objective(ObjectiveId.GHAST, Material.FIREBALL, "Retour à l'envoyeur", "Tuer un Ghast avec une fireball"));
		this.objectives.add(new Objective(ObjectiveId.BLAZE, Material.SNOW_BALL, "Bataille de boules de neige", "Tuer un Blaze avec une boule de neige"));
		this.objectives.add(new Objective(ObjectiveId.CREEPER, Material.ARROW, "Tir ami", "Tuer un Creeper avec un Squelette"));
		this.objectives.add(new Objective(ObjectiveId.JUKEBOX, Material.JUKEBOX, "Un peu de dubstep ?", "Crafter un jukebox et jouer un son"));
		this.objectives.add(new Objective(ObjectiveId.WITHER_SKELETON, Material.SKULL, "Wither head", "Récupérer une tête de Wither Squelette"));
		this.objectives.add(new Objective(ObjectiveId.TOTEM, Material.BEACON, "Un totem ? ça sert à quoi ?", "Faire un totem\n/totem pour plus d'informations"));
		initObjectivesInventory();
		
		generateChests(); // génération des coffres dans la zone d'apparition de l'équipe
		lockedChestLocation = area.getCenterLocation();
	}
	
	public void registerTo(Player player)
	{
		Scoreboard scoreboard = game.getScoreboards().get(player.getName());
		org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.registerNewTeam(name);
		scoreboardTeam.setPrefix(getChatColor()+"");
		scoreboardTeam.setSuffix(""+ChatColor.RESET);
		scoreboardTeam.setCanSeeFriendlyInvisibles(true);
		scoreboardTeam.setAllowFriendlyFire(false);
		for(OfflinePlayer p : this.getPlayers())
		{
			scoreboardTeam.addEntry(p.getName());
		}
		game.getScoreboards().put(player.getName(), scoreboard);
	}
	
	public void initObjectivesInventory()
	{
		Inventory inventory = Bukkit.createInventory(null, 9, "Objectifs");
		for(Objective objective : objectives)
		{
			ItemStack item = new ItemStack(objective.getMaterial());
			ItemMeta itemMeta = item.getItemMeta();

			itemMeta.setDisplayName(ChatColor.AQUA + objective.getName());
			ArrayList<String> lore = new ArrayList<String>();
			if(objective.isAccomplished())
			{
				lore.add(ChatColor.GREEN + "Objectif accompli");
			}
			else
			{
				lore.add(ChatColor.RED + "Objectif non accompli");
			}
			lore.add(ChatColor.GRAY + "Description :");
			lore.addAll(Arrays.asList((ChatColor.WHITE + objective.getDescription().replaceAll("\n","\n" + ChatColor.WHITE)).split("\n")));
			itemMeta.setLore(lore);
			item.setItemMeta(itemMeta);
			inventory.addItem(item);
		}
		ItemStack item = new ItemStack(Material.BARRIER);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setDisplayName(ChatColor.WHITE + "Fermer la fenêtre");
		item.setItemMeta(itemMeta);
		inventory.setItem(8, item);
		this.objectivesInventory = inventory;
	}
	
	public void updateObjectivesInventory()
	{
		List<HumanEntity> viewers = objectivesInventory.getViewers(); // On récupère la liste des viewers
		initObjectivesInventory(); // On met à jour l'inventaire
		Iterator<HumanEntity> iterator = viewers.iterator();
		while(iterator.hasNext())
		{
			HumanEntity player = iterator.next();
			iterator.remove();
			Main.sendDebugMessage("Actualisation de l'inventaire des objectifs pour " + player.getName());
			player.getOpenInventory().close();
			player.openInventory(objectivesInventory);
		}
	}
	
	public void generateChests()
	{
		Location[] locationCoffres = new Location[3];
		for(int i = 0; i < locationCoffres.length; i++)
		{
			locationCoffres[i] = area.getRandomLocation().add(0, 1, 0); // On ajoute 1 de hauteur pour le coffrage
			Location coffrage = locationCoffres[i].clone().add(-1, -1, -1);
			for(int y = 0; y < 3; y++)
			{
				for(int x = 0; x < 3; x++)
				{
					for(int z = 0; z < 3; z++)
					{
						if(!(x ==  1 && y == 1 && z == 1))
						{
							coffrage.clone().add(x, y, z).getBlock().setType(Material.OBSIDIAN);
						}
					}
				}
			}
		
			Main.sendDebugMessage("Coffre " + i + " : " + locationCoffres[i].getX() + " " + locationCoffres[i].getY() + " " + locationCoffres[i].getZ());
			locationCoffres[i].getBlock().setType(Material.CHEST);
			
			CraftChest chest = (CraftChest) locationCoffres[i].getBlock().getState();
			try
			{
			    Field inventoryField = chest.getClass().getDeclaredField("chest");
			    inventoryField.setAccessible(true);
			    TileEntityChest teChest = ((TileEntityChest) inventoryField.get(chest));
			    teChest.a(ChatColor.WHITE + "Coffre bonus " + (i+1));
			}
			catch (Exception e)
			{
			     e.printStackTrace();
			}
			
			ItemStack[] contenuCoffre = null;
			switch(i)
			{
				case 0:
					contenuCoffre = new ItemStack[2];
					// Un bloc de bedrock + 1 pioche diams silk touch.
					contenuCoffre[0] = new ItemStack(Material.BEDROCK, 1);
					contenuCoffre[1] = new ItemStack(Material.DIAMOND_PICKAXE, 1);
					ItemMeta piocheMeta = contenuCoffre[1].getItemMeta();
					piocheMeta.addEnchant(Enchantment.SILK_TOUCH, 1, false);
					piocheMeta.setDisplayName(ChatColor.AQUA + "Pioche spéciale");
					contenuCoffre[1].setItemMeta(piocheMeta);
					break;
				case 1:
					contenuCoffre = new ItemStack[4];
					// 2 golden apple + un arc Power 2, Fire, Infinity et une flèche
					contenuCoffre[0] = new ItemStack(Material.GOLDEN_APPLE, 1);
					contenuCoffre[1] = new ItemStack(Material.GOLDEN_APPLE, 1);
					contenuCoffre[2] = new ItemStack(Material.ARROW, 1);
					contenuCoffre[3] = new ItemStack(Material.BOW, 1);
					ItemMeta arcMeta = contenuCoffre[3].getItemMeta();
					arcMeta.addEnchant(Enchantment.ARROW_DAMAGE, 2, true);
					arcMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
					arcMeta.addEnchant(Enchantment.ARROW_FIRE, 1, true);
					arcMeta.setDisplayName(ChatColor.AQUA + "Arc amélioré");
					contenuCoffre[3].setItemMeta(arcMeta);
					break;
				case 2:
					contenuCoffre = new ItemStack[3];
					// 1 golden apple + une épée pierre Sharpness 10, knockback 2, Unbreakable
					// 1 potion personnalisée : splash, regen 2, fire resist, jump boost 2, slowness. 
					contenuCoffre[0] = new ItemStack(Material.GOLDEN_APPLE, 1);
					contenuCoffre[1] = new ItemStack(Material.STONE_SWORD, 1);
					ItemMeta epeeMeta = contenuCoffre[1].getItemMeta();
					epeeMeta.addEnchant(Enchantment.DAMAGE_ALL, 10, true);
					epeeMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
					epeeMeta.setUnbreakable(true);
					epeeMeta.setDisplayName(ChatColor.AQUA + "Épée incassable");
					contenuCoffre[1].setItemMeta(epeeMeta);
					contenuCoffre[2] = new ItemStack(Material.SPLASH_POTION);
					PotionMeta potionMeta = (PotionMeta) contenuCoffre[2].getItemMeta();
					potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, Main.TICKS_PER_SECOND * 30, 1), true);
					potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP, Main.TICKS_PER_SECOND * 60, 1), true);
					potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, Main.TICKS_PER_SECOND * 60, 0), true);
					potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Main.TICKS_PER_SECOND * 120, 0), true);
					potionMeta.setColor(Color.ORANGE);
					potionMeta.setDisplayName(ChatColor.AQUA + "Potion magique");
					contenuCoffre[2].setItemMeta(potionMeta);
					break;
				default:
					break;
			}
			
			for(int indexItem = 0; indexItem < contenuCoffre.length; indexItem++)
			{
				int slot;
				do 
				{
					slot = Utils.getRandomInteger(0, 27);
				} while (chest.getInventory().getItem(slot) != null && !chest.getInventory().getItem(slot).getType().equals(Material.AIR));
				
				chest.getInventory().setItem(slot, contenuCoffre[indexItem]);
			}
	
		}
		

		
		
	}


	public String getName()
	{
		return this.name;
	}
	
	public String getDisplayName()
	{
		return getChatColor() + this.name;
	}
	
	public ChatColor getChatColor()
	{
		ChatColor chatColor = null;
		if(this.pvpActive) // Si le PVP est activé alors la couleur est foncée
		{
			switch(this.color)
			{
				case RED:
					chatColor = ChatColor.DARK_RED;
					break;
				case BLUE:
					chatColor = ChatColor.DARK_BLUE;
					break;
				case GREEN:
					chatColor = ChatColor.DARK_GREEN;
					break;
				case PURPLE:
					chatColor = ChatColor.DARK_PURPLE;
					break;
				default:
					break;
			}
		}
		else
		{
			switch(this.color)
			{
				case RED:
					chatColor = ChatColor.RED;
					break;
				case BLUE:
					chatColor = ChatColor.BLUE;
					break;
				case GREEN:
					chatColor = ChatColor.GREEN;
					break;
				case PURPLE:
					chatColor = ChatColor.LIGHT_PURPLE;
					break;
				default:
					break;
			}
		}
		return chatColor;
		
	}
	
	public boolean hasKing()
	{
		return this.king != null;
	}
	
	public OfflinePlayer getKing()
	{
		if(hasKing())
		{
			OfflinePlayer player = Bukkit.getPlayer(king);
			if(player == null)
			{
				player = Bukkit.getOfflinePlayer(king);
			}
			return player;
		}
		return null;
	}
	
	public void setKing(OfflinePlayer player)
	{
		if(player != null)
			this.king = player.getUniqueId();
		else 
			this.king = null;
	}
	
	public boolean isKing(Player player)
	{
		return this.king.equals(player.getUniqueId());
	}
	
	public boolean hasFirstKingDead()
	{
		return this.firstKingDead;
	}
	
	public void setFirstKingDead(boolean firstKingDead)
	{
		this.firstKingDead = firstKingDead;
	}
	
	public boolean hasPvpActive()
	{
		return this.pvpActive;
	}
	
	public void setPvpActive(boolean pvpActive)
	{
		this.pvpActive = pvpActive;
		for(Map.Entry<String, Scoreboard> entry : game.getScoreboards().entrySet())
		{
			Scoreboard scoreboard = entry.getValue();
			org.bukkit.scoreboard.Team team = scoreboard.getTeam(name);
			if(team != null)
			{
				team.setPrefix(getChatColor()+"");
			}
			else
			{
				Main.sendDebugMessage("Equipe " + name + " not found for " + entry.getKey() + "'s scoreboard");
			}
			game.getScoreboards().put(entry.getKey(), scoreboard);
		}
	}
	
	public TeamColor getColor()
	{
		return this.color;
	}
	
	public OfflinePlayer getRandomPlayer()
	{
		if(this.getSize() > 0)
		{
			Random rand = new Random();
			int nbreAleatoire = 0 + rand.nextInt(this.players.size() - 0);
			return this.getPlayers().get(nbreAleatoire);
		}
		return null;
	}
	
	public void addPlayer(Player p)
	{
		this.players.put(p.getUniqueId(), false);
		for(Map.Entry<String, Scoreboard> entry : game.getScoreboards().entrySet())
		{
			Scoreboard scoreboard = entry.getValue();
			org.bukkit.scoreboard.Team team = scoreboard.getTeam(name);
			if(team != null)
			{
				team.addEntry(p.getName());
			}
			else
			{
				System.out.println("Equipe " + name + " not found for " + entry.getKey() + "'s scoreboard");
			}
			game.getScoreboards().put(entry.getKey(), scoreboard);
		}
	}

	public void removePlayer(Player p)
	{
		this.players.remove(p.getUniqueId());
		for(Map.Entry<String, Scoreboard> entry : game.getScoreboards().entrySet())
		{
			Scoreboard scoreboard = entry.getValue();
			org.bukkit.scoreboard.Team team = scoreboard.getTeam(name);
			if(team != null)
			{
				team.removeEntry(p.getName());
			}
			else
			{
				System.out.println("Equipe " + name + " not found for " + entry.getKey() + "'s scoreboard");
			}
			game.getScoreboards().put(entry.getKey(), scoreboard);
		}
	}
	
	public void setDead(Player player)
	{
		players.put(player.getUniqueId(), true);
	}
	
	public boolean isDead(OfflinePlayer player)
	{
		return players.get(player.getUniqueId()).booleanValue();
	}
	
	public boolean isDead(UUID uuid)
	{
		return players.get(uuid).booleanValue();	
	}
	
	public void setArea(Area area)
	{
		this.area = area;
	}
	
	public Area getArea()
	{
		return this.area;
	}
	
	public boolean isMember(Player player)
	{
		return players.containsKey(player.getUniqueId());
	}
	
	public int getSize(boolean aliveOnly)
	{
		return getPlayers(aliveOnly).size();
	}
	
	public int getSize()
	{
		return getPlayers(true).size();
	}
	
	public ArrayList<OfflinePlayer> getPlayers(boolean aliveOnly)
	{
		ArrayList<OfflinePlayer> players = new ArrayList<OfflinePlayer>();
		for(UUID uuid : this.players.keySet())
		{
			if(!aliveOnly || !isDead(uuid))
			{
				OfflinePlayer player = Bukkit.getPlayer(uuid);
				if(player == null)
				{
					player = Bukkit.getOfflinePlayer(uuid);
				}
				players.add(player);
			}
		}
		return players;
	}
	
	public ArrayList<OfflinePlayer> getPlayers()
	{
		return getPlayers(true);
	}
	
	public ArrayList<Objective> getObjectives()
	{
		return this.objectives;
	}
	
	public ArrayList<Objective> getAccomplishedObjectives() 
	{
		ArrayList<Objective> accomplishedObjectives = new ArrayList<Objective>();
		for(Objective objective : objectives)
		{
			if(objective.isAccomplished())
			{
				accomplishedObjectives.add(objective);
			}
		}
		return accomplishedObjectives;
	}

	public Inventory getObjectivesInventory() 
	{
		return this.objectivesInventory;
	}
}
