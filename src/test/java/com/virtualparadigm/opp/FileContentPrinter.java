package com.virtualparadigm.opp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;

public class FileContentPrinter
{
	private static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy H:mm:ss");
	public static void main(String[] args)
	{
		FileContentPrinter.printFiles(new File(args[0]), System.out);
	}
	public static void printFiles(File directory, OutputStream outputStream)
	{
		if(directory != null)
		{
			Collection<File> files = FileUtils.listFiles(directory, null, true);
			for(File file : files)
			{
				try
				{
					System.out.println(file.getName() + " contents: " + FileUtils.readFileToString(file) + " lastModified:" + file.lastModified() + " - " + sdf.format(new Date(file.lastModified())));
				}
				catch(IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		}
	}
	
	
	public static void printFile(File file, OutputStream outputStream)
	{
		
	}
	
}