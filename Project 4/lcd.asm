//Using Euclidean algorithm to find the larget common divisor of two non-negative integers
//Assuming RAM[R0] stores the first integer and RAM[R1] stores the second integer
//RAM[R2] stores the result
//Write your code here

// Compare values
@R0
D = M
@R1
D = D - M

(SWAP) // If R0 < R1, Store R0 in B, R1 in A
    @NO_SWAP
    D ; JGE

    @R1
    D = M
    @A
    M = D
    @R0
    D = M
    @B
    M = D

    @LOOP // Jump to loop
    0 ; JMP

(NO_SWAP) // Else, store R0 in A, R1 in B

    @R0
    D = M
    @A
    M = D
    @R1
    D = M
    @B
    M = D

(LOOP)
    (CHECK_A)
    @A // If A is empty, return B
    D = M

    @CHECK_B
    D ; JNE // Skips if A != 0

    @B
    D = M
    @R2 // Store B in R2
    M = D

    @END // End Program
    0 ; JMP

    (CHECK_B)    
    @B // If B is empty, return A
    D = M

    @END_CHECK
    D ; JNE // Skips if B != 0

    @A
    D = M
    @R2 // Store A in R2
    M = D

    @END // End Program
    0 ; JMP

    (END_CHECK)

    @A // Set D to A
    D = M

    (MOD_LOOP) // Calculate modulus and store in mod
        @mod // Assign D to mod (initially numerator)
        M = D

        @B // Subtract denominator from numerator
        D = D - M

        @MOD_END // If < 0, end
        D ; JLT 

        @MOD_LOOP // Jump to beginning of loop
        0 ; JMP
    (MOD_END) 

    // A = B
    @B
    D = M
    @A
    M = D

    // B = Mod
    @mod
    D = M
    @B
    M = D

    @LOOP // Jump back to beginning of loop
    0; JMP

(END)
    @END
    0 ; JMP    