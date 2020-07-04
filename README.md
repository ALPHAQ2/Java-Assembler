# Java-Assembler
					
					12 BIT Assembler created by Anunay and Tejas Dubhir

How to run :

	1 - Enter code in Input.txt

	2 - Compile with command javac *

	3 - Output would be stored in output.txt and error in error.txt

	4 - Run wiht the following command after creating .class file- "java SecondPassHandler.class"


Description of First Pass : 

	The one pass iterates over the Assembly code and checks for error . BReaks instruction into tokens and then scans each token and processes it as operation or label or operand or comment , any error found is written in the error.tx with the line of code where it is found.Checks for forward referencing of labels. BufferedReader is used to read instructions line by line from input.txt or Filereader and FileWriter is used to write into a file.
	

	Attributes : 

		1 . opcode_address = 
			 key = operation  -> value = opcode

		2 . no_of_operands = 
			operation -> no of operands it needs

		3 . can_create_var = 
			operation  -> is able to create new operand

		4 . is_error = 
			True -> error in program
			False -> no error in program

		5 . opcode_table = 
			contains oper class objects

		6 . oper =
			Attributes
				LC = location counter
				opcode_address = binary address
				operation = String name of operation

		7 . operands_table = 
			Key -> operands name
	        value =
		        index :
		            0 -> address(default = -1)
		            1 -> value(default = 0)
		            2 -> LC value

		8 . label_table :
	        Key -> label name
	        value =
	        	index :
	            	0 -> LC value (of declaration)
	            	1 onwards -> LC of labels

	Constructor :

		Hard codes all the data in opcode_address,no_of_operands,can_create_var.

	Methods :

		1 . clean(inst) : 

			param = String inst
			returns a HashTable :
			 	key :
			 		1. token -> space seperated tokens
			 		2. labels -> label tokens

		2 . isalpha(name) : 

			param = String name
			returns if string is alphanumeric or not

		3 . isdigit(name) : 

			param = String name
			return if the string is numeric or not

		4 . check_label(label)

			param = label
			 - checks label token for error
			returns flags corresponding to errors in label
			flag -> -1 = operand name is not valid.
	        flag -> 1 = operand not declared.
	        flag -> 2 = label cannot be used as a operands.
	        flag -> 3 = operation cannot be used as a operand.
	        flag -> 0 = no error found.

	    5 . check_operand(operand)

	    	param = operand
	    	 - checks operand token for errors
	    	returns flags
	    	flag -> -1 = operand name is not valid.
	        flag -> 1 = operand not declared.
	        flag -> 2 = label cannot be used as a operands.
	        flag -> 3 = operation cannot be used as a operand.
	        flag -> 0 = no error found.

	    6 . process_operand(operands , operation, location counter)

	    	processes operands
	    	return void;

	    7 . check_opcode(operation)

	    	param = operation
	    	 - checks operation token for errors
	    	returns flags

	    8 .  run(file) : 

	    	param = filereader
	    	 - runs onepass handler{
	    	 	checks every instruction for error and skips if seen.
	    	 }
	    	 output : outputs error in error.txt
	    	return void;

	Errors Handled :

		1 .  Unreachable Statement
		
		2 .  Unexpected Use of " " character
		
		3 .  Label name,operation name,operands name not valid
		
		4 .  Labels cannot be space seperated
		
		5 .  Already declared as an operand
		
		6 .  Already declared as an operation
		
		7 .  Label already declared
		
		8 .  No operation the given name
		
		9 .  Label cannot be used as an operand
		
		10 . Operation cannot be used as operand
		
		11 . Code length exceeded (255 max limit)
		
		12 . Too many operands given
		
		13 . Too less operands given

		14 . Label not declared

Description of Second Pass : 

	The second pass iterates over the input file, and checks for the operations translates it to its corresponding opcodes. Then it checks if there are any operands or not. If operands are given in the input, then it allocates the necessary address from the memory to it. If there are any branches, then the address of destination instruction is stored in the output file. Also, the program counter is set to the next value after every instruction. Then using PrintWriter class in-built in java, the output is stored in output.txt file.

	Attributes : 
		1 . same as OnePassHandler

		2 . Object Refference of OnePassHandler

	method : 
		1 . main()

			runs second pass.
