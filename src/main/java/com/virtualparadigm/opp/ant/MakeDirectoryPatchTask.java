package com.virtualparadigm.opp.ant;

import java.io.File;

import org.apache.tools.ant.Task;

import com.virtualparadigm.opp.processor.OpenPatch;

public class MakeDirectoryPatchTask extends Task
{
	private String newDir;
	private String oldDir;
	private String outputDir;
	private String archivePath;
	
//	private String patchDataDir;
//	private String patchFile;
	
	public MakeDirectoryPatchTask()
	{
		super(); 
	}
	
	public void execute()
	{
System.out.println("========================================================================================");
System.out.println("MakeDirectoryPatchTask");
System.out.println("----------------------------------------------------------------------------------------");
System.out.println("Required Runtime Jars: dom4j.jar, jaxen.jar, commons-io.jar (built with 2.4), ");
System.out.println("                       commons-codec.jar (built with 1.9).");
System.out.println("========================================================================================");
System.out.println("New State Directory:" + newDir);
System.out.println("Old State Directory:" + oldDir);
System.out.println("Output Directory:" + outputDir);
System.out.println("Archive File Path:" + archivePath);
System.out.println("");
		OpenPatch directoryPatchManager = new OpenPatch();
		try
		{
			directoryPatchManager.makePatch(
					new File(newDir), 
					new File(oldDir), 
					new File(outputDir), 
					archivePath);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
System.out.println("Done.");
	}

	public String getNewDir()
	{
		return newDir;
	}

	public void setNewDir(String newStateRootDir)
	{
		this.newDir = newStateRootDir;
	}

	public String getOldDir()
	{
		return oldDir;
	}

	public void setOldDir(String oldStateRootDir)
	{
		this.oldDir = oldStateRootDir;
	}

	public String getOutputDir()
	{
		return outputDir;
	}

	public void setOutputDir(String outputDir)
	{
		this.outputDir = outputDir;
	}

	public String getArchivePath()
	{
		return archivePath;
	}

	public void setArchivePath(String archivePath)
	{
		this.archivePath = archivePath;
	}

	

}