package com.virtualparadigm.opp.ant;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.Task;

import com.virtualparadigm.opp.processor.OpenPatch;

public class ExecuteDirectoryPatchTask extends Task
{
	private String patchDir;
	private String archivePath;
	private String tempDir;
	private String targetDir;
	private String rollbackDir;
	private String excludeRegex;
	private String excludeGlob;
//	private String rollbackFile;
//	private String rollbackDataDir;
	private boolean insertsOnly;
	private boolean forceUpdates;
	
	public ExecuteDirectoryPatchTask()
	{
		super(); 
	}
	
	public void execute()
	{
System.out.println("========================================================================================");
System.out.println("ExecuteDirectoryPatchTask");
System.out.println("----------------------------------------------------------------------------------------");
System.out.println("Required Runtime Jars: dom4j.jar, jaxen.jar, commons-io.jar (built with 2.4), ");
System.out.println("                       commons-codec.jar (built with 1.9).");
System.out.println("========================================================================================");
System.out.println("Patch Dir:" + patchDir);
System.out.println("Archive Path:" + archivePath);
System.out.println("Target Root Directory:" + targetDir);
System.out.println("Rollback Directory:" + rollbackDir);
System.out.println("Regex Filter:" + excludeRegex);
System.out.println("Wildcard Filter:" + excludeGlob);

//System.out.println("Rollback Patch File:" + rollbackFile);
//System.out.println("Rollback Patch Files Root Directory:" + rollbackDataDir);
System.out.println("Inserts Only:" + insertsOnly);
System.out.println("Force Updates:" + forceUpdates);
System.out.println("");
		OpenPatch directoryPatchManager = new OpenPatch();
		try
		{
			
			
			if(patchDir == null || patchDir.length() == 0)
			{
				//use zip
				if(rollbackDir == null || rollbackDir.length() == 0)
				{
					directoryPatchManager.executePatch(
							new File(archivePath), 
							new File(tempDir), 
							new File(targetDir), 
							ExecuteDirectoryPatchTask.createMatcher(excludeRegex, excludeGlob), 
							null, 
							insertsOnly, 
							forceUpdates);
				}
				else
				{
					directoryPatchManager.executePatch(
							new File(archivePath), 
							new File(tempDir), 
							new File(targetDir), 
							ExecuteDirectoryPatchTask.createMatcher(excludeRegex, excludeGlob), 
							new File(rollbackDir), 
							insertsOnly, 
							forceUpdates);
				}				
			}
			else
			{
				if(rollbackDir == null || rollbackDir.length() == 0)
				{
					directoryPatchManager.executePatch(
							new File(patchDir),
							new File(targetDir), 
							ExecuteDirectoryPatchTask.createMatcher(excludeRegex, excludeGlob), 
							null, 
							insertsOnly, 
							forceUpdates);
				}
				else
				{
					directoryPatchManager.executePatch(
							new File(patchDir),
							new File(targetDir), 
							ExecuteDirectoryPatchTask.createMatcher(excludeRegex, excludeGlob), 
							new File(rollbackDir), 
							insertsOnly, 
							forceUpdates);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
System.out.println("Done.");
	}

	
	private static Matcher createMatcher(String regexFilterExpression, String globFilterExpression)
	{
		Matcher matcher = null;
		if(regexFilterExpression != null && regexFilterExpression.length() > 0)
		{
			matcher = Pattern.compile(regexFilterExpression).matcher("");
		}
		else if(globFilterExpression != null && globFilterExpression.length() > 0)
		{
			matcher = Pattern.compile(ExecuteDirectoryPatchTask.convertGlobToRegex(globFilterExpression)).matcher("");
		}
		return matcher;
	}
	
	private static String convertGlobToRegex(String globExpression)
	{
		String regex = "";
		if(globExpression != null)
		{
			
		}
		return regex;
	}
	
//	private static FileFilter createFileFilter(String regexFilterExpression, String wildcardFilterExpression)
//	{
//		FileFilter fileFilter = null;
//		if(regexFilterExpression != null && regexFilterExpression.length() > 0)
//		{
//			fileFilter = new RegexFileFilter(regexFilterExpression);
//		}
//		else if(wildcardFilterExpression != null && wildcardFilterExpression.length() > 0)
//		{
//			fileFilter = new WildcardFileFilter(wildcardFilterExpression);
//		}
//		return fileFilter;
//	}
	
	public String getPatchDir()
	{
		return patchDir;
	}

	public void setPatchDir(String patchDir)
	{
		this.patchDir = patchDir;
	}

	public String getArchivePath()
	{
		return archivePath;
	}

	public void setArchivePath(String archivePath)
	{
		this.archivePath = archivePath;
	}

	public String getTempDir()
	{
		return tempDir;
	}

	public void setTempDir(String tempDir)
	{
		this.tempDir = tempDir;
	}

	public String getTargetDir()
	{
		return targetDir;
	}

	public void setTargetDir(String targetRootDir)
	{
		this.targetDir = targetRootDir;
	}

	public String getRollbackDir()
	{
		return rollbackDir;
	}

	public void setRollbackDir(String rollbackDir)
	{
		this.rollbackDir = rollbackDir;
	}

	public boolean isInsertsOnly()
	{
		return insertsOnly;
	}

	public void setInsertsOnly(boolean insertsOnly)
	{
		this.insertsOnly = insertsOnly;
	}

	public boolean isForceUpdates()
	{
		return forceUpdates;
	}

	public void setForceUpdates(boolean forceUpdates)
	{
		this.forceUpdates = forceUpdates;
	}

	public String getExcludeRegex()
	{
		return excludeRegex;
	}

	public void setExcludeRegex(String regexFilter)
	{
		this.excludeRegex = regexFilter;
	}

	public String getExcludeGlob()
	{
		return excludeGlob;
	}

	public void setExcludeGlob(String wildcardFilter)
	{
		this.excludeGlob = wildcardFilter;
	}

}