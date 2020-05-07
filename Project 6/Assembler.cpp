#include <iostream>
#include <fstream>
#include <string>
#include <exception>
#include <unordered_map>
#include <bitset>

using namespace std;

void trim(string& line) { // Trims whitespace and comments from a line of text
    string new_line;
    for (int i = 0; i < line.length(); i++) { // For every character in the line
        switch (line[i]) {
            case ' ': break; // If it's whitespace, ignore it
            case '/':  // If it's a comment, return line up to that point
                if (i < line.length() && line[i + 1] == '/') {
                    line = new_line;
                    return;
                }
            case '\r': // If it's a carraige return, return trimmed line
                line = new_line;
                return; 
            default: new_line += line[i];
        }
    }
    line = new_line; // Case for EOF
}

void pre_parse(int& line_num, string line, unordered_map<string, int>& symTable) { // Looks for symbols in a line of text and defines them if they are not yet defined
    if (line[0] == '(') { // If identifier declaration
        line.erase(0, 1); // Remove parentheses
        line.erase(line.size() - 1, line.size());

        if (isdigit(line[0])) throw runtime_error("Invalid variable name " + line);

        unordered_map<string, int>::const_iterator it = symTable.find(line); // Search for the symbol
        if (it == symTable.end()) { // If it's not in symTable, add it
            symTable.insert(pair<string, int>(line, line_num));
        }
        else throw runtime_error("Redefinition of " + line); // If it's already in symTable, throw an error (redeclaration)
        line_num--; // Don't count line number since it's not an 'a' type or 'c' type command
    }
}

void parse(string& line, unordered_map<string, int>& symTable, int& nextRAM) { // Converts a trimmed and preparsed line of text into hack machine language
    string bin_line; // String for binary translation of hack command

    if (line[0] == '@') { // 'a' type command
        line.erase(0, 1); // remove '@'
        if (isdigit(line[0])) { // If it's a number, convert it to binary and return it
            bin_line = bitset<16>(stoi(line)).to_string();
        }
        else { // If it's an identifier, check symTable; if it's new, give it a location, otherwise, return the value tied to the identifier
            unordered_map<string, int>::const_iterator it = symTable.find(line);
            if (it == symTable.end()) {
                symTable.insert(pair<string, int>(line, nextRAM));
                bin_line = bitset<16>(nextRAM).to_string();
                nextRAM++;
            }
            else 
                bin_line = bitset<16>(it->second).to_string();
        }     
    }
    else if (line[0] == '(') { // If it's a symbol declaration, check to make sure it's in the symTable; if it's not, throw an error
        line.erase(0, 1);
        line.erase(line.size() - 1, line.size());
        unordered_map<string, int>::const_iterator it = symTable.find(line);
        if (it == symTable.end()) {
            throw logic_error("Pre-parser error, " + line + ", not found in symbol table");
        }
    }
    else { // 'c' type command
        bin_line = "111"; // Set first three digits
        bool foundAssignment = false, foundJump = false;

        for (int i = 0; i < line.length(); i++) { // Search for assignment and jump syntax
            if (line[i] == '=') foundAssignment = true;
            if (line[i] == ';') foundJump = true; 
        }

        int i = 0; 
        string second; // Variable to hold token, then binary for assignment
        if (foundAssignment) {
            while(line[i] != '=') { // Fill 'second' with token
                second += line[i];
                i++;
            }
            i++;

            if (second == "M") second = "001";
            else if (second == "D") second = "010";
            else if (second == "A") second = "100";            
            else if (second == "MD") second = "011";
            else if (second == "AM") second = "101";
            else if (second == "AD") second = "110";
            else if (second == "AMD") second = "111";
            else throw runtime_error("Invalid assignment lvalue" + second);
        }
        else second = "000";

        string first; // Variable to hold expression, then binary for expression
        string aval = "0"; // 'A' or 'M'
        while(line[i] != ';' && i < line.size()) { // Fill first with expression
            if (line[i] == 'M') aval = "1"; // Update aval if 'M' is encountered
            first += line[i];
            i++;
        }

        if (first == "0") first = "101010";
        else if (first == "1") first = "111111";
        else if (first == "D") first = "001100";      
        else if (first == "-1") first = "111010";
        else if (first == "!D") first = "001101";
        else if (first == "-D") first = "001111";
        else if (first == "D+1") first = "011111";
        else if (first == "D-1") first = "001110";
        else if (first == "A" || first == "M") first = "110000";
        else if (first == "!A" || first == "!M") first = "110001";
        else if (first == "-A" || first == "-M") first = "110011";
        else if (first == "A+1" || first == "M+1") first = "110111";        
        else if (first == "A-1" || first == "M-1") first = "110010";
        else if (first == "D+A" || first == "D+M") first = "000010";
        else if (first == "D-A" || first == "D-M") first = "010011";
        else if (first == "A-D" || first == "M-D") first = "000111";
        else if (first == "D&A" || first == "D&M") first = "000000";
        else if (first == "D|A" || first == "D|M") first = "010101";
        else throw runtime_error("Invalid operator " + first);

        if (foundJump) i++;

        string third; // Variable to hold jump token, then binary for jump
        if (foundJump) {
            while (i < line.size()) { // Fill 'third' with the rest of the trimmed line
                third += line[i];
                i++;
            }

            if (third == "JGT") third = "001";
            else if (third == "JEQ") third = "010";
            else if (third == "JGE") third = "011";
            else if (third == "JLT") third = "100";
            else if (third == "JNE") third = "101";
            else if (third == "JLE") third = "110";
            else if (third == "JMP") third = "111";
            else throw runtime_error("Invalid jump assignment" + third);
        }
        else third = "000";

        bin_line = bin_line + aval + first + second + third; // concatenate the binaries
    }

    line = bin_line; // update line with parsed value
}

