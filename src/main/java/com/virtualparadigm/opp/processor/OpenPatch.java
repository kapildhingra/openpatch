package com.virtualparadigm.opp.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.virtualparadigm.openlcs.LCS;
import com.virtualparadigm.openlcs.LCSElement;
import com.virtualparadigm.opp.util.ZipUtils;

public class OpenPatch
{
	private static Logger logger = LoggerFactory.getLogger(OpenPatch.class);
	
	private static final String ELEMENT_PATCH = "patch";
	private static final String ELEMENT_ADDED_FILES = "addedFiles";
	private static final String ELEMENT_UPDATED_FILES = "udpatedFiles";
	private static final String ELEMENT_REMOVED_FILES = "removedFiles";
	private static final String ELEMENT_ADDED_DIRECTORIES = "addedDirectories";
	private static final String ELEMENT_REMOVED_DIRECTORIES = "removedDirectories";
	private static final String ELEMENT_DIRECTORY = "directory";
	private static final String ELEMENT_FILE = "file";
	private static final String ATTRIBUTE_PATH = "path";
	private static final String ATTRIBUTE_EXPECTED_LAST_MODIFIED = "expectedLastModified";
	
    private static final String PATCH_FILE_NAME = "patch.xml";
    private static final String PATCH_FILES_DIR_NAME = "patch-files";
    
	public OpenPatch()
	{
		super(); 
	}
	
	public static void main(String[] args) throws IOException
	{
		OpenPatch.makePatch(new File("c:/tmp/migrator/archive/old"), new File("c:/tmp/migrator/archive/new"), new File("temp"), "arch.zip");
		
		
//		OpenPatch.executePatch(
//				new File("C:/dev/workbench/test_workspace/openpatch/temp/arch.zip"), 
//				new File("tempexec"), 
//				new File("c:/tmp/migrator/archive/target"), 
//				null, 
//				new File("rollback"), 
//				false, 
//				true);
		
	}
	

