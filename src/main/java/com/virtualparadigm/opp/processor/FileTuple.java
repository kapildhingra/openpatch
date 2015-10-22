package com.virtualparadigm.opp.processor;

import java.io.Serializable;
import java.net.URI;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class FileTuple implements Comparable<FileTuple>, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String path;
//	private URI path;
	private String checksum;
	private long lastModified;
	private boolean directory;
	
	public FileTuple()
	{
		super();
	}

	public FileTuple(String path, String checksum, long lastModified, boolean directory)
//	public FileTuple(URI path, String checksum, long lastModified, boolean directory)
	{
		super();
		this.path = path;
		this.checksum = checksum;
		this.lastModified = lastModified;
		this.directory = directory;
	}

//	public URI getPath()
//	{
//		return path;
//	}
//
//	public void setPath(URI path)
//	{
//		this.path = path;
//	}

	public String getPath()
	{
		return path;
	}
	public void setPath(String path)
	{
		this.path = path;
	}
	public String getChecksum()
	{
		return checksum;
	}
	public void setChecksum(String checksum)
	{
		this.checksum = checksum;
	}
	public long getLastModified()
	{
		return lastModified;
	}
	public void setLastModified(long lastModified)
	{
		this.lastModified = lastModified;
	}
	
	public boolean isDirectory()
	{
		return directory;
	}
	public void setDirectory(boolean directory)
	{
		this.directory = directory;
	}
	
	@Override
	public int compareTo(FileTuple that)
	{
		if(that == null)
		{
			return 1;
		}
		
//		int result = FileTuple.nullSafeURIComparator(this.getPath(), that.getPath());
//		if(result != 0)
//		{
//			return result;
//		}
		
		int result = FileTuple.nullSafeStringComparator(this.getPath(), that.getPath());
		if(result != 0)
		{
			return result;
		}
		
		result = FileTuple.nullSafeStringComparator(this.getChecksum(), that.getChecksum());
		if(result != 0)
		{
			return result;
		}
		
		result = Boolean.compare(this.isDirectory(), that.isDirectory());
		if(result != 0)
		{
			return 1;
		}
		
		//only compare last modified if they are both directories
		if(this.isDirectory())
		{
			return 0;
		}
		else
		{
			if(this.lastModified < that.getLastModified())
			{
				return -1;
			}
			else if(this.lastModified > that.getLastModified())
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}
	}
	
	public static int nullSafeStringComparator(final String s1, final String s2)
	{
		if (s1 == null ^ s2 == null)
		{
			return (s1 == null) ? -1 : 1;
		}
		if (s1 == null && s2 == null)
		{
			return 0;
		}
		return s1.compareTo(s2);
	}	
	
	public static int nullSafeURIComparator(final URI u1, final URI u2)
	{
		if (u1 == null ^ u2 == null)
		{
			return (u1 == null) ? -1 : 1;
		}
		if (u1 == null && u2 == null)
		{
			return 0;
		}
		return u1.compareTo(u2);
	}	
	
	// ==============================================
	// UTILITY METHODS
	// ==============================================
    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this);
    }
    
    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(this.getPath());
        builder.append(this.getChecksum());
        builder.append(this.getLastModified());
        return builder.toHashCode();        
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (obj.getClass() != getClass())
        {
            return false;
        }
        FileTuple that = (FileTuple)obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.getPath(), that.getPath());
        builder.append(this.getChecksum(), that.getChecksum());
        builder.append(this.getLastModified(), that.getLastModified());
        return builder.isEquals();
    }
	
}