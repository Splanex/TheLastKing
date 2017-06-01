package fr.tmagnier.TheLastKing;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Item;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_11_R1.block.CraftChest;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import fr.tmagnier.TheLastKing.Objective.ObjectiveId;
import fr.tmagnier.TheLastKing.Team.TeamColor;
import fr.tmagnier.TheLastKing.events.CompleteObjectiveEvent;
import fr.tmagnier.TheLastKing.events.PickEvent;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.NBTTagInt;
import net.minecraft.server.v1_11_R1.NBTTagList;
import net.minecraft.server.v1_11_R1.NBTTagString;
import net.minecraft.server.v1_11_R1.TileEntityChest;

public class Game implements Listener
{
	private boolean running;
	private ArrayList<Team> teams;
	private int episode;
	private int episodeLength;
	private int minutesLeft;
	private int secondsLeft;
	private boolean damageActived;
	private boolean canMove;
	private HashMap<String, Scoreboard> scoreboards;
	private ArrayList<LockedChest> lockedChests;
	private DecimalFormat df;
	private ArrayList<Material> matsTotem;
	private Location[] crystalLocations;
	
	public Game()
	{
		this.running = false;
		this.canMove = true;
		this.teams = new ArrayList<Team>();
		this.episodeLength = 20;
		this.scoreboards = new HashMap<String, Scoreboard>();
		this.damageActived = false;
		this.lockedChests = new ArrayList<LockedChest>();
		this.df = new DecimalFormat("00");
		
		// Initialisation des emplacements pour les cristaux
		this.crystalLocations = new Location[4];
		crystalLocations[0] = new Location(Bukkit.getWorlds().get(0), 1, 64, 0);
		crystalLocations[1] = new Location(Bukkit.getWorlds().get(0), 0, 64, 1);
		crystalLocations[2] = new Location(Bukkit.getWorlds().get(0), -1, 64, 0);
		crystalLocations[3] = new Location(Bukkit.getWorlds().get(0), 0, 64, -1);
		
		// Initialisation des matériaux requis pour le totem
		matsTotem = new ArrayList<Material>();
		matsTotem.add(Material.DIAMOND_BLOCK);
		matsTotem.add(Material.BONE_BLOCK);
		matsTotem.add(Material.NETHER_WART_BLOCK);
		matsTotem.add(Material.OBSIDIAN);
		matsTotem.add(Material.HUGE_MUSHROOM_1);
		matsTotem.add(Material.HUGE_MUSHROOM_2);
		matsTotem.add(Material.BEDROCK);
		matsTotem.add(Material.SKULL);
	}
		
