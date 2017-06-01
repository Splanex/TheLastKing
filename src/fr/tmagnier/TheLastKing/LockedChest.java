package fr.tmagnier.TheLastKing;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.tmagnier.TheLastKing.events.PickEvent;
import fr.tmagnier.TheLastKing.events.PickEvent.Action;


public class LockedChest 
{
	public Chest chest;
	private Team owner;
	private long startTime;
	private long requiredTime;
	private boolean locked;
	private boolean inAttempt;
	private BukkitTask task;
	private static DecimalFormat df = new DecimalFormat("#00.00");
	private Player picker;
	public static final long DEFAULT_REQUIRED_TIME = 150 * 1000; 
	
	public LockedChest(Chest chest, Team owner) 
	{
		this.chest = chest;
		this.owner = owner;
		this.startTime = System.currentTimeMillis(); // timestamp du début
		this.requiredTime = 150 * 1000; // temps requis en millisecondes pour déverrouiller le coffre 150 secondes soit 2 minutes 30 secondes
		this.locked = true; // Savoir si le coffre est verrouillé ou non
		this.inAttempt = false; // Savoir si on a commencé le crochetage ou non
		this.picker = null;
	}
	
	private void startTask()
	{
		this.task = Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable(){
			@Override
			public void run() 
			{
				interruptPick();
			}
			
		}, Main.TICKS_PER_SECOND * 1);
	}
	
	private void restartTask()
	{
		task.cancel();
		startTask();
	}
	
	private void interruptPick()
	{
		PickEvent event = new PickEvent(Action.INTERRUPTED, this, picker);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled())
		{
			setInAttempt(false);
		}
	}
	
	private void startPick()
	{
		PickEvent event = new PickEvent(Action.START, this, picker);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled())
		{
			setInAttempt(true);
			this.startTime = System.currentTimeMillis();
			startTask();
		}		
	}
	
	public void completePick()
	{
		PickEvent event = new PickEvent(Action.COMPLETED, this, picker);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled())
		{
			this.task.cancel();
			this.locked = false;
		}
	}
	
	public void pick(Player picker)
	{
		this.picker = picker;
		if(isInAttempt()) // Si en cours de crochetage
		{
			restartTask(); 
			if((startTime + requiredTime) < System.currentTimeMillis()) // Si on a terminé de crocheter
			{
				completePick();
			}
		}
		else
		{
			startPick(); // On commence le crochetage
		}
		
		// On notifie le joueur
		if(isInAttempt())
		{
			Utils.sendActionBar(picker, this.getActionBarPercentage());
		}
		Main.sendDebugMessage("Avancement:" + this.getPercentage());
	}
	
	public void setInAttempt(boolean inAttempt)
	{
		this.inAttempt = inAttempt;
	}
	
	public boolean isInAttempt()
	{
		return this.inAttempt;
	}
	
	public Team getOwner() 
	{
		return owner;
	}
	
	public Player getPicker()
	{
		return this.picker;
	}
	
	public void setPicker(Player picker)
	{
		this.picker = picker;
	}
	
	public void setOwner(Team owner) 
	{
		this.owner = owner;
	}
	
	public long getStartTime() 
	{
		return startTime;
	}
	public void setStartTime(long startTime) 
	{
		this.startTime = startTime;
	}
	
	public double getPercentage()
	{
		double a = new Double(System.currentTimeMillis() - startTime);  
		double b = new Double(requiredTime);
		 	  
        double resultat = a/b;
        double resultatFinal = resultat*100;
        
        if(resultatFinal > 100)
        {
        	resultatFinal = new Double(100);
        }
	 
		Main.sendDebugMessage("Avancement crochetage: " + df.format(resultatFinal) + "% - " + (System.currentTimeMillis() - startTime) + "/" + requiredTime);
		return resultatFinal;
	}
	
	public String getActionBarPercentage()
	{
		double avancement = this.getPercentage();
		double pos = avancement / 5.0; // max = 100 / 5 = 20 
		String bar = ChatColor.GOLD + "[" + ChatColor.GREEN;
		String percentage = df.format(avancement)+"%";
		for(int i = 0; i < 20; i++)
		{
			if(i >= 7 && i <= 12)
			{
				if(i == 7)
				{
					bar += ChatColor.RESET;
				}
				bar += percentage.charAt(i-7);
				if(i == 12)
				{
					bar += ChatColor.GREEN;
				}
			}
			else
			{
				if(i < pos)
				{
					bar += "||";
				}
				else
				{
					bar += " ";
				}
			}
		}
		return bar + ChatColor.GOLD + "]";
	}
	
	public long getRequiredTime() 
	{
		return requiredTime;
	}
	
	public void setRequiredTime(long requiredTime)
	{
		this.requiredTime = requiredTime;
	}
	
	public boolean isLocked() 
	{
		return locked;
	}
	
	public void setLocked(boolean locked)
	{
		this.locked = locked;
	}
}
