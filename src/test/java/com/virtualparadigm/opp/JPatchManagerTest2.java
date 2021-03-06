package com.virtualparadigm.opp;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.virtualparadigm.opp.processor.OpenPatch;

public class JPatchManagerTest2
{
	public static void main(String[] args)
	{
		JPatchManagerTest2.execute();
	}
	
	
	public static void execute()
	{
		System.out.println("test2: creating patch - (destructive) inserts-only:false force-updates:true");
		String rootTestDataDirPath = "data/test2/";
		String version1DirPath = rootTestDataDirPath + "version1";
		String version2DirPath = rootTestDataDirPath + "version2";
		String patchDirPath = rootTestDataDirPath + "patch";
		String destTemplateDirPath = rootTestDataDirPath + "dest-template";
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

			System.out.println("");
			System.out.println("=================================================");
			System.out.println("V1 DIR INITIAL STATE:");
			System.out.println("=================================================");
			FileContentPrinter.printFiles(new File(version1DirPath), System.out);
			
			FileUtils.copyDirectory(new File(destTemplateDirPath), destDir, true);
			
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
					false, 
					true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("");
		System.out.println("=================================================");
		System.out.println("DEST DIR UPDATED STATE:");
		System.out.println("=================================================");
		FileContentPrinter.printFiles(new File(destDirPath), System.out);
	}

}