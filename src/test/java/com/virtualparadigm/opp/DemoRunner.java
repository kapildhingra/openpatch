package com.virtualparadigm.opp;

public class DemoRunner
{	
	public static void main(String[] args)
	{
		if(args != null)
		{
			for(String arg : args)
			{
				if("test1".equalsIgnoreCase(arg))
				{
					JPatchManagerTest1.execute();
				}
				else if("test2".equalsIgnoreCase(arg))
				{
					JPatchManagerTest2.execute();
				}
				else if("test3".equalsIgnoreCase(arg))
				{
					JPatchManagerTest3.execute();
				}
				else if("test4".equalsIgnoreCase(arg))
				{
					JPatchManagerTest4.execute();
				}
				else
				{
					System.out.println("could not find that test.");
				}
			}
		}
		
//        Options cliOptions = new Options();
//        
//        cliOptions.addOption(
//            OptionBuilder
//                .withArgName(CMD_OPTION_SHORT_INPUT_FILE)
//                .withLongOpt(CMD_OPTION_LONG_INPUT_FILE)
//                .withDescription("input file name")
//                .hasOptionalArgs(10)
//                .isRequired(true)
//                .create(CMD_OPTION_SHORT_INPUT_FILE));
//        
//        cliOptions.addOption(
//            OptionBuilder
//                .withArgName(CMD_OPTION_SHORT_OUTPUT_FILE)
//                .withLongOpt(CMD_OPTION_LONG_OUTPUT_FILE)
//                .withDescription("output file name")
//                .hasArg(true)
//                .isRequired(false)
//                .create(CMD_OPTION_SHORT_INPUT_FILE));
//        
//        CommandLineParser cliParser = new BasicParser();
//        CommandLine cmd = null;
//        try
//        {
//            cmd = cliParser.parse(cliOptions, args);
//        }
//        catch(ParseException pe)
//        {
//            throw new RuntimeException(pe);
//        }
//        if(cmd != null)
//        {
//        	cmd.get
////            String inputFilePath = cmd.getOptionValue(CMD_OPTION_LONG_INPUT_FILE);
//            String[] inputFilePaths = cmd.getOptionValues(CMD_OPTION_LONG_INPUT_FILE);
//            String outputFilePath = cmd.getOptionValue(CMD_OPTION_LONG_OUTPUT_FILE);
//            
//            
//            
//            System.out.println("done.");
//        }
		
		
		
		
		
		
	}
	
	
	
	
}