	public void start()
	{
		if(!isRunning())
		{
			setRunning(true);
			setDamageActived(false);
			Bukkit.broadcastMessage(Main.prefix + "Début du jeu" + ChatColor.RESET);
			
			Main.sendDebugMessage("Creation de l'equipe ROUGE");
			this.teams.add(new Team(this, TeamColor.RED));
			Main.sendDebugMessage("Creation de l'equipe BLEU");
			this.teams.add(new Team(this, TeamColor.BLUE));
			Main.sendDebugMessage("Creation de l'equipe VERTE");
			this.teams.add(new Team(this, TeamColor.GREEN));
			Main.sendDebugMessage("Creation de l'equipe VIOLETTE");
			this.teams.add(new Team(this, TeamColor.PURPLE));
			
			// Récupération de tous les joueurs en ligne et réinitialisation des joueurs pour le début du jeu
			ArrayList<Player> players = new ArrayList<Player>();
			for(Player player : Bukkit.getOnlinePlayers())
			{
				player.setHealth(20); 
				player.setFoodLevel(20);
				player.setSaturation(20);
				player.setExp(0);
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Main.TICKS_PER_SECOND * 5, 1));
				player.getInventory().clear();
				player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2, 1));
				player.getActivePotionEffects().clear();
				player.setGameMode(GameMode.SURVIVAL);
				player.closeInventory();
				player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(30.0D); // PVP 1.8
				player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
				players.add(player);
				player.damage(1);
				player.setInvulnerable(false);
				
				if(!getScoreboards().containsKey(player.getName()))
				{
					registerScoreboard(player);
				}
				
				// On enregistre les équipes aux joueurs
				for(Team team : teams)
				{
					team.registerTo(player);
				}
				
			}
			Bukkit.getServer().getWorlds().get(0).setTime(6000);
			Bukkit.getServer().getWorlds().get(0).setStorm(false);
			Bukkit.getServer().getWorlds().get(0).setThundering(false);
			// UltraHardcore
			Bukkit.getServer().getWorlds().get(0).setDifficulty(Difficulty.HARD);
			Bukkit.getServer().getWorlds().get(0).setGameRuleValue("naturalRegeneration", "false");
					
			// Constitution aléatoire des équipes
			while(players.size() > 0) // Tant qu'il y a des joueurs sans équipe
			{
				for(Team team : this.teams)
				{
					if(players.size() > 0) // S'il y a encore des joueurs sans équipe
					{
						int nbreAleatoire = Utils.getRandomInteger(0, players.size()); // Nombre aléatoire entre 0 et le nombre de joueurs sans équipe
						Player player = players.get(nbreAleatoire);
						AddPlayer(team, player); // On choisi un joueur aléatoirement et on l'ajoute à l'équipe actuelle
						player.teleport(team.getArea().getRandomLocation().add(0, 1, 0));
						players.remove(nbreAleatoire); // On enlève le joueur des joueurs sans équipe
					}
					else
					{
						break; // Condition d'arrêt
					}
				}
			}
			
			// Choix du roi pour chaque équipe
			for(Team team : this.teams)
			{
				if(team.getSize() > 0)
				{
					team.setKing(team.getRandomPlayer());	
					Main.sendDebugMessage("Le roi de l'equipe " + team.getName() + " est " + team.getKing().getName());
				}
				else
				{
					Main.sendDebugMessage("Aucun roi dans l'equipe " + team.getName());
				}
						
				
				
			}
			
			// Initialisation du timer
			this.episode = 1;
			this.secondsLeft = 0;
			this.minutesLeft = episodeLength;
			Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable(){

				@Override
				public void run()
				{
					secondsLeft--;
					if(secondsLeft == -1)
					{
						minutesLeft--;
						secondsLeft = 59;
					}
					if(minutesLeft == -1)
					{
						minutesLeft = episodeLength;
						secondsLeft = 0;
						Bukkit.getServer().broadcastMessage(Main.prefix + "Fin du jour " + episode);
						episode++;
					}

					// Mise à jour des scoreboards des joueurs
					for(Player player : Bukkit.getOnlinePlayers())
					{
						updateScoreboard(player);
					}
					
					// Listes des événements :
					// - Annonce des rois adverses
					if(episode == 1 && minutesLeft == 19 && secondsLeft == 45)
					{
						for(Team team : teams)
						{						
							for(OfflinePlayer player : team.getPlayers())
							{
								if(player.isOnline())
								{
									String message = "";
									for(Team t : teams)
									{
										if(t != team)
										{
											if(t.hasKing())
											{
												message += Main.prefix + "Le roi de l'équipe " + t.getDisplayName() + ChatColor.GOLD + " est " + t.getKing().getName()+"\n";
											}
											else
											{
												message += Main.prefix + "L'équipe " + t.getDisplayName() + ChatColor.GOLD + " n'a pas de roi\n";
											}
										}
									}
									((Player)player).sendMessage(message);
								}
							}
						}
					}
					// - Activation des dommages
					else if(episode == 1 && minutesLeft == 19 && secondsLeft == 55)
					{
						setDamageActived(true);
						for(Player player : Bukkit.getOnlinePlayers())
						{
							player.setInvulnerable(false);
						}
						Bukkit.broadcastMessage(Main.prefix + ChatColor.GOLD + "Vous êtes désormais " + ChatColor.RED + "vulnérable" + ChatColor.GOLD + " aux dégâts");
					}
					// - Activation du pvp pour toutes les équipes
					else if(episode == 5 && minutesLeft == 20 && secondsLeft == 0)
					{
						boolean allPvpActive = true;
						for(Team team : teams)
						{
							if(!team.hasPvpActive())
							{
								allPvpActive = false;
								team.setPvpActive(true);
							}
						}
						
						if(!allPvpActive)
						{
							Bukkit.broadcastMessage(Main.prefix + "Le Pvp est désormais " + ChatColor.RED + "activé " + ChatColor.GOLD + "pour toutes les équipes");
						}
					}
					// - Remise des cristaux aux rois
					else if(episode == 6 && minutesLeft == 20 && secondsLeft == 0)
					{
						for(Team team : teams)
						{
							if(team.hasKing())
							{
								if(team.getKing().isOnline())
								{
									Player king = (Player) team.getKing();
									king.getInventory().addItem(getCrystal(team));
									king.sendMessage(Main.prefix + "Étant le Roi, vous venez de recevoir le cristal de l'end de votre équipe, vous devez le protéger");
								}
							}
						}
					}
				}
				
			}, Main.TICKS_PER_SECOND, Main.TICKS_PER_SECOND);
		}
	}
	
	public void stop()
	{
		if(isRunning())
		{
			setRunning(false);	
			Bukkit.broadcastMessage(Main.prefix + "Fin du jeu");
			lockedChests.clear(); // On supprime les coffres verrouillés
			teams.clear(); // On supprime les équipes
			Bukkit.getScheduler().cancelAllTasks(); // On annule toutes les tâches en cours
			// Réinitialisation des joueurs
			getScoreboards().clear(); // On supprime les scoreboards
			for(Player player : Bukkit.getOnlinePlayers())
			{
					Scoreboard s = Bukkit.getScoreboardManager().getNewScoreboard();
					player.setScoreboard(s);
			}
		}
	}
	
	public void updateScoreboard(Player player)
	{
		Scoreboard scoreboard = getScoreboards().get(player.getName()); // On recupère le scoreboard du joueur
		if(scoreboard == null)
		{
			registerScoreboard(player);
		}
		Objective objSidebar = scoreboard.getObjective(DisplaySlot.SIDEBAR); // On récupère l'ancien objectif
		if(objSidebar != null)
		{
			objSidebar.unregister(); // On supprime l'ancien objectif s'il existe
		}
		objSidebar = scoreboard.registerNewObjective("sidebarObjective", "dummy");
		objSidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		objSidebar.setDisplayName(ChatColor.YELLOW +""+ ChatColor.BOLD + "TheLastKing");

		int nbPlayers = 0;
		int nbTotalPlayers = 0;
		int nbTeams = 0;
		for(Team team : teams)
		{
			if(team.getSize() > 0)
			{
				nbTeams++;
				nbPlayers += team.getSize();
			}					
			nbTotalPlayers += team.getSize(false);
		}
		objSidebar.getScore(" ").setScore(5);
		objSidebar.getScore(ChatColor.RED + "Jour "+episode+ " - "+df.format(minutesLeft)+":"+df.format(secondsLeft)).setScore(4);
		objSidebar.getScore("").setScore(3);
		objSidebar.getScore("Equipes : " + nbTeams + ChatColor.GRAY + " ("+nbPlayers+"/"+nbTotalPlayers+")").setScore(2);
		
		if(hasTeam(player))
		{
			Team team = getTeam(player);
			objSidebar.getScore(getTeam(player).getChatColor() + "Equipe " + getTeam(player).getName()).setScore(1);
			
			int i = 0;
			for(OfflinePlayer p : team.getPlayers(false))
			{
				if(p != player)
				{
					if(p.isOnline() && !team.isDead(p))
					{
						String fleches = "↑↗→↘↓↙←↖";
						int index = (int)Math.floor(getDirection(player, (Player)p) / 45.0);
						char fleche = fleches.charAt(index);							
						objSidebar.getScore(p.getName() + " " + ChatColor.AQUA + fleche + ChatColor.RESET + " " + getDistance((Player) p, player)).setScore(i--);
					}
					else if(team.isDead(p))
					{
						objSidebar.getScore(p.getName() + ChatColor.RED + " (mort)").setScore(i--);
					}
					else
					{
						objSidebar.getScore(p.getName() + ChatColor.GRAY + " (deco)").setScore(i--);
					}
				}
					
			}
		}
		getScoreboards().put(player.getName(), scoreboard); // On met à jour le scoreboard
		player.setScoreboard(scoreboard);
	}
	
	public void registerScoreboard(Player p) 
	{
		if(Main.debug) Main.sendDebugMessage("Initialisation du scoreboard de " + p.getName());
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard(); // On crée un nouveau scoreboard
		Objective objPlayerList = scoreboard.registerNewObjective("health2", "health"); // On crée l'objectif pour l'affichage des coeurs
		objPlayerList.setDisplaySlot(DisplaySlot.PLAYER_LIST); // On affiche les coeurs dans la liste des joueurs (TAB)
		Objective objBelowName = scoreboard.registerNewObjective("health", "health");
		objBelowName.setDisplayName(ChatColor.RED + "❤");
		objBelowName.setDisplaySlot(DisplaySlot.BELOW_NAME);
		getScoreboards().put(p.getName(), scoreboard); // On sauvegarde le scoreboard
		p.setScoreboard(scoreboard); // On affiche le scoreboard au joueur
	}

	@EventHandler
	public void onPlayerLeaveEvent(PlayerQuitEvent event)
	{
		Player p = event.getPlayer();
		if(isRunning() && getScoreboards().containsKey(p.getName()))
		{
			event.setQuitMessage(Main.prefix + event.getPlayer().getDisplayName() + ChatColor.GOLD + " vient de se déconnecter");
		}
		else
		{
			event.setQuitMessage(Main.prefix + ChatColor.WHITE + event.getPlayer().getName() + ChatColor.GOLD + " vient de se déconnecter");
		}
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		Main.initTabHeaderFooter(player);
		if(!getScoreboards().containsKey(player.getName())) // Si le joueur n'a pas de scoreboard
		{
			registerScoreboard(player); // On lui enregistre un scoreboard
		}
		else
		{
			player.setScoreboard(getScoreboards().get(player.getName())); // On recupère son scoreboard
		}
		
		if(isRunning()) // Si le jeu est en cours
		{
			if(hasTeam(player))
			{
				player.setInvulnerable(!damageActived); // On rend le joueur invinsible si le pvp n'est pas activé, ou inversement
				event.setJoinMessage(Main.prefix + event.getPlayer().getDisplayName() + ChatColor.GOLD + " vient de se reconnecter");
			}
			else
			{
				player.setGameMode(GameMode.SPECTATOR);
				event.setJoinMessage(Main.prefix + ChatColor.WHITE + event.getPlayer().getName() + ChatColor.GOLD + " regarde la partie");
				for(Team team : teams)
				{
					team.registerTo(player);
				}
			}
			
		}
		else
		{
			player.setInvulnerable(true);
			event.setJoinMessage(Main.prefix + ChatColor.WHITE + event.getPlayer().getName() + ChatColor.GOLD + " vient de se connecter");
		}
	}
	
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event)
	{
		if(!canMove)
		{
			event.setCancelled(true);
		}
		
		if(isRunning() && event.getTo().getWorld().getEnvironment().equals(Environment.NORMAL))
		{
			if(episode < 3)
			{
				if(hasTeam(event.getPlayer()) && !getTeam(event.getPlayer()).isDead(event.getPlayer()))
				{
					if(!getTeam(event.getPlayer()).getArea().isInside(event.getTo()) && getTeam(event.getPlayer()).getArea().isInside(event.getFrom()))
					{
						event.setCancelled(true);
						
						Vector velocity = event.getFrom().toVector().subtract(event.getTo().toVector()).multiply(3).setY(0.25);
						event.getPlayer().setVelocity(velocity);
						//event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(-1).setY(0.2));
						event.getPlayer().sendMessage(Main.prefix + "Vous devez rester dans votre zone jusqu'au jour 3");
					}
					else if(!getTeam(event.getPlayer()).getArea().isInside(event.getFrom()))
					{
						Main.sendDebugMessage(event.getPlayer().getName() + " n'est pas dans sa zone");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerPortalEvent(PlayerPortalEvent event)
	{
		if(hasTeam(event.getPlayer()))
		{
			if(event.getFrom().getWorld().getEnvironment().equals(Environment.NETHER))
			{
				event.useTravelAgent(true);
				event.getPortalTravelAgent().setCanCreatePortal(true);
				event.getPortalTravelAgent().setSearchRadius(100);
				event.getPortalTravelAgent().setCreationRadius(100);
				Location portal = null;
				portal = event.getPortalTravelAgent().findOrCreate(getTeam(event.getPlayer()).getArea().getCenterLocation());
				Main.sendDebugMessage("Center of research or creation : " + getTeam(event.getPlayer()).getArea().getCenterLocation().toString());
				if(portal == null)
				{
					Main.sendDebugMessage("Using TravelAgent: " + event.useTravelAgent());
					Main.sendDebugMessage("No Portal Found");
					Main.sendDebugMessage("Search Radius: " + event.getPortalTravelAgent().getSearchRadius());
					Main.sendDebugMessage("Creation Radius: " + event.getPortalTravelAgent().getCreationRadius());
		            Main.sendDebugMessage("Can Create Portal: " + event.getPortalTravelAgent().getCanCreatePortal());
				}
				else
		        {
		            Bukkit.broadcastMessage("Portail : " + portal.toString());
		            event.setTo(portal);
		        }
			}
		}
	}
	
	@EventHandler
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event)
	{
		if(!isRunning())
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onItemDespawnEvent(ItemDespawnEvent event)
	{
		if(event.getEntityType().equals(EntityType.DROPPED_ITEM))
		{
			if(event.getEntity().getItemStack().getType().equals(Material.END_CRYSTAL))
			{
				// event.setCancelled(true);
				Main.sendDebugMessage("Un end_crystal ne peut pas disparaitre : " + event.getEntity().getLocation().toString());
			}
		}
	}
	
	@EventHandler
	public void onBlockBurnEvent(BlockBurnEvent event)
	{
		if(event.getBlock().getType().equals(Material.CHEST))
		{
			if(getLockedChest(event.getBlock().getLocation()) != null) // S'il s'agit d'un coffre verrouillé
			{
				event.setCancelled(true);
				Main.sendDebugMessage("Impossible de brûler un coffre verrouillé");
			}
		}
	}
	
	@EventHandler
	public void onEntityCombustEvent(EntityCombustEvent event)
	{
		if(event.getEntityType().equals(EntityType.DROPPED_ITEM))
		{
			if(((Item)event.getEntity()).getItemStack().getType().equals(Material.END_CRYSTAL))
			{
				// event.setCancelled(true);
				Main.sendDebugMessage("Un " + Material.END_CRYSTAL.toString() +" vient de se faire bruler : " + event.getEntity().getLocation());
			}
		}
	}
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (!isRunning())
		{
			event.setCancelled(true);
			Block b = event.getBlock();
			Main.sendDebugMessage("Information du bloc :");
			Main.sendDebugMessage(b.getType().toString());
			Main.sendDebugMessage(b.toString());
			
		}
		else
		{
			if(event.getBlock().getType() == Material.CHEST) // Si c'est un coffre
			{
				if(getLockedChest(event.getBlock().getLocation()) != null)
				{
					event.setCancelled(true); // On ne peut pas détruire un coffre vérouillé
					event.getPlayer().sendMessage(Main.prefix + "Impossible de casser un coffre verrouillé");
				}
				else
				{
					if((((CraftChest)event.getBlock().getState()).getBlockInventory().getName()).startsWith(ChatColor.WHITE + "Coffre bonus")) // S'il s'agit d'un coffre bonus
					{
						if(!getTeam(event.getPlayer()).getArea().isInside(event.getBlock().getLocation())) // Si le coffre bonus n'est dans la zone de départ de la team du joueur
						{
							event.getPlayer().sendMessage(Main.prefix + "Impossible de casser un coffre bonus d'une autre équipe");
							event.setCancelled(true); // On annule l'événement
						}
					}
				}
			}
			else if(isTotem(event.getBlock()))
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage(Main.prefix + "Vous ne pouvez pas détruire le totem.");
			}
		}
	}
	
	@EventHandler
	void onCompleteObjectiveEvent(CompleteObjectiveEvent event)
	{
		Team team = getTeam(event.getPlayer());
		if(team == null) return; // Si le joueur n'a pas d'équipe, il n'a pas d'objectif à accomplir
		for(fr.tmagnier.TheLastKing.Objective objective : team.getObjectives())
		{
			if(objective.getId() == event.getObjectiveId())
			{
				if(!objective.isAccomplished())
				{
					objective.setAccomplished(true);
					team.updateObjectivesInventory();
					Bukkit.broadcastMessage(Main.prefix + "L'équipe " + team.getDisplayName() + ChatColor.GOLD + " a réalisé l'objectif " + ChatColor.AQUA + objective.getName());
					if(team.getAccomplishedObjectives().size() == 3) // Si l'équipe a accompli 3 objectifs, le pvp est activé
					{
						team.setPvpActive(true);
						Bukkit.broadcastMessage(Main.prefix + "Le Pvp de l'équipe " + team.getDisplayName() + ChatColor.GOLD + " est désormais activé");
					}
				}
			}
		}
	}
	
	@EventHandler 
	void onPlayerPickupItemEvent(PlayerPickupItemEvent event)
	{
		if(event.getItem().getItemStack().getType() == Material.SKULL_ITEM)
		{
			Short durability = event.getItem().getItemStack().getDurability();
			if(durability.compareTo(new Short((short) SkullType.WITHER.ordinal())) == 0)
			{
				Bukkit.getPluginManager().callEvent(new CompleteObjectiveEvent(ObjectiveId.WITHER_SKELETON, event.getPlayer()));	
			}	
		}		
	}
	
	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event)
	{
		if (!isRunning())
		{
			event.setCancelled(true);
		}
		else
		{
			if(event.getBlockPlaced().getType() == Material.CHEST) // Si c'est un coffre
			{
				if((event.getBlockPlaced().getLocation().clone().add(1,0,0).getBlock().getType() == Material.CHEST && getLockedChest(event.getBlockPlaced().getLocation().clone().add(1,0,0).getBlock().getLocation()) != null) || (event.getBlockPlaced().getLocation().clone().add(0,0,1).getBlock().getType() == Material.CHEST && getLockedChest(event.getBlockPlaced().getLocation().clone().add(0,0,1).getBlock().getLocation()) != null) || (event.getBlockPlaced().getLocation().clone().add(-1,0,0).getBlock().getType() == Material.CHEST && getLockedChest(event.getBlockPlaced().getLocation().clone().add(-1,0,0).getBlock().getLocation()) != null) || (event.getBlockPlaced().getLocation().clone().add(0,0,-1).getBlock().getType() == Material.CHEST && getLockedChest(event.getBlockPlaced().getLocation().clone().add(0,0,-1).getBlock().getLocation()) != null))
				{
					event.setCancelled(true); // On ne peut pas placer un coffre à côté d'un coffre verrouillé
					event.getPlayer().sendMessage(Main.prefix + "Impossible de poser un coffre à côté d'un coffre verrouillé");
				}
			}
		
			if(matsTotem.contains(event.getBlockPlaced().getType()))
			{
				Main.sendDebugMessage(event.getBlockPlaced().getType().toString() + " " + event.getBlockAgainst().getType().toString());
				// Si le block posé est une tête
				if(event.getBlockPlaced().getType().equals(Material.SKULL))
				{
					// On vérifie qu'il s'agit bien d'une tête de wither
					if(!((Skull)event.getBlock().getState()).getSkullType().equals(SkullType.WITHER))
					{
						return;
					}
				}
				
				if(isTotem(event.getBlockPlaced()))
				{
					Bukkit.getPluginManager().callEvent(new CompleteObjectiveEvent(ObjectiveId.TOTEM, event.getPlayer()));
				}
				
			}
		}
	}
	
	public ArrayList<Block> getTotemBlocksNextTo(ArrayList<Block> visitedBlocks)
	{
		ArrayList<Block> newBlocks = new ArrayList<Block>();
		Main.sendDebugMessage("--- getTitemBlocksNextTo( " + visitedBlocks.size() + " block(s) ) ---");
		for(Block b : visitedBlocks)
		{
			Block block = b.getLocation().clone().add(1, 0, 0).getBlock();
			if(matsTotem.contains(block.getType()) && !visitedBlocks.contains(block) && !newBlocks.contains(block))
			{
				Main.sendDebugMessage("- new block : " + block.getType().toString() + " " + block.getX() + " " + block.getY() + " " +  block.getZ());
				newBlocks.add(block);
			}
			block = b.getLocation().clone().add(-1, 0, 0).getBlock();
			if(matsTotem.contains(block.getType()) && !visitedBlocks.contains(block) && !newBlocks.contains(block))
			{
				Main.sendDebugMessage("- new block : " + block.getType().toString() + " " + block.getX() + " " + block.getY() + " " +  block.getZ());
				newBlocks.add(block);
			}
			block = b.getLocation().clone().add(0, 1, 0).getBlock();
			if(matsTotem.contains(block.getType()) && !visitedBlocks.contains(block) && !newBlocks.contains(block))
			{
				Main.sendDebugMessage("- new block : " + block.getType().toString() + " " + block.getX() + " " + block.getY() + " " +  block.getZ());
				newBlocks.add(block);
			}
			block = b.getLocation().clone().add(0, -1, 0).getBlock();
			if(matsTotem.contains(block.getType()) && !visitedBlocks.contains(block) && !newBlocks.contains(block))
			{
				Main.sendDebugMessage("- new block : " + block.getType().toString() + " " + block.getX() + " " + block.getY() + " " +  block.getZ());
				newBlocks.add(block);
			}
			block = b.getLocation().clone().add(0, 0, 1).getBlock();
			if(matsTotem.contains(block.getType()) && !visitedBlocks.contains(block) && !newBlocks.contains(block))
			{
				Main.sendDebugMessage("- new block : " + block.getType().toString() + " " + block.getX() + " " + block.getY() + " " +  block.getZ());
				newBlocks.add(block);
			}
			block = b.getLocation().clone().add(0, 0, -1).getBlock();
			if(matsTotem.contains(block.getType()) && !visitedBlocks.contains(block) && !newBlocks.contains(block))
			{
				Main.sendDebugMessage("- new block : " + block.getType().toString() + " " + block.getX() + " " + block.getY() + " " +  block.getZ());
				newBlocks.add(block);
			}
			Main.sendDebugMessage("--- return " + newBlocks.size() + " new blocks ---");
		}
		return newBlocks;
	}
	
	public boolean isTotem(Block block)
	{
		if(matsTotem.contains(block.getType()))
		{
			ArrayList<Block> visitedBlocks = new ArrayList<Block>(); 
			visitedBlocks.add(block);
			
			ArrayList<Block> newBlocks;
			do 
			{
				newBlocks = getTotemBlocksNextTo(visitedBlocks);
				visitedBlocks.addAll(newBlocks);
			} while(newBlocks.size() > 0); // Tant qu'on trouve des nouveaux blocs
		
			ArrayList<Material> totem = new ArrayList<Material>();
			int max = -1;
			Main.sendDebugMessage("Le totem est composé de :");
			for(Block b : visitedBlocks)
			{
				System.out.println("- " + b.getType().toString() + " " + b.getX() + " " + b.getY() + " " +  b.getZ());
				
				if(max < b.getY())
				{
					max = b.getY();
				}
				
				if(matsTotem.contains(b.getType()) && !totem.contains(b.getType()))
				{
					totem.add(b.getType());
				}
			}
			
			boolean headAtTop = false;
			for(Block b : visitedBlocks)
			{
				if(b.getType().equals(Material.SKULL))
				{
					if(((Skull)b.getState()).getSkullType().equals(SkullType.WITHER))
					{
						if(b.getY() == max)
						{
							
								new BukkitRunnable(){

									@Override
									public void run() {
										for(Player p : Bukkit.getOnlinePlayers())
										{p.spawnParticle(Particle.LAVA, b.getLocation(), 10);}
									}
								}.runTaskTimer(Main.getInstance(), 0, Main.TICKS_PER_SECOND * 1);
								
									
							
							
								
							
							headAtTop = true;
						}
					}
				}
			}
			
			if(totem.containsAll(matsTotem) && headAtTop)
			{
				Main.sendDebugMessage("Objectif totem!!");
				//for(Player p : Bukkit.getOnlinePlayers())
				//{
					//for(Block b : visitedBlocks)
					//{
					//		b.breakNaturally(null);
					//}
					
				//}
				
				return true;
			}
		
		}
		
		return false;
	}
	
	public void setAttackDamage(ItemStack item, int value)
	{
		net.minecraft.server.v1_11_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
		NBTTagList modifiers = new NBTTagList();
		NBTTagCompound damage = new NBTTagCompound();
		damage.set("AttributeName", new NBTTagString("generic.attackDamage"));
		damage.set("Name", new NBTTagString("generic.attackDamage"));
		damage.set("Amount", new NBTTagInt(value));
		damage.set("Operation", new NBTTagInt(0));
		damage.set("UUIDLeast", new NBTTagInt(894654));
		damage.set("UUIDMost", new NBTTagInt(2872));
		damage.set("Slot", new NBTTagString("mainhand"));
		modifiers.add(damage);
		compound.set("AttributeModifiers", modifiers);
		nmsStack.setTag(compound);
		item = CraftItemStack.asBukkitCopy(nmsStack);
	}

	protected static Object getHandle(Item item) {
        Object handle = null;
        try {
                Method handleMethod = item.getClass().getMethod("getHandle");
                handle = handleMethod.invoke(item);
        } catch (Throwable ex) {
                ex.printStackTrace();
        }
        return handle;
}

    public static void setInvulnerable(Item item) {
        try {
                Object handle = getHandle(item);
                Field invulnerableField = Entity.class.getDeclaredField("invulnerable");
                invulnerableField.setAccessible(true);
                invulnerableField.set(handle, true);
        } catch (Exception ex) {
                ex.printStackTrace();
        }
}
                
	public void onItemSpawnEvent(ItemSpawnEvent event)
	{
		if(event.getEntity().getItemStack().getType().equals(Material.END_CRYSTAL))
		{
			setInvulnerable(event.getEntity());
		}
	}
	
	@EventHandler
	public void onCraftItemEvent(CraftItemEvent event)
	{
		ItemStack item = event.getInventory().getResult();
		if(item.getType() == Material.WOOD_AXE || item.getType() == Material.STONE_AXE || item.getType() == Material.IRON_AXE || item.getType() == Material.GOLD_AXE || item.getType() == Material.DIAMOND_AXE)
		{
			if(event.isShiftClick())
			{
				event.setCancelled(true); // On doit crafter une hache une par une pour avoir les modifications des dommages sur toutes
				return;
			}
			
			switch(item.getType())
			{
				case WOOD_AXE:
					setAttackDamage(item, 3);
					break;
				case STONE_AXE:
					setAttackDamage(item, 4);
					break;
				case IRON_AXE:
					setAttackDamage(item, 5);
					break;
				case GOLD_AXE:
					setAttackDamage(item, 3);
					break;
				case DIAMOND_AXE:
					setAttackDamage(item, 6);
					break;
				default:
					break;
			}
			event.getInventory().setResult(item);
		}
	}
	
	@EventHandler
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event)
	{
		if(event.getItem().getType() == Material.END_CRYSTAL)
		{
			event.setCancelled(true); // On ne peut pas récupérer avec un entonnoir un end_crystal
		}
	}
	
	public boolean isRunning()
	{
		return this.running;
	}
	
	public void setRunning(boolean running) 
	{
		this.running = running;
	}
	
	public ArrayList<Team> getTeams()
	{
		return this.teams;
	}
	
	public int getEpisode() 
	{
		return episode;
	}

	public void setEpisode(int episode) 
	{
		this.episode = episode;
	}

	public int getMinutesLeft() 
	{
		return minutesLeft;
	}

	public void setMinutesLeft(int minutesLeft) 
	{
		this.minutesLeft = minutesLeft;
	}

	public int getSecondsLeft() 
	{
		return secondsLeft;
	}

	public void setSecondsLeft(int secondsLeft) 
	{
		this.secondsLeft = secondsLeft;
	}

	public void setDamageActived(boolean damageActived)
	{
		this.damageActived = damageActived;
	}
	
	public boolean isDamageActived()
	{
		return this.damageActived;
	}
	
	public void setCanMove(boolean canMove)
	{
		this.canMove = canMove;
	}
	
	public boolean canMove()
	{
		return this.canMove;
	}
	
	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent event)
	{
		if(event.getInventory().getName().equalsIgnoreCase("Objectifs"))
		{
			event.setCancelled(true); // On annule l'événement
			if(event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR)
			{
				switch(event.getCurrentItem().getType())
				{
					case BEACON:
						event.getWhoClicked().sendMessage("aide totem");
						break;
					case BARRIER:
						event.getWhoClicked().getOpenInventory().close();
					default:
						break;
				}
			}
		}
		else if(event.getInventory().getName().startsWith("Coffre de l'équipe"))
		{
			if(event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR)
			{
				if(event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE)
				{
					event.setCancelled(true); // On annule l'événement
				}
			}
		}
		
		if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
		{
			if(event.getInventory() != null && event.getInventory().getType().equals(InventoryType.BREWING))
			{
				if(event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.GLOWSTONE_DUST))
				{
					event.setCancelled(true);
				}
			}
		}
		else if(event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_SOME)
		{
			if(event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.BREWING))
			{
				if(event.getCursor() != null && event.getCursor().getType().equals(Material.GLOWSTONE_DUST))
				{
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryDragEvent(InventoryDragEvent event)
	{
		if(event.getInventory().getType().equals(InventoryType.BREWING))
		{
			if(event.getOldCursor() != null && event.getOldCursor().getType().equals(Material.GLOWSTONE_DUST))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event)
	{
		if(isRunning()) // Si le jeu est en cours
		{
			event.setCancelled(true);
	        Player sender = event.getPlayer();
	        Team team = getTeam(sender);
	        String msg = team.getChatColor() + sender.getName() + ": " + ChatColor.RESET + event.getMessage();
	        if(event.getMessage().charAt(0) == '!')
	        {
	        	Bukkit.broadcastMessage(ChatColor.GRAY + "(Global) " + ChatColor.RESET + msg.replaceFirst("!", ""));
	        }
	        else
	        {
	        	for(OfflinePlayer player :team.getPlayers())
	        	{
	        		if(player.isOnline())
	        		{
	        			((Player)player).sendMessage(ChatColor.GRAY + "(Equipe) " + ChatColor.RESET + msg);
	        		}
	        	}
	        }
		}
    }
	
	LockedChest getLockedChest(Location location)
	{
		for(LockedChest crochetableChest : this.getLockedChests())
		{
			if(location.equals(crochetableChest.chest.getLocation())) // Si c'est un coffre crochetable
			{
				if(Main.debug) Main.sendDebugMessage("Coffre crochetable trouve");
				return crochetableChest; // On retourne le coffre crochetable
			}
		}
		if(Main.debug) Main.sendDebugMessage("Coffre crochetable non trouve");
		return null; // Ce n'est pas un coffre crochetable
	}
	
	public ItemStack getCrystal(Team team)
	{
		ItemStack item = new ItemStack(Material.END_CRYSTAL, 1);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setDisplayName(ChatColor.AQUA + "Cristal de l'équipe " + team.getDisplayName());
		item.setItemMeta(itemMeta);
		return item;
	}
	
	void setLockedChest(Team team)
	{
		team.lockedChestLocation.getBlock().setType(Material.CHEST);
		
		CraftChest chest = (CraftChest) team.lockedChestLocation.getBlock().getState();
		try
		{
		    Field inventoryField = chest.getClass().getDeclaredField("chest");
		    inventoryField.setAccessible(true);
		    TileEntityChest teChest = ((TileEntityChest) inventoryField.get(chest));
		    teChest.a("Coffre de l'équipe " + team.getDisplayName());
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
				content[i] = getCrystal(team);
			}
			else // C'est du verre
			{
				byte color = 0;
				switch(team.getColor())
				{
					case BLUE:
						color = 11;
						break;
					case RED:
						color = 14;
						break;
					case GREEN:
						color = 13;
						break;
					case PURPLE:
						color = 10;
						break;
					default:
						break;
				}
				content[i] = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) color);
				ItemMeta itemMeta = content[i].getItemMeta();
				itemMeta.setDisplayName(team.getDisplayName());
				itemMeta.addEnchant(Enchantment.FIRE_ASPECT, 1, false);
				itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				content[i].setItemMeta(itemMeta);
			}
			chest.getBlockInventory().setItem(i, content[i]);
		}
		
		LockedChest crochetableChest = new LockedChest(chest, team);
		lockedChests.add(crochetableChest);
	}
	
	@EventHandler 
	void onPlayerInteractEvent(PlayerInteractEvent event)
	{
		if(isRunning()) // Si le jeu est en cours
		{
			Player player = event.getPlayer(); // On récupère le joueur qui a fait l'action
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK) // S'il a fait un clic droit sur un bloc
			{
				if(event.getClickedBlock().getType() == Material.CHEST) // Si le bloc est un coffre
				{
					Main.sendDebugMessage(player.getName() + " ouvre un coffre : " + event.getClickedBlock().getLocation().getX() + " "  + event.getClickedBlock().getLocation().getY() + " " + event.getClickedBlock().getLocation().getZ());
					LockedChest lockedChest = getLockedChest(event.getClickedBlock().getLocation()); // On récupère le coffre verrouillé
					if(lockedChest == null) // Si ce n'est pas un crochetableChest
					{
						Main.sendDebugMessage(((CraftChest)event.getClickedBlock().getState()).getBlockInventory().getName());
						if((((CraftChest)event.getClickedBlock().getState()).getBlockInventory().getName()).startsWith(ChatColor.WHITE + "Coffre bonus")) // Si c'est un coffre bonus
						{
							if(hasTeam(player)) // Si le joueur a une team
							{
								if(!getTeam(player).getArea().isInside(event.getClickedBlock().getLocation())) // Si le coffre bonus n'est dans la zone de départ de la team du joueur
								{
									player.sendMessage(Main.prefix + "Vous ne pouvez pas prendre le coffre bonus d'une autre équipe");
									event.setCancelled(true); // On annule l'événement
								}
							}
						}
					}
					else
					{
						if(hasTeam(player))
						{
							if(lockedChest.isLocked())
							{
								event.setCancelled(true);
								lockedChest.pick(player); // Le joueur crochète le coffre
							}
						}
						else
						{
							event.setCancelled(true);
						}
					}
				}
				else if(event.getClickedBlock().getType() == Material.JUKEBOX) // Si le bloc est un jukebox
				{
					if(hasTeam(player))
					{
						ItemStack item = event.getItem();
						if(item != null)
						{
							// Si l'item est un disque de musique
							if(item.getType().isRecord())
							{
								Jukebox jukebox =(Jukebox) event.getClickedBlock().getState();
								if(!jukebox.isPlaying()) // Le jukebox ne jouait pas de la musique c'est à dire qu'on vient de mettre un cd
								{
									Bukkit.getPluginManager().callEvent(new CompleteObjectiveEvent(ObjectiveId.JUKEBOX, event.getPlayer()));
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	@EventHandler
	void onPickEvent(PickEvent event)
	{
		Player player = event.getPlayer();
		if(episode >= 6)
		{
			Team team = getTeam(player);
			if(team.hasKing())
			{
				if(!team.isKing(player))
				{
					player.sendMessage(Main.prefix + "Uniquement les rois peuvent crocheter ce coffre");
					event.setCancelled(true);
					return;
				}
				else // Il s'agit du Roi
				{
					event.getLockedChest().setRequiredTime(LockedChest.DEFAULT_REQUIRED_TIME / 10);
				}
			}
			else if(episode >= 8) // Il s'agit d'un joueur d'une équipe n'ayant plus de Roi
			{
				event.getLockedChest().setRequiredTime(LockedChest.DEFAULT_REQUIRED_TIME * 2);
			}
			else
			{
				player.sendMessage(Main.prefix + "Sans Roi, vous devez attendre le jour 8 pour crocheter ce coffre");
				event.setCancelled(true);
				return;
				// Vous aurez la possibilité de crocheter le coffre uniquement à partir du jour 8
			}
		}
		else
		{
			event.setCancelled(true);
			player.sendMessage(Main.prefix + "Vous devez attendre le jour 6 pour crocheter ce coffre");
			return;
		}
		
		String message = Main.prefix + "";
		switch(event.getAction())
		{
			case START:
				message += "Le coffre verrouillé de l'équipe " + event.getLockedChest().getOwner().getDisplayName() + ChatColor.GOLD + " commence à être crocheter par " + getTeam(event.getPlayer()).getChatColor() + event.getPlayer().getDisplayName();
				break;
			case INTERRUPTED:
				message += "Le crochetage du coffre verrouillé de l'équipe " + event.getLockedChest().getOwner().getDisplayName() + ChatColor.GOLD + " a été interrompu";
				break;
			case COMPLETED:
				message += "Le coffre verrouillé de l'équipe " + event.getLockedChest().getOwner().getDisplayName() + ChatColor.GOLD + " a été déverrouillé par " + getTeam(event.getPlayer()).getChatColor() +  event.getPlayer().getDisplayName();
				break;
			default:
				break;
		}
		Bukkit.broadcastMessage(message);
		
	}
	
	@EventHandler 
	void onPlayerDeath(PlayerDeathEvent event)
	{
		if(isRunning())
		{
			Player player = (Player) event.getEntity();
			if(player.getKiller() instanceof Player)
			{
				event.setDeathMessage(Main.prefix + getTeam(player).getChatColor() + player.getName() + ChatColor.GOLD + " s'est fait tuer par " + getTeam(player.getKiller()).getChatColor() + player.getKiller().getName());
			}
			for(Player p : Bukkit.getOnlinePlayers())
			{
				p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
			}
			ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)SkullType.PLAYER.ordinal());
			SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
			skullMeta.setOwner(player.getName());
			skullMeta.setDisplayName(getTeam(player).getChatColor()+player.getName());
			skull.setItemMeta(skullMeta);
			player.getLocation().getWorld().dropItem(player.getLocation(), skull);
			// Retirer le joueur de l'équipe et si c'était le premier roi alors deuxieme roi sinon coffre
			
			Team team = getTeam(player);
			if(team.isKing(player))
			{
				Main.sendDebugMessage("Le roi " + player.getName() + " de l'equipe " + team.getName() + " s'est fait tuer");
				if(!team.hasFirstKingDead() && episode < 6)
				{
					if(team.getSize() - 1 == 0) // S'il n'y a plus de joueurs vivants dans l'équipe
					{
						for(Team t : teams)
						{
							for(OfflinePlayer p : t.getPlayers())
							{
								if(p.isOnline())
								{
									if(t.equals(team))
									{
										((Player)p).sendMessage(Main.prefix + "Vous étiez le Roi et dernier survivant de votre équipe");
									}
									else
									{
										((Player)p).sendMessage("Le Roi " + team.getKing().getName() + ChatColor.GOLD + ", dernier survivant de l'équipe " + team.getDisplayName() + ChatColor.GOLD + " est mort, leur coffre verrouillé vient d'apparaitre !");
									}
								}
							}
							
						}
						team.setKing(null); // Il n'y a plus de roi, d'ailleurs l'équipe a perdu lol
						// On crée le coffre crochetable:
						setLockedChest(team);
					}
					else
					{
						for(Team t : teams)
						{
							for(OfflinePlayer p : t.getPlayers())
							{
								if(p.isOnline())
								{
									if(t.equals(team))
									{
										((Player)p).sendMessage(Main.prefix + "Votre Roi " + team.getKing().getName() + ChatColor.GOLD + " est mort, un nouveau Roi est désigné !");
									}
									else
									{
										((Player)p).sendMessage("Le Roi " + team.getKing().getName() + ChatColor.GOLD + " de l'équipe " + team.getDisplayName() + ChatColor.GOLD + " est mort, un nouveau Roi est désigné !");
									}
								}
							}
							
						}
						team.setFirstKingDead(true); // Le premier roi est mort
						team.setKing(team.getRandomPlayer()); // On choisit un autre roi
						Main.sendDebugMessage("Le nouveau Roi de l'equipe " + team.getName() + " est " + team.getKing().getName());
					}
				}
				else if(team.hasFirstKingDead() || episode >= 6)
				{
					for(Team t : teams)
					{
						for(OfflinePlayer p : t.getPlayers())
						{
							if(p.isOnline())
							{
								if(t.equals(team))
								{
									((Player)p).sendMessage(Main.prefix + "Votre nouveau Roi " + team.getKing().getName() + ChatColor.GOLD + " est mort, votre coffre verrouillé vient d'apparaitre !");
								}
								else
								{
									((Player)p).sendMessage("Le Roi " + team.getKing().getName() + ChatColor.GOLD + " de l'équipe " + team.getDisplayName() + ChatColor.GOLD + " est mort, leur coffre verrouillé vient d'apparaitre !");
								}
							}
						}
					}
					for(ItemStack item : player.getInventory().getContents())
					{
						if(item != null && item.getType().equals(Material.END_CRYSTAL)
								&& item.getItemMeta().getDisplayName().endsWith(team.getName()))
						{
							Main.sendDebugMessage("Le joueur possédait le cristal de son équipe");
							player.getInventory().remove(item); // On retire l'item
						}
					}
					team.setKing(null); // Le deuxième et dernier roi est mort
					
					setLockedChest(team); // On crée le coffre crochetable
				}
			}
			team.removePlayer(player);
			team.setDead(player);
			player.setGameMode(GameMode.SPECTATOR);
		}
	}
	
	@EventHandler
	public void onWeatherChangeEvent(WeatherChangeEvent event)
	{
		if(Main.debug) Main.sendDebugMessage("Annulation changement de la meteo.");
		event.setCancelled(true);
	}
	
	@EventHandler
	void onServerListPingEvent(ServerListPingEvent event)
	{
		event.setMotd("\u00a76Bienvenue sur \u00a7eTheLastKing \u00a76version " + Main.getInstance().getDescription().getVersion());
	}
		
	@EventHandler 
	void onEntityDamageEvent(EntityDamageEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			if(event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)event).getDamager() instanceof Player)
			{			System.out.println(event.getDamage());
				Player player = (Player) event.getEntity();
				Player damager = (Player) ((EntityDamageByEntityEvent)event).getDamager();
				if(!(getTeam(player).hasPvpActive() && getTeam(damager).hasPvpActive()))
				{
					event.setCancelled(true);
				}			
			}
		}
		else if(event.getEntityType().equals(EntityType.ENDER_CRYSTAL))
		{
			event.setCancelled(true);
		}
		else if(event.getEntityType().equals(EntityType.DROPPED_ITEM))
		{
			if(((Item)event.getEntity()).getItemStack().getType().equals(Material.END_CRYSTAL))
				
				Main.sendDebugMessage("DEGAT SUR DROPPED ITEM END CRYSTAL" + ((Item)event.getEntity()).getCustomName());
				event.getEntity().remove();
		}
	}
	
	@EventHandler 
	void onEntityDeathEvent(EntityDeathEvent event)
	{
		if(event.getEntity() instanceof WitherSkeleton)
		{				 
			boolean skull = false;
			for(ItemStack itemStack : event.getDrops())
			{
				if(itemStack.getType() == Material.SKULL_ITEM)
				{
					skull = true;
				}
			}
			if(!skull) // On a déjà eu 2.5% chance d'avoir la tête de wither ça n'a pas suffit
			{
				double nbreAleatoire = 100 * Math.random();
				double luck = (2.5 * 4); // 2.5% de chance de drop tête de wither sans Looting et 3.5% avec Looting I, 4.5% avec Looting II et 5.5% avec Looting III
				for(Map.Entry<Enchantment, Integer> entry : event.getEntity().getKiller().getInventory().getItemInMainHand().getEnchantments().entrySet())
				{
					if(entry.getKey().getName() == "LOOT_BONUS_MOBS")
					{
						luck += entry.getValue() * 4;
					}
				}
				Main.sendDebugMessage("Wither tue! luck:"+luck+", rand:"+nbreAleatoire);
				if(nbreAleatoire < luck)
				{						
					event.getDrops().add(new ItemStack(Material.SKULL_ITEM, 1, (short)SkullType.WITHER.ordinal())); // On drop une tête
				}
				
			}
		}
	}
	
	@EventHandler
	public void onBrewEvent(BrewEvent event)
	{
		ItemStack itemStack = event.getContents().getIngredient();
		if(itemStack.getType() == Material.GLOWSTONE_DUST)
		{
			for(HumanEntity player : event.getContents().getViewers())
			{
				player.sendMessage(Main.prefix + "Les potions de niveau II sont "+ChatColor.RED+"désactivées" + ChatColor.RESET);
			}
			Main.sendDebugMessage("Les potions de niveau II sont desactivees.");
			itemStack.setAmount(itemStack.getAmount()-1);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Fireball)
		{
			if(event.getEntity() instanceof Ghast)
			{
				Fireball fireball = (Fireball) event.getDamager();
				if(fireball.getShooter() instanceof Player)
				{
					Ghast ghast = (Ghast) event.getEntity();
					if((ghast.getHealth() - event.getDamage()) <= 0)
					{
						Bukkit.getPluginManager().callEvent(new CompleteObjectiveEvent(ObjectiveId.GHAST, (Player) fireball.getShooter()));
					}
				}
			}
			
		}
		else if(event.getDamager() instanceof Snowball)
		{
			if(event.getEntity() instanceof Blaze)
			{
				Snowball snowball = (Snowball) event.getDamager();
				if(snowball.getShooter() instanceof Player)
				{
					Blaze blaze = (Blaze) event.getEntity();
					if((blaze.getHealth() - event.getDamage()) <= 0) // S'il est mort
					{
						Bukkit.getPluginManager().callEvent(new CompleteObjectiveEvent(ObjectiveId.BLAZE, (Player) snowball.getShooter()));
					}
					
				}
			}
		}
		else if(event.getDamager() instanceof Arrow)
		{
			if(event.getEntity() instanceof Creeper)
			{
				Arrow arrow = (Arrow) event.getDamager();
				if(arrow.getShooter() instanceof Skeleton)
				{
					Creeper creeper = (Creeper) event.getEntity();
					if((creeper.getHealth() - event.getDamage()) <= 0) // S'il est mort
					{
						Skeleton skeleton = (Skeleton) arrow.getShooter();
						if(skeleton.getTarget() instanceof Player)
						{
							Bukkit.getPluginManager().callEvent(new CompleteObjectiveEvent(ObjectiveId.CREEPER, (Player) skeleton.getTarget()));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Returns the color's team
	 * <p>See TeamColor for the possible colors</p>
	 * @param color
	 * @return null if no team found, otherwise the color's team
	 */
	public Team getTeam(TeamColor teamColor) 
	{
		for(Team team : this.teams)
		{
			if(team.getColor().equals(teamColor))
			{
				return team;
			}
		}
		return null;
	}
	
	/**
	 * Returns the player's team
	 * @param player
	 * @return null if the player doesn't have a team, otherwise his team
	 */
	public Team getTeam(Player player)
	{
		for(Team team : this.teams)
		{
			if(team.isMember(player))
			{
				return team;
			}
		}
		return null;
	}
	
	/**
	 * Returns if the player has a team 
	 * @param player
	 * @return true if the player has a team, otherwise false
	 */
	public boolean hasTeam(Player player)
	{
		return getTeam(player) != null;
	}
	
	
	public void removeTeam(Player player)
	{
		Team team = getTeam(player); // On récupère la team du joueur
		team.removePlayer(player); // On le retire de la team
		Main.sendDebugMessage(player.getName() + " a ete retire de la team " + team.getColor().toString());
	}
	
	public void AddPlayer(Team team, Player player)
	{
		if(hasTeam(player)) removeTeam(player); // On retire le joueur de son ancienne team s'il en a une (normalement non)
		team.addPlayer(player); // On ajoute le joueur à la team
		Main.sendDebugMessage(player.getName() + " a ete ajoute dans la team " + team.getColor().toString());
	}
	
	private int getDistance(Player player1, Player player2)
	{
		Location player1Location = player1.getLocation();
		player1Location.setY(0);
		Location player2Location = player2.getLocation();
		player2Location.setY(0);
		
		return (int) Math.ceil(player1Location.distance(player2Location));
	}
	
	private double getDirection(Player player, Player target)
	{
		Location playerLocation = player.getLocation();
		playerLocation.setY(0); // On a pas besoin de l'axe Y
		Location targetLocation = target.getLocation();
		targetLocation.setY(0);
		
		// on récupère la direction de la tête du player
		Vector playerDirection = playerLocation.getDirection();
		// on récupère la direction du point par rapport au player
		Vector vector = targetLocation.subtract(playerLocation).toVector().normalize();
		// on convertit le tout en un angle en degrés
		double a = Math.toDegrees(Math.atan2(playerDirection.getX(), playerDirection.getZ()));
		a -= Math.toDegrees(Math.atan2(vector.getX(), vector.getZ()));
		// on se décale de 22.5 degrés pour se caler sur les demi points cardinaux
		a = (int)(a + 22.5) % 360;
		// on  s'assure d'avoir un angle strictement positif
		if (a < 0)
			a += 360;
		
		return a;
	}

	public ArrayList<LockedChest> getLockedChests() 
	{
		return lockedChests;
	}


	public HashMap<String, Scoreboard> getScoreboards() 
	{
		return scoreboards;
	}
}