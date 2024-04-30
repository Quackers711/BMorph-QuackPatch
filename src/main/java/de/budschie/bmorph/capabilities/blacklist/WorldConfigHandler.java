package de.budschie.bmorph.capabilities.blacklist;

import java.io.File;
import java.io.IOException;

import de.budschie.bmorph.main.ServerSetup;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;

public abstract class WorldConfigHandler
{
	public final String path;
	
	public WorldConfigHandler(String path)
	{
		this.path = path;
	}
	
	public abstract void read(CompoundNBT data);
	public abstract CompoundNBT write();
	
	public void readFromFile()
	{
		File resolvedPath = new File(ServerSetup.server.anvilConverterForAnvilFile.getWorldDir().toFile(), "morph_blacklist.dat");
		
		if(resolvedPath.exists())
		{
			try
			{
				CompoundNBT data = CompressedStreamTools.read(resolvedPath);
				
				read(data);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void writeToFile()
	{
		File resolvedPath = new File(ServerSetup.server.anvilConverterForAnvilFile.getWorldDir().toFile(), "morph_blacklist.dat");
		
		CompoundNBT serialized = write();
		
		try
		{
			CompressedStreamTools.write(serialized, resolvedPath);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