	public static void makePatch(File oldStateRootDir, File newStateRootDir, File outputDir, String archiveFileName)// throws IOException
	{
		try
		{
			//if prepare fails (IOException), doesn't make sense to go further
	        OpenPatch.prepareDir(outputDir, false);
			File patchFile = new File(outputDir.getAbsolutePath() + "/" + OpenPatch.PATCH_FILE_NAME);
			File patchFilesDir = new File(outputDir.getAbsolutePath() + "/" + OpenPatch.PATCH_FILES_DIR_NAME);
			
			Map<String, List<FileElement>> patchMap = OpenPatch.createPatchMap(oldStateRootDir, newStateRootDir);
			OpenPatch.writePatchToFile(patchMap, patchFile);
			OpenPatch.copyFiles(patchMap.get(ELEMENT_ADDED_FILES), newStateRootDir, patchFilesDir);
			OpenPatch.copyFiles(patchMap.get(ELEMENT_UPDATED_FILES), newStateRootDir, patchFilesDir);
			
			if(archiveFileName != null && archiveFileName.length() > 0)
			{
				ZipUtils.createZipFile(outputDir.getAbsolutePath() + "/" + archiveFileName, new File[]{patchFile, patchFilesDir}, 1024);
				FileUtils.deleteQuietly(patchFile);
				FileUtils.deleteQuietly(patchFilesDir);
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	private static Map<String, List<FileElement>> createPatchMap(File oldStateRootDir, File newStateRootDir)// throws IOException
	{
		Map<String, List<FileElement>> patchElementMap = null;
		if(oldStateRootDir != null && newStateRootDir != null)
		{
			Map<String, FileElement> oldDirSequenceMap = new LinkedHashMap<String, FileElement>();
			Map<String, FileElement> newDirSequenceMap = new LinkedHashMap<String, FileElement>();
			
			File file = null;
			String relativePath = null;
			for(Iterator<File> fileIterator=FileUtils.iterateFilesAndDirs(oldStateRootDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE); fileIterator.hasNext(); )
			{
				file = fileIterator.next();
				relativePath = OpenPatch.getRelativePath(file.getAbsolutePath(), oldStateRootDir.getAbsolutePath());
				if(relativePath.length() > 0)
				{
					oldDirSequenceMap.put(relativePath,	new FileElement(new FileTuple(relativePath, checksum(file), file.lastModified(), file.isDirectory())));
				}
			}

			for(Iterator<File> fileIterator=FileUtils.iterateFilesAndDirs(newStateRootDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE); fileIterator.hasNext(); )
			{
				file = fileIterator.next();
				relativePath = OpenPatch.getRelativePath(file.getAbsolutePath(), newStateRootDir.getAbsolutePath());
				if(relativePath.length() > 0)
				{
					newDirSequenceMap.put(relativePath, new FileElement(new FileTuple(relativePath,	checksum(file), file.lastModified(), file.isDirectory())));
				}
			}
			
			List<LCSElement<FileTuple>[]> solutions = 
					LCS.compute(
							oldDirSequenceMap.values().toArray(new FileElement[oldDirSequenceMap.size()]), 
							newDirSequenceMap.values().toArray(new FileElement[newDirSequenceMap.size()]), 
							false);
			
			if(solutions != null)
			{
				for(LCSElement<FileTuple>[] solution : solutions)
				{
					for(LCSElement<FileTuple> element : solution)
					{
						oldDirSequenceMap.remove(element.getValue().getPath());
						newDirSequenceMap.remove(element.getValue().getPath());
					}
				}
			}

			//there are still some intersections (updated files right now)
			//files in old not in new = delete
			//files in new not in old = added
			List<FileElement> addedFileList = new ArrayList<FileElement>();
			List<FileElement> updatedFileList = new ArrayList<FileElement>();
			List<FileElement> removedFileList = new ArrayList<FileElement>();
			List<FileElement> addedDirectoryList = new ArrayList<FileElement>();
			List<FileElement> removedDirectoryList = new ArrayList<FileElement>();
			
			//expected last modified is only for remove and update files!!!!
			FileElement newDirFileElement = null;
			for(FileElement oldFileElement : oldDirSequenceMap.values())
			{
				newDirFileElement = newDirSequenceMap.get(oldFileElement.getPathString());
				if(newDirFileElement != null)
				{
					// directories with same name but different last modified dates are considered equal
					// so they were already filtered out at this point.
					// we only need to handle files here. Only update if new file timestamp is greater than old
					if(newDirFileElement.getLastModifiedLong() > oldFileElement.getLastModifiedLong())
					{
						updatedFileList.add(oldFileElement);
					}
				}
				else
				{
					if(oldFileElement.isDirectoryBoolean())
					{
						removedDirectoryList.add(oldFileElement);
					}
					else
					{
						removedFileList.add(oldFileElement);
					}
				}
			}
			
			for(FileElement newFileElement : newDirSequenceMap.values())
			{
				if(!oldDirSequenceMap.containsKey(newFileElement.getPathString()))
				{
					
					if(newFileElement.isDirectoryBoolean())
					{
						addedDirectoryList.add(newFileElement);
					}
					else
					{
						addedFileList.add(newFileElement);
					}
//					addedFileList.add(newFileElement);
				}
			}
			
			patchElementMap = new HashMap<String, List<FileElement>>();
			patchElementMap.put(OpenPatch.ELEMENT_ADDED_FILES, addedFileList);
			patchElementMap.put(OpenPatch.ELEMENT_UPDATED_FILES, updatedFileList);
			patchElementMap.put(OpenPatch.ELEMENT_REMOVED_FILES, removedFileList);
			patchElementMap.put(OpenPatch.ELEMENT_ADDED_DIRECTORIES, addedDirectoryList);
			patchElementMap.put(OpenPatch.ELEMENT_REMOVED_DIRECTORIES, removedDirectoryList);
		}
		return patchElementMap;
	}
	
	public static void executePatch(File archiveFile, File tempDir, File targetRootDir, Matcher excludeMatcher, File rollbackRootDir, boolean insertsOnly, boolean forceUpdates)
	{
		try
		{
			OpenPatch.prepareDir(tempDir, true);
			ZipUtils.unzipArchive(archiveFile, tempDir);
			OpenPatch.executePatch(tempDir, targetRootDir, excludeMatcher, rollbackRootDir, insertsOnly, forceUpdates);
			FileUtils.deleteQuietly(tempDir);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public static void executePatch(File patchRootDir, File targetRootDir, Matcher excludeMatcher, File rollbackPatchRootDir, boolean insertsOnly, boolean forceUpdates)
	{
		try
		{
			File patchFile = new File(patchRootDir.getAbsolutePath() + "/" + OpenPatch.PATCH_FILE_NAME);
			File patchFilesDir = new File(patchRootDir.getAbsolutePath() + "/" + OpenPatch.PATCH_FILES_DIR_NAME);
			File rollbackPatchFile = null;
			File rollbackPatchFilesDir = null;
			
			if(rollbackPatchRootDir != null)
			{
				rollbackPatchFile = new File(rollbackPatchRootDir.getAbsolutePath() + "/" + OpenPatch.PATCH_FILE_NAME);
				rollbackPatchFilesDir = new File(rollbackPatchRootDir.getAbsolutePath() + "/" + OpenPatch.PATCH_FILES_DIR_NAME);
				
				//if rollback is required, but cannot be prepared, doesnt make sense to go on (catching IOException at bottom)
				OpenPatch.prepareDir(rollbackPatchRootDir, true);
				OpenPatch.prepareDir(rollbackPatchFilesDir, true);
			}
			
			if(excludeMatcher == null)
			{
				//If no matcher provided, match everything 
//				excludeMatcher = Pattern.compile(".*").matcher("");
				excludeMatcher = Pattern.compile("").matcher("");
			}
			
			SAXReader saxReader = new SAXReader();
			Document patchdocument = 
					saxReader.read(
							new StringReader(
									FileUtils.readFileToString(patchFile)));

			if(patchdocument != null)
			{
	            List<File> addedRollbackFileList = new ArrayList<File>();
	            List<File> updatedRollbackFileList = new ArrayList<File>();
	            List<File> addedRollbackDirList = new ArrayList<File>(); 
	            List<File> removedRollbackFileList = new ArrayList<File>();
	            List<File> removedRollbackDirList = new ArrayList<File>();
				
				Element element = null;
				File targetFileOrDir = null;
				File patchedFileOrDir = null;
				String targetFileOrDirPath = null;
				
				if(!insertsOnly)
				{
					// ==================================================
					// DESTRUCTIVE OPERATIONS
					// ==================================================
					
					//delete removed directories
					Element removedDirectoriesElement = (Element)patchdocument.selectSingleNode(ELEMENT_PATCH + "/" + ELEMENT_REMOVED_DIRECTORIES);
					if(removedDirectoriesElement != null)
					{
		                for(Iterator<Element> elementIterator=removedDirectoriesElement.elementIterator(); elementIterator.hasNext(); )
		                {
		                    element = elementIterator.next();
		                    targetFileOrDirPath = convertPathToForwardSlash(targetRootDir.getAbsolutePath()) + "/" + element.attributeValue(ATTRIBUTE_PATH);
		                    if(targetFileOrDirPath != null && targetFileOrDirPath.length() > 0)
		                    {
			                    excludeMatcher.reset(targetFileOrDirPath);
			                    if(!excludeMatcher.matches())
			                    {
				                    targetFileOrDir = new File(targetFileOrDirPath);
				                    if(targetFileOrDir.exists())
				                    {
					                    if(rollbackPatchFilesDir != null)
					                    {
					                    	//copy removed directory to rollback location
					                    	FileUtils.copyDirectoryToDirectory(
					                    			targetFileOrDir, 
					                    			new File(rollbackPatchFilesDir.getAbsolutePath() + "/" + element.attributeValue(ATTRIBUTE_PATH)).getParentFile());
					                    	addedRollbackDirList.add(targetFileOrDir);
					                    }
					                    FileUtils.deleteDirectory(targetFileOrDir);
				                    }
			                    }
		                    }
		                }
					}
					
					//delete removed files
					Element removedFilesElement = (Element)patchdocument.selectSingleNode(ELEMENT_PATCH + "/" + ELEMENT_REMOVED_FILES);
					if(removedFilesElement != null)
					{
		                for(Iterator<Element> elementIterator=removedFilesElement.elementIterator(); elementIterator.hasNext(); )
		                {
		                    element = elementIterator.next();
		                    targetFileOrDirPath = convertPathToForwardSlash(targetRootDir.getAbsolutePath()) + "/" + element.attributeValue(ATTRIBUTE_PATH);
		                    if(targetFileOrDirPath != null && targetFileOrDirPath.length() > 0)
		                    {
		                    	if(excludeMatcher != null)
		                    	{
				                    excludeMatcher.reset(targetFileOrDirPath);
		                    	}
			                    if(excludeMatcher == null || !excludeMatcher.matches())
			                    {
				                    targetFileOrDir = new File(targetFileOrDirPath);
				                    if(targetFileOrDir.exists())
				                    {
				                    	if(forceUpdates || targetFileOrDir.lastModified() == Long.valueOf(element.attributeValue(ATTRIBUTE_EXPECTED_LAST_MODIFIED)))
				                    	{
						                    if(rollbackPatchFilesDir != null)
						                    {
						                    	//copy removed directory to rollback location
						                    	FileUtils.copyFile(
						                    			targetFileOrDir, 
						                    			new File(rollbackPatchFilesDir.getAbsolutePath() + "/" + element.attributeValue(ATTRIBUTE_PATH)), 
						                    			true);
						                    	
						                    	addedRollbackFileList.add(targetFileOrDir);
						                    }
						                    FileUtils.deleteQuietly(targetFileOrDir);
					                    }
			                    	}				                    
			                    }
		                    }
		                }
					}
					
					
					//copy updated files
					Element updatedFilesElement = (Element)patchdocument.selectSingleNode(ELEMENT_PATCH + "/" + ELEMENT_UPDATED_FILES);
					if(updatedFilesElement != null)
					{
		                for(Iterator<Element> elementIterator=updatedFilesElement.elementIterator(); elementIterator.hasNext(); )
		                {
		                    element = elementIterator.next();
		                    targetFileOrDirPath = convertPathToForwardSlash(targetRootDir.getAbsolutePath()) + "/" + element.attributeValue(ATTRIBUTE_PATH);
		                    if(targetFileOrDirPath != null && targetFileOrDirPath.length() > 0)
		                    {
		                    	if(excludeMatcher != null)
		                    	{
				                    excludeMatcher.reset(targetFileOrDirPath);
		                    	}
			                    if(excludeMatcher == null || !excludeMatcher.matches())
			                    {
				                    targetFileOrDir = new File(targetFileOrDirPath);
									patchedFileOrDir = new File(convertPathToForwardSlash(patchFilesDir.getAbsolutePath()) + "/" + element.attributeValue(ATTRIBUTE_PATH));
				                    if(targetFileOrDir.exists())
				                    {
				                    	if(forceUpdates || targetFileOrDir.lastModified() == Long.valueOf(element.attributeValue(ATTRIBUTE_EXPECTED_LAST_MODIFIED)))
				                    	{
				    	                    if(rollbackPatchFilesDir != null)
				    	                    {
				    	                    	//copy removed directory to rollback location
				    	                    	FileUtils.copyFile(
				    	                    			targetFileOrDir, 
				    	                    			new File(rollbackPatchFilesDir.getAbsolutePath() + "/" + element.attributeValue(ATTRIBUTE_PATH)), 
				    	                    			true);
				    	                    	
				    	                    	//this should be an updaterollback file list
				    	                    	updatedRollbackFileList.add(targetFileOrDir);
				    	                    }
				    	                    
				    	                    FileUtils.copyFileToDirectory(
				    	                    		patchedFileOrDir, 
				    	                    		targetFileOrDir.getParentFile(), 
				    	                    		true);
				                    	}
				                    }
				                    else
				                    {
				                    	//do add file
					                    if(rollbackPatchFilesDir != null)
					                    {
					                    	removedRollbackFileList.add(targetFileOrDir);
					                    }
					                    FileUtils.copyFileToDirectory(
					                    		patchedFileOrDir, 
					                    		targetFileOrDir.getParentFile(), 
					                    		true);
				                    }
			                    }
		                    }
		                }
					}
				}
				
				
				// ==================================================
				// NON DESTRUCTIVE OPERATIONS
				// ==================================================
				
				//create new directories
				Element addedDirectoriesElement = (Element)patchdocument.selectSingleNode(ELEMENT_PATCH + "/" + ELEMENT_ADDED_DIRECTORIES);
				if(addedDirectoriesElement != null)
				{
	                for(Iterator<Element> elementIterator=addedDirectoriesElement.elementIterator(); elementIterator.hasNext(); )
	                {
	                    element = elementIterator.next();
	                    targetFileOrDirPath = convertPathToForwardSlash(targetRootDir.getAbsolutePath()) + "/" + element.attributeValue(ATTRIBUTE_PATH);
	                    if(targetFileOrDirPath != null && targetFileOrDirPath.length() > 0)
	                    {
	                    	if(excludeMatcher != null)
	                    	{
			                    excludeMatcher.reset(targetFileOrDirPath);
	                    	}
		                    if(excludeMatcher == null || !excludeMatcher.matches())
		                    {
			                    targetFileOrDir = new File(targetFileOrDirPath);
//			                    if(rollbackPatchFilesDir != null)
//			                    {
//			                    	removedRollbackDirList.add(targetFileOrDir);
//			                    }
			                    if(targetFileOrDir.exists())
			                    {
			                    	//if directory already exists, no need to do anything
			                    	if(forceUpdates)
			                    	{
			    	                    if(rollbackPatchFilesDir != null)
			    	                    {
			    	                    	//copy existing file to rollback location
					                    	FileUtils.copyDirectoryToDirectory(
					                    			targetFileOrDir, 
					                    			new File(rollbackPatchFilesDir.getAbsolutePath() + "/" + element.attributeValue(ATTRIBUTE_PATH)).getParentFile());
			    	                    	removedRollbackDirList.add(targetFileOrDir);
			    	                    }
					                    FileUtils.deleteQuietly(targetFileOrDir);
					                    FileUtils.forceMkdir(targetFileOrDir);
			                    	}
			                    	else
			                    	{
			                    		//if dir exists and force is false, do nothing
			                    	}
			                    }
			                    else
			                    {
				                    FileUtils.forceMkdir(targetFileOrDir);
			                    }
		                    }
	                    }
	                }
				}
				
				//create added files
				Element addedFilesElement = (Element)patchdocument.selectSingleNode(ELEMENT_PATCH + "/" + ELEMENT_ADDED_FILES);
				if(addedFilesElement != null)
				{
	                for(Iterator<Element> elementIterator=addedFilesElement.elementIterator(); elementIterator.hasNext(); )
	                {
	                    element = elementIterator.next();
	                    
	                    
	                    targetFileOrDirPath = convertPathToForwardSlash(targetRootDir.getAbsolutePath()) + "/" + element.attributeValue(ATTRIBUTE_PATH);
	                    if(targetFileOrDirPath != null && targetFileOrDirPath.length() > 0)
	                    {
	                    	if(excludeMatcher != null)
	                    	{
			                    excludeMatcher.reset(targetFileOrDirPath);
	                    	}
		                    if(excludeMatcher == null || !excludeMatcher.matches())
		                    {
			                    targetFileOrDir = new File(targetFileOrDirPath);
			                    
								patchedFileOrDir = new File(convertPathToForwardSlash(patchFilesDir.getAbsolutePath()) + "/" + element.attributeValue(ATTRIBUTE_PATH));
			                    if(targetFileOrDir.exists())
			                    {
			                    	if(forceUpdates)
			                    	{
			    	                    if(rollbackPatchFilesDir != null)
			    	                    {
			    	                    	//copy existing file to rollback location
			    	                    	FileUtils.copyFile(
			    	                    			targetFileOrDir, 
			    	                    			new File(rollbackPatchFilesDir.getAbsolutePath() + "/" + element.attributeValue(ATTRIBUTE_PATH)), 
			    	                    			true);
			    	                    	updatedRollbackFileList.add(targetFileOrDir);
			    	                    }
			    	                    
			    	                    FileUtils.copyFileToDirectory(
			    	                    		patchedFileOrDir, 
			    	                    		targetFileOrDir.getParentFile(), 
			    	                    		true);
			                    	}
			                    }
			                    else
			                    {
				                    FileUtils.copyFileToDirectory(
				                    		patchedFileOrDir, 
				                    		targetFileOrDir.getParentFile(), 
				                    		true);
			                    }
		                    }
	                    }
	                }
				}
				
				// ==================================================
				// BUILD ROLLBACK PATCH
				//  ** rollback patches do NOT look at timestamps
				// ==================================================
				if(rollbackPatchFilesDir != null)
				{
					if(!rollbackPatchFilesDir.exists())
					{
						rollbackPatchFilesDir.mkdirs();
					}
					
					Document rollbackPatchDocument = DocumentHelper.createDocument();
					Element patchElement = rollbackPatchDocument.addElement(ELEMENT_PATCH);
					patchElement.addElement(ELEMENT_ADDED_FILES);
					patchElement.addElement(ELEMENT_UPDATED_FILES);
					patchElement.addElement(ELEMENT_REMOVED_FILES);
					patchElement.addElement(ELEMENT_ADDED_DIRECTORIES);
					patchElement.addElement(ELEMENT_REMOVED_DIRECTORIES);

		            for(File addedRollbackFile : addedRollbackFileList)
		            {
		            	OpenPatch.addPathElement(
		            			rollbackPatchDocument, 
		            			ELEMENT_PATCH + "/" + ELEMENT_ADDED_FILES, 
		            			ELEMENT_FILE, 
		            			ATTRIBUTE_PATH, 
		            			OpenPatch.getRelativePath(addedRollbackFile.getAbsolutePath(), targetRootDir.getAbsolutePath()));
		            }
					
		            for(File updatedRollbackFile : updatedRollbackFileList)
		            {
		            	OpenPatch.addPathElement(
		            			rollbackPatchDocument, 
		            			ELEMENT_PATCH + "/" + ELEMENT_ADDED_FILES, 
		            			ELEMENT_FILE, 
		            			ATTRIBUTE_PATH, 
		            			OpenPatch.getRelativePath(updatedRollbackFile.getAbsolutePath(), targetRootDir.getAbsolutePath()), 
		            			updatedRollbackFile.lastModified());
		            }
					
		            for(File addedRollbackDir : addedRollbackDirList)
		            {
		            	OpenPatch.addPathElement(
		            			rollbackPatchDocument, 
		            			ELEMENT_PATCH + "/" + ELEMENT_ADDED_DIRECTORIES, 
		            			ELEMENT_DIRECTORY, 
		            			ATTRIBUTE_PATH, 
		            			OpenPatch.getRelativePath(addedRollbackDir.getAbsolutePath(), targetRootDir.getAbsolutePath()));
		            }
					
		            for(File removedRollbackFile : removedRollbackFileList)
		            {
		            	OpenPatch.addPathElement(
		            			rollbackPatchDocument, 
		            			ELEMENT_PATCH + "/" + ELEMENT_REMOVED_FILES, 
		            			ELEMENT_FILE, 
		            			ATTRIBUTE_PATH, 
		            			OpenPatch.getRelativePath(removedRollbackFile.getAbsolutePath(), targetRootDir.getAbsolutePath()), 
		            			removedRollbackFile.lastModified());
		            }
		            
		            for(File removedRollbackDir : removedRollbackDirList)
		            {
		            	OpenPatch.addPathElement(
		            			rollbackPatchDocument, 
		            			ELEMENT_PATCH + "/" + ELEMENT_REMOVED_DIRECTORIES, 
		            			ELEMENT_DIRECTORY, 
		            			ATTRIBUTE_PATH, 
		            			OpenPatch.getRelativePath(removedRollbackDir.getAbsolutePath(), targetRootDir.getAbsolutePath()), 
		            			removedRollbackDir.lastModified());
		            }					
					
					OpenPatch.writeDocumentToFile(rollbackPatchDocument, rollbackPatchFile);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
//	public void executePatch(File patchRootDir, File targetRootDir, File rollbackRootDir, File rollbackArchiveFile, boolean insertsOnly, boolean forceUpdates)
//	{
//		try
//		{
//			PPatchManager.prepareDir(tempDir);
//			this.executePatch(tempDir, targetRootDir, rollbackRootDir, insertsOnly, forceUpdates);
//			FileUtils.deleteQuietly(tempDir);
//		}
//		catch(IOException ioe)
//		{
//			ioe.printStackTrace();
//		}
//	}
	
	
	
	// ============================================================
	// UTILITY METHODS
	// ============================================================
	public class FileTupleBean
	{
		private File file;
		private long expectedLastModified;
		public FileTupleBean()
		{
			super();
		}
		public FileTupleBean(File file, long expectedLastModified)
		{
			super();
			this.file = file;
			this.expectedLastModified = expectedLastModified;
		}
		public File getFile()
		{
			return file;
		}
		public void setFile(File file) 
		{
			this.file = file;
		}
		public long getExpectedLastModified()
		{
			return expectedLastModified;
		}
		public void setExpectedLastModified(long expectedLastModified)
		{
			this.expectedLastModified = expectedLastModified;
		}
	}
	
	
	private static void prepareDir(File dir, boolean force) throws IOException
	{
		if(dir != null)
		{
			if(force && dir.exists())
			{
				FileUtils.forceDelete(dir);
			}
			FileUtils.forceMkdir(dir);
		}
	}
	
	private static boolean forceCopyFileToDirectory(String path, File directory)
	{
		return OpenPatch.forceCopyFileToDirectory(new File(path), directory);
	}
	
	private static boolean forceCopyFileToDirectory(File file, File directory)
	{
		boolean status = false;
		if(file != null && directory != null)
		{
			if(!directory.exists())
			{
				try
				{
					FileUtils.forceMkdir(directory);
				}
				catch(IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
			if(!file.isDirectory())
			{
				try
				{
					FileUtils.copyFileToDirectory(file, directory, true);
					status = true;
				}
				catch(IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		}
		return status;
	}
	
	private static void copyFiles(Collection<FileElement> fileElements, File rootDir, File outputDir)
	{
		if(fileElements != null && rootDir != null && outputDir != null) 
		{
			for(FileElement fileElement : fileElements)
			{
				try
				{
					FileUtils.copyFile(
							new File(OpenPatch.convertPathToForwardSlash(rootDir.getAbsolutePath() + "/" + fileElement.getPathString())), 
							new File(OpenPatch.convertPathToForwardSlash(outputDir.getAbsolutePath() + "/" + fileElement.getPathString())), 
							true);
				}
				catch(IOException ioe)
				{
					ioe.printStackTrace();
				}
//				OpenPatch.forceCopyFileToDirectory(OpenPatch.convertPathToForwardSlash(rootDir.getAbsolutePath() + "/" + fileElement.getPathString()), outputDir);
			}
		}
	}
	
	//TODO: better implementation
	public static String getRelativePath(String fullPath, String root)
	{
		String relativePath = "";
		if(fullPath != null)
		{
			if(root != null && fullPath.indexOf(root) == 0 && fullPath.length() > root.length())
			{
				relativePath = fullPath.substring(root.length() + 1);
			}
		}
		return relativePath.replace("\\", "/");
	}
	
	public static String getParentDirectoryPath(String path)
	{
		String parentDirPath = "";
		if(path != null)
		{
			path = OpenPatch.convertPathToForwardSlash(path);
			parentDirPath = path.substring(0, path.lastIndexOf("/"));
		}
		return parentDirPath;
	}
	
	private static String convertPathToForwardSlash(String path)
	{
		if(path != null)
		{
			return path.replace("\\", "/");
		}
		return "";
	}

	private static String checksum(File file)
	{
	    String checksum = "0";
	    if(file != null && !file.isDirectory())
	    {
	        try
	        {
	            FileInputStream fileInputStream = new FileInputStream(file);
	            checksum = org.apache.commons.codec.digest.DigestUtils.md5Hex(fileInputStream);
	        }
            catch(FileNotFoundException fnfe)
            {
                fnfe.printStackTrace();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
	    }
	    return checksum;
	}
	
	private static void writePatchToFile(Map<String, List<FileElement>> patchMap, File file)
	{
		if(patchMap != null)
		{
			Document patchDocument = DocumentHelper.createDocument();
			Element patchElement = patchDocument.addElement(ELEMENT_PATCH);
			patchElement.addElement(ELEMENT_ADDED_FILES);
			patchElement.addElement(ELEMENT_UPDATED_FILES);
			patchElement.addElement(ELEMENT_REMOVED_FILES);
			patchElement.addElement(ELEMENT_ADDED_DIRECTORIES);
			patchElement.addElement(ELEMENT_REMOVED_DIRECTORIES);
			
			for(FileElement addedFileElement : patchMap.get(ELEMENT_ADDED_FILES))
			{
				OpenPatch.addPathElement(
						patchDocument, 
            			ELEMENT_PATCH + "/" + ELEMENT_ADDED_FILES, 
            			ELEMENT_FILE, 
            			ATTRIBUTE_PATH, 
            			addedFileElement.getPathString());
			}
			
			for(FileElement updatedFileElement : patchMap.get(ELEMENT_UPDATED_FILES))
			{
				//EXPECTED LAST MODIFIED SHOULD BE FROM OLD, NOT NEW!!!!
	        	OpenPatch.addPathElement(
	        			patchDocument, 
	        			ELEMENT_PATCH + "/" + ELEMENT_UPDATED_FILES, 
	        			ELEMENT_FILE, 
	        			ATTRIBUTE_PATH, 
	        			updatedFileElement.getPathString(), 
	        			updatedFileElement.getLastModifiedLong());
			}
			
			for(FileElement removedFileElement : patchMap.get(ELEMENT_REMOVED_FILES))
			{
				//EXPECTED LAST MODIFIED SHOULD BE FROM OLD, NOT NEW!!!!
	        	OpenPatch.addPathElement(
	        			patchDocument, 
	        			ELEMENT_PATCH + "/" + ELEMENT_REMOVED_FILES, 
	        			ELEMENT_FILE, 
	        			ATTRIBUTE_PATH, 
	        			removedFileElement.getPathString(), 
	        			removedFileElement.getLastModifiedLong());
			}
			
			for(FileElement addedDirFileElement : patchMap.get(ELEMENT_ADDED_DIRECTORIES))
			{
				OpenPatch.addPathElement(
						patchDocument, 
            			ELEMENT_PATCH + "/" + ELEMENT_ADDED_DIRECTORIES, 
            			ELEMENT_FILE, 
            			ATTRIBUTE_PATH, 
            			addedDirFileElement.getPathString());
			}
			
			for(FileElement removedDirFileElement : patchMap.get(ELEMENT_REMOVED_DIRECTORIES))
			{
				OpenPatch.addPathElement(
						patchDocument, 
            			ELEMENT_PATCH + "/" + ELEMENT_REMOVED_DIRECTORIES, 
            			ELEMENT_FILE, 
            			ATTRIBUTE_PATH, 
            			removedDirFileElement.getPathString());
			}
			
			OpenPatch.writeDocumentToFile(patchDocument, file);
		}
	}

	private static void writeDocumentToFile(Document document, File file)
	{
		if(document != null && file != null)
		{
			FileOutputStream fileOutputStream = null;
			try
			{
		        OutputFormat outformat = OutputFormat.createPrettyPrint();
		        outformat.setEncoding("UTF-8");
		        StringWriter stringWriter = new StringWriter();
		        XMLWriter xmlWriter = new XMLWriter(stringWriter, outformat);
		        xmlWriter.write(document);
//		        xmlWriter.flush();
		        logger.debug(stringWriter.toString());
		        
		        fileOutputStream = new FileOutputStream(file);
		        XMLWriter fileWriter = new XMLWriter(fileOutputStream, outformat);
		        fileWriter.write(document);
		        fileWriter.flush();
			}
			catch(IOException ioe)
			{
			    ioe.printStackTrace();
			}
			finally
			{
			    if(fileOutputStream != null)
			    {
			        try
			        {
			            fileOutputStream.close();
			        }
			        catch(Exception e)
			        {
			            e.printStackTrace();
			        }
			    }
			}
		}
	}
	
	
//	private static void writeDocumentToFileOld(Document document, File file)
//	{
//		if(document != null && file != null)
//		{
//			FileOutputStream fileOutputStream = null;
//			try
//			{
//		        OutputFormat outformat = OutputFormat.createPrettyPrint();
//		        outformat.setEncoding("UTF-8");
//		        StringWriter stringWriter = new StringWriter();
//		        XMLWriter xmlWriter = new XMLWriter(stringWriter, outformat);
//		        xmlWriter.write(document);
////		        xmlWriter.flush();
//		        logger.debug(stringWriter.toString());
//		        
//		        fileOutputStream = new FileOutputStream(file);
//		        XMLWriter fileWriter = new XMLWriter(fileOutputStream, outformat);
//		        fileWriter.write(document);
//		        fileWriter.flush();
//			}
//			catch(IOException ioe)
//			{
//			    ioe.printStackTrace();
//			}
//			finally
//			{
//			    if(fileOutputStream != null)
//			    {
//			        try
//			        {
//			            fileOutputStream.close();
//			        }
//			        catch(Exception e)
//			        {
//			            e.printStackTrace();
//			        }
//			    }
//			}
//		}
//	}
	
	private static void printFileElements(Collection<FileElement> fileElements)
	{
		if(fileElements != null)
		{
			for(FileElement fileElement : fileElements)
			{
				System.out.println("  " + fileElement.getValue().getPath());
			}
		}
	}
	
	private static Document addPathElement(Document document, String parentNodePath, String element, String attribute, String relativePath)
	{
		if(document != null)
		{
	        ((Element)document.selectSingleNode(parentNodePath)).addElement(element).addAttribute(attribute, OpenPatch.convertPathToForwardSlash(relativePath));
		}
		return document;
	}
	private static Document addPathElement(Document document, String parentNodePath, String element, String attribute, String relativePath, long lastModifiedTimestamp)
	{
		if(document != null)
		{
	        ((Element)document.selectSingleNode(parentNodePath)).addElement(element).addAttribute(attribute, OpenPatch.convertPathToForwardSlash(relativePath)).addAttribute(ATTRIBUTE_EXPECTED_LAST_MODIFIED, String.valueOf(lastModifiedTimestamp));
		}
		return document;
	}
	
	
	
}

