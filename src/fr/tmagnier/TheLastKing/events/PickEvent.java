package fr.tmagnier.TheLastKing.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.tmagnier.TheLastKing.LockedChest;

public class PickEvent extends Event implements Cancellable {

	public static final HandlerList handlers = new HandlerList();
	
	public enum Action { START, INTERRUPTED, COMPLETED };
	private Action action;
	private LockedChest lockedChest;
	private Player player;
	private boolean cancelled;
	
	public PickEvent(Action action, LockedChest lockedChest, Player player)
	{
		this.action = action;
		this.lockedChest = lockedChest;
		this.player = player;
		this.cancelled = false;
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
	
	public PickEvent.Action getAction() 
	{
		return action;
	}

	public void setAction(Action action) 
	{
		this.action = action;
	}
	
	public LockedChest getLockedChest()
	{
		return this.lockedChest;
	}
	
	public void setLockedChest(LockedChest lockedChest)
	{
		this.lockedChest = lockedChest;
	}

	public Player getPlayer() 
	{
		return player;
	}

	public void setPlayer(Player player) 
	{
		this.player = player;
	}

	@Override
	public boolean isCancelled() 
	{
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) 
	{
		this.cancelled = cancelled;
	}

}
