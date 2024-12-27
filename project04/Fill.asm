// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, 
// the screen should be cleared.

//// Replace this comment with your code.

//while(true){
//      kb = getKB()
//      if (kb > 0)
//          goto BLACK    
//      if (kb == 0)
//          goto WHITE
//      BLACK:
//          for (i=0; i<8192; i++)
//              M[i+SCREEN] = 1;
//      WHITE:
//          for (i=0; i<8192; i++)
//              M[i+SCREEN] = 0;
// }

(CHECK)
    @KBD
    D=M
    @BLACK
    D;JGT
    @WHITE
    0;JMP

(BLACK)
    @i
    M=0
    (LOOPB)
        @8192
        D=A
        @i
        D=M-D
        @CHECK
        D;JEQ

        @i
        D=M
        @SCREEN
        A=D+A
        M=-1
        
        @i
        M=M+1

        @LOOPB
        0;JMP

(WHITE)
    @i
    M=0
    (LOOPW)
        @8192
        D=A
        @i
        D=M-D
        @CHECK
        D;JEQ

        @i
        D=M
        @SCREEN
        A=D+A
        M=0
        
        @i
        M=M+1

        @LOOPW
        0;JMP
