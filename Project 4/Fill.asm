// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, the
// program clears the screen, i.e. writes "white" in every pixel.

// Put your code here.

// Check keyboard input

(START)
    @16384
    D = A 
    @TEMP
    M = D // Set temporary variable equal to first screen register

(CHECK)
    @24576
    D = M // Read keyboard

    @BLACK
    D ; JNE // if keboard is pressed, jump to black, otherwise, stay on white

(WHITE)
    @TEMP
    A = M // Visit memory location specified by TEMP
    M = 0 // Memory location whited
    @INCREMENT
    0 ; JMP

(BLACK)
    @TEMP
    A = M // Visit memory location specified by TEMP
    M = -1 // Memory location blacked
    @INCREMENT
    0 ; JMP

(INCREMENT)
    @TEMP 
    MD = M + 1 // Incrementing memory address

    @24576
    D = D - A 
    @START
    D ; JGE // If address is no longer valid, go back to start

    @CHECK
    0 ; JMP // Otherwise, go back to check