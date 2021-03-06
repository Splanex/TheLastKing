package fr.tmagnier.TheLastKing;

import java.lang.reflect.Field;
import java.util.Random;

import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_11_R1.IChatBaseComponent;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;
import net.minecraft.server.v1_11_R1.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_11_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_11_R1.PlayerConnection;
import net.minecraft.server.v1_11_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_11_R1.PacketPlayOutTitle.EnumTitleAction;

public class Utils 
{
	public static Random random = new Random(System.currentTimeMillis());
	
	public static int getRandomInteger(int min, int max)
	{
		Main.sendDebugMessage("getRandomInteger(" + min + ", " + max + ")");
		int result = min + random.nextInt(max - min);
		Main.sendDebugMessage("Nombre aleatoire entre " + min + " et " + max + " : " + result);
		return result;
	}
	
	public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) 
	{
        CraftPlayer craftplayer = (CraftPlayer) player;
        PlayerConnection connection = craftplayer.getHandle().playerConnection;
        IChatBaseComponent titleJSON = ChatSerializer.a("{\"text\": \"" + title + "\"}");
        IChatBaseComponent subtitleJSON = ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, titleJSON, fadeIn, stay, fadeOut);
        PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, subtitleJSON);
        connection.sendPacket(titlePacket);
        connection.sendPacket(subtitlePacket);
    }

	public static void sendTabHeaderFooter(Player player, String header, String footer)
	{
	    CraftPlayer craftplayer = (CraftPlayer) player;
	    PlayerConnection connection = craftplayer.getHandle().playerConnection;
	    IChatBaseComponent headerJSON = ChatSerializer.a("{\"text\": \"" + header +"\"}");
	    IChatBaseComponent footerJSON = ChatSerializer.a("{\"text\": \"" + footer +"\"}");
	    PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
	  
	    try {
	        Field headerField = packet.getClass().getDeclaredField("a");
	        headerField.setAccessible(true);
	        headerField.set(packet, headerJSON);
	        headerField.setAccessible(!headerField.isAccessible());
	      
	        Field footerField = packet.getClass().getDeclaredField("b");
	        footerField.setAccessible(true);
	        footerField.set(packet, footerJSON);
	        footerField.setAccessible(!footerField.isAccessible());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    connection.sendPacket(packet);
	}
	
	public static void sendActionBar(Player p, String message)
	{
		IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message +"\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc,(byte) 2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
	}
}
