//This asm computes the modulo of two numbers
//Assuming R0 stores the number a and R1 stores the number b (b can not be 0)
//so in normal programming language, the goal is to compute RAM[R0]%RAM[R1]
//The result will be put to RAM[R2]
//Assuming RAM[R1] is positive integer and RAM[R0] is non-negative integer
//write your code here.

    @R0 // Set data equal to numerator
    D = M

    @R2 // Set result equal to zero
    M = 0

(LOOP)
    @R2
    M = D

    @R1 // Subtract denominator from numerator
    D = D - M

    @END // If < 0, end
    D ; JLT 

    @LOOP // Jump to beginning of loop
    0 ; JMP 

(END)
    @END
    0 ; JMP 