int main(int argc, char *argv[]) try { // Entry point for program

    string input_filename, output_filename, input_lowercase;
    int nextRAM;
    unordered_map<string, int> symTable; // Dictionary to hold identifiers
    
    while(true) {
        nextRAM = 16; // Reset current available RAM index
        symTable.clear(); // Clear all values in symTable

        // IO FILENAME HANDLING
        //###########################################################
        if (argc > 3) throw runtime_error("Maximum two additional command line arguments");

        if (argc == 1) { // If no additional command line arguments are specified
            cout << endl;
            cout << "Please enter an input filename or enter 'Q' to quit." << endl; 
            cin >> input_filename; // Get input filename from user

            input_lowercase = input_filename; // Convert to lowercase
            for (int i = 0; i < input_filename.size(); i++)
                input_lowercase[i] = tolower(input_filename[i]); 

            if (input_lowercase == "q" || input_lowercase == "quit") break; // If exit request, end program
        }
        else
            input_filename = argv[1]; // If an additional command line argument is provided, initialize input_filename to argument

        if (input_filename.substr(input_filename.find_last_of(".") + 1) != "asm") // Make sure file extension is .asm 
            throw runtime_error("Incorrect input file extension, expected .asm"); 

        output_filename = "";
        if (argc != 3) { // If an output filename is specified by command line argument
            // Set output filename to <input filename>.hack
            for (int i = 0; i < input_filename.size(); i++) { 
                if (input_filename[i] == '.') break;
                else output_filename += input_filename[i];
            }
            output_filename += ".hack";
        }
        else // If a second additional command line argument is provided, initialize output_filename to argument 
            output_filename = argv[2];
        //###########################################################

        // SYMBOL TABLE INSERTIONS
        symTable.insert(pair<string, int>("SP", 0)); // Declaring standard identifiers
        symTable.insert(pair<string, int>("LCL", 1));
        symTable.insert(pair<string, int>("ARG", 2));
        symTable.insert(pair<string, int>("THIS", 3));
        symTable.insert(pair<string, int>("THAT", 4));
        symTable.insert(pair<string, int>("SCREEN", 16384));
        symTable.insert(pair<string, int>("KBD", 24576));
        for (int i = 0; i < 16; i++) { // R0 -> R15
            string temp = "R" + to_string(i);
            symTable.insert(pair<string, int>(temp, i));
        }   

        ifstream ifs(input_filename); // Opening input file
        if (!ifs) {
            throw runtime_error("Could not open " + input_filename);
        }

        ofstream ofs(output_filename); // Opening output file
        if (!ofs) {
            throw runtime_error("Could not open " + output_filename);
        }

        string line;
        int line_num = 0;
        while (getline(ifs, line)) { // For each line in input file, trim and pre-parse the entire file (add identifiers to symTable)
            trim(line); 
            if (line.size() > 0 && line[0] != '\r') {
                pre_parse(line_num, line, symTable);
                line_num++;
            }
        }

        ifs.close(); // Close and reopen input file to start reading from beginning of file again
        ifs.open(input_filename);

        // For each line in input file, trim line, and then parse if it is non-empty. If it is still non-empty, write machine code to output file
        while (getline(ifs, line)) { 
            trim(line);
            if (line.size() > 0 && line[0] != '\r') {
                parse(line, symTable, nextRAM);
                if (line.size() > 0)
                    ofs << line << '\n';
            }
        }

        // Close open files
        ifs.close();
        ofs.close();

        cout << "Successfully translated " << input_filename << "!" << endl;
        cout << "Output written to " << output_filename << endl;
        if (argc > 1) break;
    }
    return 0;
}
catch (exception& e) { // Exception handling
    cerr << "Error: " << e.what() << "!\n";
    return 2;
}
catch (...) {
    cerr << "Error: An unexpected error occured!\n";
    return 1;
}