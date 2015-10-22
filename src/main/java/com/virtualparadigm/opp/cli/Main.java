package com.virtualparadigm.opp.cli;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.virtualparadigm.opp.processor.OpenPatch;

public class Main
{
	public static final String CMD_CREATE = "create";
	public static final String CMD_EXECUTE = "execute";
	
	public static final String CMD_OPTION_LONG_NEW_STATE_DIR = "newDir";
	public static final String CMD_OPTION_LONG_OLD_STATE_DIR = "oldDir";
	public static final String CMD_OPTION_LONG_TARGET_DIR = "targetDir";
	public static final String CMD_OPTION_LONG_REGEX_EXCLUDE_FILTER = "excludeRegex";
	public static final String CMD_OPTION_LONG_GLOB_EXCLUDE_FILTER = "excludeGlob";
	public static final String CMD_OPTION_LONG_OUTPUT_DIR = "outputDir";
	public static final String CMD_OPTION_LONG_TEMP_DIR = "tempDir";
	
	public static final String CMD_OPTION_LONG_ARCHIVE_FILE_NAME = "archiveName";
	public static final String CMD_OPTION_LONG_ARCHIVE_FILE_PATH = "archivePath";
	public static final String CMD_OPTION_LONG_PATCH_DIR = "patchDir";
	
	public static final String CMD_OPTION_LONG_ROLLBACK_DIR = "rollbackDir";
	public static final String CMD_OPTION_LONG_INSERT_ONLY = "insertsOnly";
	public static final String CMD_OPTION_LONG_FORCE_UPDATE = "forceUpdates";
	
	public static void main(String[] args)
	{
		if(args.length > 0)
		{
			CommandLineParser cliParser = new BasicParser();
			CommandLine cmd = null;
			try
			{
                cmd = cliParser.parse(Main.buildCommandLineOptions(args[0]), args);
			}
			catch(ParseException pe)
			{
					throw new RuntimeException(pe);
			}
			Main.callJPatchManager(cmd, args[0]);
		}
	}
	
