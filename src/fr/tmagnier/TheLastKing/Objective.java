package fr.tmagnier.TheLastKing;

import org.bukkit.Material;

public class Objective 
{
	public enum ObjectiveId { GHAST, BLAZE, CREEPER, JUKEBOX, WITHER_SKELETON, TOTEM };
	
	private ObjectiveId id;
	private Material material;
	private String name;
	private String description;
	private boolean accomplished;
	
	public Objective(ObjectiveId id, Material material, String title, String description)
	{
		this.id = id;
		this.material = material;
		this.name = title;
		this.description = description;
		this.accomplished = false;
	}

	public ObjectiveId getId()
	{
		return this.id;
	}
	
	public void setId(ObjectiveId id)
	{
		this.id = id;
	}
	
	public Material getMaterial()
	{
		return this.material;
	}
	
	public void setMaterial(Material material)
	{
		this.material = material;
	}
	
	public String getName() 
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description) 
	{
		this.description = description;
	}

	public boolean isAccomplished() 
	{
		return accomplished;
	}
	
	public void setAccomplished(boolean isAccomplished)
	{
		this.accomplished = isAccomplished;
	}
	
	
}
