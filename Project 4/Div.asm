// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/div.asm

// Divides R0 by R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.

    @R0 // Set data equal to numerator
    D = M

    @R2 // Set result equal to zero
    M = 0

(LOOP)
    @R1 // Subtract denominator from numerator
    D = D - M

    @END // If < 0, end
    D ; JLT 

    @R2 // Add one to result
    M = M + 1

    @LOOP // Jump to beginning of loop
    0 ; JMP 

(END)
    @END
    0 ; JMP 