	public static void callJPatchManager(CommandLine cmd, String operation)
	{
		if(cmd != null)
		{
			if(CMD_CREATE.equalsIgnoreCase(operation))
			{
				OpenPatch.makePatch(
						new File(cmd.getOptionValue(CMD_OPTION_LONG_OLD_STATE_DIR)), 
						new File(cmd.getOptionValue(CMD_OPTION_LONG_NEW_STATE_DIR)), 
						new File(cmd.getOptionValue(CMD_OPTION_LONG_OUTPUT_DIR)),
						cmd.getOptionValue(CMD_OPTION_LONG_ARCHIVE_FILE_NAME));
			}
			else if(CMD_EXECUTE.equalsIgnoreCase(operation))
			{
				if(cmd.getOptionValue(CMD_OPTION_LONG_ARCHIVE_FILE_PATH) == null || cmd.getOptionValue(CMD_OPTION_LONG_ARCHIVE_FILE_PATH).length() == 0)
				{
					//no patch zip passed, so use patch root dir
					OpenPatch.executePatch(
							new File(cmd.getOptionValue(CMD_OPTION_LONG_PATCH_DIR)), 
							new File(cmd.getOptionValue(CMD_OPTION_LONG_TARGET_DIR)), 
							Main.createMatcher(cmd.getOptionValue(CMD_OPTION_LONG_REGEX_EXCLUDE_FILTER), cmd.getOptionValue(CMD_OPTION_LONG_GLOB_EXCLUDE_FILTER)), 
							(cmd.getOptionValue(CMD_OPTION_LONG_ROLLBACK_DIR) == null) ? null : new File(cmd.getOptionValue(CMD_OPTION_LONG_ROLLBACK_DIR)), 
							Boolean.valueOf(cmd.getOptionValue(CMD_OPTION_LONG_INSERT_ONLY)), 
							Boolean.valueOf(cmd.getOptionValue(CMD_OPTION_LONG_FORCE_UPDATE)));
				}
				else
				{
					//use patch zip file
					OpenPatch.executePatch(
							new File(cmd.getOptionValue(CMD_OPTION_LONG_ARCHIVE_FILE_PATH)), 
							new File(cmd.getOptionValue(CMD_OPTION_LONG_TEMP_DIR)), 
							new File(cmd.getOptionValue(CMD_OPTION_LONG_TARGET_DIR)), 
							Main.createMatcher(cmd.getOptionValue(CMD_OPTION_LONG_REGEX_EXCLUDE_FILTER), cmd.getOptionValue(CMD_OPTION_LONG_GLOB_EXCLUDE_FILTER)), 
							(cmd.getOptionValue(CMD_OPTION_LONG_ROLLBACK_DIR) == null) ? null : new File(cmd.getOptionValue(CMD_OPTION_LONG_ROLLBACK_DIR)), 
							(cmd.hasOption((CMD_OPTION_LONG_INSERT_ONLY)) ? true : false), 
							(cmd.hasOption((CMD_OPTION_LONG_FORCE_UPDATE)) ? true : false));
				}
			}
			else
			{
				System.out.println("unsupported OpenPatch operation");
			}
		}
		else
		{
			System.out.println("No command specified. Options are:");
			System.out.println("  " + CMD_CREATE);
			System.out.println("  " + CMD_EXECUTE);
		}
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
			matcher = Pattern.compile(Main.convertGlobToRegex(globFilterExpression)).matcher("");
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
	
	private static Options buildCommandLineOptions(String command)
	{
        Options cliOptions = null;
        if(CMD_CREATE.equalsIgnoreCase(command))
        {
            cliOptions = Main.buildCreatePatchCommandLineOptions();
        }
        else if(CMD_EXECUTE.equalsIgnoreCase(command))
        {
            cliOptions = Main.buildExecutePatchCommandLineOptions();
        }
        return cliOptions;
	}
	
	
	private static Options buildCreatePatchCommandLineOptions()
	{
		Options cliOptions = new Options();
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_OLD_STATE_DIR);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_OLD_STATE_DIR);
		OptionBuilder.withDescription("old state directory");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(true);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_OLD_STATE_DIR));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_NEW_STATE_DIR);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_NEW_STATE_DIR);
		OptionBuilder.withDescription("new state directory");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(true);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_NEW_STATE_DIR));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_OUTPUT_DIR);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_OUTPUT_DIR);
		OptionBuilder.withDescription("output directory");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(true);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_OUTPUT_DIR));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_ARCHIVE_FILE_NAME);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_ARCHIVE_FILE_NAME);
		OptionBuilder.withDescription("archive file name");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(false);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_ARCHIVE_FILE_NAME));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_ARCHIVE_FILE_PATH);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_ARCHIVE_FILE_PATH);
		OptionBuilder.withDescription("archive file path");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(false);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_ARCHIVE_FILE_PATH));
		
		return cliOptions;
		
	}
	
	private static Options buildExecutePatchCommandLineOptions()
	{
		Options cliOptions = new Options();
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_PATCH_DIR);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_PATCH_DIR);
		OptionBuilder.withDescription("patch root dir");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(false);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_PATCH_DIR));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_ARCHIVE_FILE_PATH);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_ARCHIVE_FILE_PATH);
		OptionBuilder.withDescription("archive file path");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(false);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_ARCHIVE_FILE_PATH));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_TEMP_DIR);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_TEMP_DIR);
		OptionBuilder.withDescription("temp directory");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(false);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_TEMP_DIR));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_TARGET_DIR);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_TARGET_DIR);
		OptionBuilder.withDescription("target directory");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(true);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_TARGET_DIR));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_REGEX_EXCLUDE_FILTER);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_REGEX_EXCLUDE_FILTER);
		OptionBuilder.withDescription("regular expression exclude filter");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(false);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_REGEX_EXCLUDE_FILTER));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_GLOB_EXCLUDE_FILTER);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_GLOB_EXCLUDE_FILTER);
		OptionBuilder.withDescription("wildcard exclude filter");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(false);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_GLOB_EXCLUDE_FILTER));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_ROLLBACK_DIR);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_ROLLBACK_DIR);
		OptionBuilder.withDescription("rollback directory");
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(false);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_ROLLBACK_DIR));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_INSERT_ONLY);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_INSERT_ONLY);
		OptionBuilder.withDescription("inserts only");
		OptionBuilder.hasArg(false);
		OptionBuilder.isRequired(false);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_INSERT_ONLY));
		
		OptionBuilder.withArgName(CMD_OPTION_LONG_FORCE_UPDATE);
		OptionBuilder.withLongOpt(CMD_OPTION_LONG_FORCE_UPDATE);
		OptionBuilder.withDescription("force updates");
		OptionBuilder.hasArg(false);
		OptionBuilder.isRequired(false);
		cliOptions.addOption(OptionBuilder.create(CMD_OPTION_LONG_FORCE_UPDATE));
		
		return cliOptions;
	}	

}