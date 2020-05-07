// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Do multiplication of R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.

@R2 // Set R2 equal to 0
M = 0

@counter
M = 0

@7 // Position of MSB of input
   // (Can work with inputs up to 15 bits, but does not complete in the given testing interval)
D = A
@counter // Set the counter equal to the MSB position
M = D

(LOOP)
    @counter // Load the value in counter
    D = M
    @END // End if counter is less than 0
    D ; JLT

    // Store 1 left-shifted counter times in D 
    D = 1 < D
    
    @R1 // Store R1 & D in D
    D = M & D

    (IF)
        @ENDIF // Jump to end if R1 & 1 << count is LEQ 0
        D ; JLE

        @counter // Store the counter in D
        D = M

        @R0 // Store R0 << counter in D
        D = M < D

        @R2 // Add R0 << counter to R2
        M = M + D
        (ENDIF)
    
    @counter // Decrement Counter
    M = M - 1

    @LOOP // Return to beginning
    0 ; JMP

(END)
    @END
    0 ; JMP