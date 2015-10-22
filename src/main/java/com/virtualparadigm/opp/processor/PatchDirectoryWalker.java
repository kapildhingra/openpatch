package com.virtualparadigm.opp.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;

public class PatchDirectoryWalker extends DirectoryWalker<File>
{
	public PatchDirectoryWalker()
	{
		super();
	}
	
	protected boolean handleDirectory(File directory, int depth, Collection<File> results)
	{
		boolean status = false;
		System.out.println(">>>" + directory.getAbsolutePath());
		return status;
	}
	
	protected void handleFile(File file, int depth, Collection<File> results)
	{
		System.out.println(">>>" + file.getAbsolutePath());
	}
	
	public void doWalk(File file, Collection<File> results) throws IOException
	{
		super.walk(file, results);
	}
	
	public static void main(String[] args)
	{
		PatchDirectoryWalker dwalker = new PatchDirectoryWalker();
		try
		{
			List<File> result = new ArrayList<File>();
			dwalker.doWalk(new File("c:/tmp/migrator/swxiface-migrator-1.0-SNAPSHOT"), result);
			System.out.println("done");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		
	}
}