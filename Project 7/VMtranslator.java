import java.io.*;

public class VMtranslator
{

	public static void main(String[] args)
	{
		try
		{
			// Verifying command line arguments for form validity
			if (args.length == 0)
			{
				throw new RuntimeException("No input file specified");
			} else if (args.length > 1)
			{
				throw new RuntimeException("Too many input files specified");
			} else if (args[0].split("[.]").length != 2 || !args[0].split("[.]")[1].equals("vm"))
			{
				throw new RuntimeException("Incorrect file format or invalid '.' character in filename");
			}

			// Setting output filename
			String outFilename = args[0].split("[.]")[0] + ".asm";

			// Opening input and output files
			BufferedReader inputFile = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
			BufferedWriter outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFilename)));

			// Read file line-by-line and output resulting hack assembler code
			String line = inputFile.readLine();
			for (int lineNumber = 1; line != null; line = inputFile.readLine(), lineNumber++)
			{
				outputFile.write(Parser.translateLine(line, args[0].split("[.]")[0], lineNumber));
			}
			// Write infinite loop to end of output file
			outputFile.write(Parser.end());
			
			System.out.println("Successfully translated " + args[0] + "!");
			System.out.println("Result written to " + outFilename);
			// Close open files
			inputFile.close();
			outputFile.close();

		} catch (Exception e)
		{
			System.out.println("ERROR: " + e + "!");
			return;
		}
	}

}
