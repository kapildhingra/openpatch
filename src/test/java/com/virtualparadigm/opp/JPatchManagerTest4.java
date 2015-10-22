package com.virtualparadigm.opp;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.virtualparadigm.opp.processor.OpenPatch;

public class JPatchManagerTest4
{
	
	public static void main(String[] args)
	{
		JPatchManagerTest4.execute();
	}
	
	public static void execute()
	{
		System.out.println("test4: creating patch with rollback - (destructive) inserts-only:false force-updates:false");
		String rootTestDataDirPath = "data/test4/";
		String version1DirPath = rootTestDataDirPath + "version1";
		String version2DirPath = rootTestDataDirPath + "version2";
		String patchDirPath = rootTestDataDirPath + "patch";
		String destTemplateDirPath = rootTestDataDirPath + "dest-template";
		String destDirPath = rootTestDataDirPath + "dest";
		String rollbackPatchDirPath = rootTestDataDirPath + "rollback";
		
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

			FileUtils.copyDirectory(new File(destTemplateDirPath), destDir, true);
			
			System.out.println("");
			System.out.println("=================================================");
			System.out.println("DEST DIR INITIAL STATE:");
			System.out.println("=================================================");
			FileContentPrinter.printFiles(new File(destDirPath), System.out);
			
			System.out.println("");
			System.out.println("applying rollback patch...");
			OpenPatch.executePatch(
					new File(patchDirPath),
					new File(destDirPath),
					null,
					new File(rollbackPatchDirPath), 
					false, 
					true);
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