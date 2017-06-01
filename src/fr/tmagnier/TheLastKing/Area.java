package fr.tmagnier.TheLastKing;

import org.bukkit.Location;
import org.bukkit.Material;

public class Area 
{
    private Location locationMin;
    private Location locationMax;

    public Area(Location location1, Location location2)
    {
        this.setLocations(location1, location2);
    }

    public boolean isInside(Location location)
    {
        return location.getX() > locationMin.getX() && location.getX() < locationMax.getX()
                && location.getZ() > locationMin.getZ() && location.getZ() < locationMax.getZ();
    }
    
    public Location getLocationMin()
    {
        return this.locationMin;
    }

    public Location getLocationMax()
    {
        return this.locationMax;
    }

    public void setLocations(Location location1, Location location2)
    {
        double xMin = Math.min(location1.getX(), location2.getX());
        double xMax = Math.max(location1.getX(), location2.getX());
        double zMin = Math.min(location1.getZ(), location2.getZ());
        double zMax = Math.max(location1.getZ(), location2.getZ());
        this.locationMin = new Location(location1.getWorld(), xMin, 0, zMin);
        this.locationMax = new Location(location2.getWorld(), xMax, 0, zMax);
        Main.sendDebugMessage("Location min : " + locationMin.getX() + " " + locationMin.getZ());
        Main.sendDebugMessage("Location max : " + locationMax.getX() + " " + locationMax.getZ());
    }
    
    public Location getCenterLocation()
    {
    	Location center = locationMax.clone();
    	center.add(-150,0,-150);
    	center.setY(center.getWorld().getHighestBlockYAt(center.getBlockX(), center.getBlockZ()));
    	Main.sendDebugMessage("Centre : " + center.getX() + " " + center.getY() + " " + center.getZ());
    	return center;
    }
    
    public Location getRandomLocation()
    {
    	int x, y, z;
    	do {
    		x = Utils.getRandomInteger((int)locationMin.getX(), (int)locationMax.getX());
    		z = Utils.getRandomInteger((int)locationMin.getZ(), (int)locationMax.getZ());
    		y = locationMin.getWorld().getHighestBlockYAt(x, z);
    		Main.sendDebugMessage("Y de " + x + " " + z + " : " + y + " ("+locationMin.getWorld().getBlockAt(x, y-1,z).getType()+")"); 
    	} while (locationMin.getWorld().getBlockAt(x, y-1, z).getType() == Material.STATIONARY_WATER);
    	
    	return new Location(locationMin.getWorld(), x, y, z);
    }
}