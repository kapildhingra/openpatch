package com.virtualparadigm.opp;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.virtualparadigm.opp.processor.OpenPatch;

public class JPatchManagerTest3
{
	
	public static void main(String[] args)
	{
		JPatchManagerTest3.execute();
	}
	
	public static void execute()
	{
		System.out.println("test3: creating patch - (non-destructive) inserts-only:true force-inserts:false");
		String rootTestDataDirPath = "data/test3/";
		String version1DirPath = rootTestDataDirPath + "version1";
		String version2DirPath = rootTestDataDirPath + "version2";
		String patchDirPath = rootTestDataDirPath + "patch";
		String destDirPath = rootTestDataDirPath + "dest";
		
		try
		{
			FileUtils.deleteDirectory(new File(patchDirPath));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		OpenPatch.makePatch(
				new File(version1DirPath),
				new File(version2DirPath),
				new File(patchDirPath),
				null);
		
		// TEST EXECUTE PATCH:
		File destDir = new File(destDirPath);
		try
		{
			FileUtils.deleteDirectory(destDir);

			FileUtils.copyDirectory(new File(version1DirPath), destDir, true);
			System.out.println("");
			System.out.println("=================================================");
			System.out.println("DEST DIR INITIAL STATE:");
			System.out.println("=================================================");
			FileContentPrinter.printFiles(new File(destDirPath), System.out);
			
			System.out.println("");
			System.out.println("applying patch...");
			OpenPatch.executePatch(
					new File(patchDirPath),
					new File(destDirPath),
					null,
					null, 
					true, 
					false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("");
		System.out.println("=================================================");
		System.out.println("RESULT:");
		System.out.println("=================================================");
		FileContentPrinter.printFiles(new File(destDirPath), System.out);
	}

}