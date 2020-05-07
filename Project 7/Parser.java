import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Parser
{
	/**
	 * 
	 * @return a string corresponding to an infinite loop in hack machine language
	 */
	public static String end()
	{
		return "(END)\n" + "@END\n" + "0 ; JMP";
	}

	/**
	 * 
	 * @param inputLine  is the vm language input string to be translated
	 * @param fileName   is the vm code filename without the .vm extension (for
	 *                   naming static variables)
	 * @param lineNumber is the corresponding vm code line number (for descriptive
	 *                   error messages)
	 * @return a string of hack assembly code representing the vm code input
	 * @throws RuntimeException for general exception handling
	 */
	public static String translateLine(String inputLine, String fileName, int lineNumber) throws RuntimeException
	{
		// Format input line
		inputLine = inputLine.toLowerCase().trim();

		// If input line is empty or a comment, ignore it
		if (inputLine.length() == 0)
		{
			return "\n";
		} else if (inputLine.length() >= 2 && inputLine.startsWith("//"))
		{
			return inputLine + "\n";
		}

		// Break the input up into array of whitespace separated tokens, initialize token to the first token, and args to the rest
		String[] line = inputLine.split("\\s+");
		String token = line[0];
		String[] args = Arrays.copyOfRange(line, 1, line.length);
		
		String translation = "";
		
		// Keep track of hack assembler representation of the command types
		Map<String, String> dict = new HashMap<String, String>();
		if (line[0].equals("push") || line[0].equals("pop"))
		{
			dict.put("constant", "constant");
			dict.put("local", "LCL");
			dict.put("argument", "ARG");
			dict.put("this", "THIS");
			dict.put("that", "THAT");
			dict.put("temp", "R5");
			dict.put("pointer", "R3");
			dict.put("static", fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length()) + "." + args[1]);
		}

		
		// Verify correct number of arguments present
		if (args.length != 0 && !(token.equals("pop") || token.equals("push")))
			throw new RuntimeException("Too many arguments for " + token + " operation on line " + lineNumber);
		else if (args.length != 2 && (token.equals("pop") || token.equals("push")))
			throw new RuntimeException(
					"Incorrect number of arguments for " + token + " operation on line " + lineNumber);

		// Check other error conditions for push and pop operations and add header comment to translation
		if (token.equals("push") || token.equals("pop"))
		{
			if (token.equals("push"))
				translation = "// Push " + args[0] + " " + args[1] + "\n";
			else
				translation = "// Pop " + args[0] + " " + args[1] + "\n";

			args[0] = dict.get(args[0]);
			if (args[0] == null)
				throw new RuntimeException("Unrecognized segment identifier on line " + lineNumber);

			// If pointing to 'that', change pointer to R4
			if (args[0].equals("R3") && args[1].equals("1"))
				args[0] = "R4";
			else if (args[0].equals("R3") && !args[1].equals("0"))
				throw new RuntimeException(
						"Invalid argument for 'pointer' on line " + lineNumber + "\nValid values are 0 or 1");
		}

		switch (token)
		{
			case "push":
				translation += translatePush(args, lineNumber) + "\n";
				break;

			case "pop":
				translation += translatePop(args, lineNumber) + "\n";
				break;

			default:
				translation = translateArithmeticLogical(token, lineNumber) + "\n";
		}

		// Return translated line
		return translation;

	}

	/**
	 * 
	 * @param args       is the list of vm operation arguments
	 * @param lineNumber is the is the corresponding vm code line number (for
	 *                   descriptive error messages)
	 * @return the translated push command
	 */
	private static String translatePush(String[] args, int lineNumber)
	{
		String result = "";

		if (!args[0].equals("constant"))
		{
			// 'A' starts off at the segment value
			result += "@" + args[0] + "\n";
			// If the command is not static
			if (args[0].split("[.]").length == 1)
			{
				// Assign 'D' to either 'A' or 'M'
				if (args[0].equals("R5"))
					result += "D = A\n";
				else
					result += "D = M\n";

				// If the command is not 'pointer'
				if (!args[0].equals("R3") && !args[0].equals("R4"))
				{
					// Add the corresponding value to the base address
					result += "@" + args[1] + "\n";
					result += "A = D + A\n";
				}
			}
			// If the command is not 'pointer'
			if (!args[0].equals("R3") && !args[0].equals("R4"))
			{
				// Assign the data to the value RAM[A]
				result += "D = M\n";
			}

		} else // If pushing constant
		{
			result += "@" + args[1] + "\n";
			result += "D = A\n";
		}

		result += "@SP\n";
		result += "A = M\n";
		result += "M = D\n";
		result += "@SP\n";
		result += "M = M + 1\n";

		return result;
	}

	/**
	 * 
	 * @param args       is the list of vm operation arguments
	 * @param lineNumber is the is the corresponding vm code line number (for
	 *                   descriptive error messages)
	 * @return the translated pop command
	 */
	private static String translatePop(String[] args, int lineNumber)
	{
		if (args[0].equals("constant"))
			throw new RuntimeException("line " + lineNumber + ": cannot pop to constant");

		String result = "";

		// 'A' starts off at the segment value
		result += "@" + args[0] + "\n";

		// If the command is static, or if referencing a pointer or temp
		if (args[0].split("[.]").length > 1 || args[0].startsWith("R"))
			result += "D = A\n";
		else
			result += "D = M\n";

		// If the command is not static or 'that' add the second argument value to the base
		// address
		if (!args[0].equals("R4") && args[0].split("[.]").length == 1)
		{
			result += "@" + args[1] + "\n";
			result += "D = D + A\n";
		}

		result += "@R15\n";
		result += "M = D\n";
		result += "@SP\n";
		result += "M = M - 1\n";
		result += "A = M\n";
		result += "D = M\n";
		result += "@R15\n";
		result += "A = M\n";
		result += "M = D\n";

		return result;
	}

	/**
	 * 
	 * @param operation  is the vm operation
	 * @param lineNumber is the is the corresponding vm code line number (for
	 *                   descriptive error messages)
	 * @return the translated arithmetic or logical command
	 */
	private static String translateArithmeticLogical(String operation, int lineNumber)
	{
		String command = "";
		// Get primitive hack assembler command from the operation type
		if (operation.equals("add"))
			command = "M = M + D";
		else if (operation.equals("sub"))
			command = "M = M - D";
		else if (operation.equals("neg"))
			command = "M = -M";
		else if (operation.equals("not"))
			command = "M = !M";
		else if (operation.equals("and"))
			command = "M = M & D";
		else if (operation.equals("or"))
			command = "M = M | D";
		else if (operation.equals("lt"))
			command = "D ; JLT";
		else if (operation.equals("gt"))
			command = "D ; JGT";
		else if (operation.equals("eq"))
			command = "D ; JEQ";
		else
			throw new RuntimeException("Invalid operator on line " + lineNumber + ": " + operation);

		String result = "";
		// Create appropriate comment for the command type
		if (operation.equals("add"))
			result += "// Addition operation\n";
		else if (operation.equals("sub"))
			result += "// Subtraction operation\n";
		else if (operation.equals("neg"))
			result += "// Arithmetic negation operation\n";
		else if (operation.equals("not"))
			result += "// Logical negation operation\n";
		else if (operation.equals("and"))
			result += "// Logical and operation\n";
		else if (operation.equals("or"))
			result += "// Logical or operation\n";
		else if (operation.equals("lt"))
			result += "// Less-than operation\n";
		else if (operation.equals("gt"))
			result += "// Greater-than operation\n";
		else if (operation.equals("eq"))
			result += "// Equality operation\n";

		// Start at the stack pointer
		result += "@SP\n";

		// If operation is not unary, alter stack size
		if (!(operation.equals("not") || operation.equals("neg")))
		{
			result += "AM = M - 1\n";
			result += "D = M\n";
			result += "@SP\n";
		}

		// Assign the value of 'A' to the top of the stack
		result += "A = M - 1\n";

		// If operator is a comparator
		if (operation.equals("eq") || operation.equals("gt") || operation.equals("lt"))
		{
			result += "D = M - D\n";
			result += "@TRUE" + lineNumber + "\n";
			result += command + "\n";
			result += "@SP\n";
			result += "A = M - 1\n";
			result += "M = 0\n";
			result += "@FALSE" + lineNumber + "\n";
			result += "0 ; JMP\n";
			result += "(TRUE" + lineNumber + ")\n";
			result += "@SP\n";
			result += "A = M - 1\n";
			result += "M = -1\n";
			result += "(FALSE" + lineNumber + ")\n";

			return result;
		}

		// Do basic command and return the result
		result += command + "\n";

		return result;
	}

}
