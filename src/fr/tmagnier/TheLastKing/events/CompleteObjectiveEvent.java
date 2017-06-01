package fr.tmagnier.TheLastKing.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.tmagnier.TheLastKing.Objective.ObjectiveId;

public class CompleteObjectiveEvent extends Event
{

	public static final HandlerList handlers = new HandlerList();
	private ObjectiveId id;
	private Player player;
	
	public CompleteObjectiveEvent(ObjectiveId id, Player player)
	{
		this.id = id;
		this.player = player;
	}
	
	
	public static HandlerList getHandlerList() 
	{
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() 
	{
		return handlers;
	}
	
	public ObjectiveId getObjectiveId()
	{
		return this.id;
	}
	
	public void setObjectiveId(ObjectiveId id)
	{
		this.id = id;
	}
	
	public Player getPlayer()
	{
		return this.player;
	}
	
	public void setPlayer(Player player)
	{
		this.player = player;
	}
	
}
