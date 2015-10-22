package com.virtualparadigm.opp.processor;

import java.io.Serializable;
import java.net.URI;

import com.virtualparadigm.openlcs.LCSElement;

public class FileElement extends LCSElement<FileTuple> implements Serializable
{
	private static final long serialVersionUID = 1L;

	public FileElement(FileTuple fileTuple)
	{
		super(fileTuple);
	}
	
	public String getPathString()
	{
		if(this.getValue() != null)
		{
			return this.getValue().getPath();
//			return this.getValue().getPath().
		}
		return "";
	}
	public String getChecksumString()
	{
		if(this.getValue() != null)
		{
			return this.getValue().getChecksum();
		}
		return "";
	}
	public long getLastModifiedLong()
	{
		if(this.getValue() != null)
		{
			return this.getValue().getLastModified();
		}
		return -1;
	}
	public boolean isDirectoryBoolean()
	{
		if(this.getValue() != null)
		{
			return this.getValue().isDirectory();
		}
		System.out.println(this.getPathString() + " is NOT a directory");
		return false;
	}
	
	@Override
	public String asString()
	{
		StringBuffer sb = new StringBuffer();
//		sb.append("checksum: " + this.getValue().getChecksum());
//		sb.append(" lastModified: " + this.getValue().getLastModified());
		sb.append(" path: " + this.getValue().getPath());
		return sb.toString();
	}
	
	
	public static void main(String[] args)
	{
		String s1 = "c:/tmp/foo";
		System.out.println(OpenPatch.getParentDirectoryPath(s1));
		
		String s2 = "/tmp/foo";
		System.out.println(OpenPatch.getParentDirectoryPath(s2));
		
		String s3 = "c:\\tmp\\bar\\foo";
		System.out.println(OpenPatch.getParentDirectoryPath(s3));
		
		
		
		
		
		
	}
}