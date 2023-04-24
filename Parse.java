import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


public class Parse {
    private static FileWriter writer;
    private static Scanner scan;
    private static String line;
    private static String lexeme;
    private static final int MAX_LEXEME_LEN = 100;
    private static int charIndex;       // the index of myChar in the line
    private static char myChar;         // the current character (aka nextChar)
    private static Token charToken;     // the token of myChar (aka charClass)
    private static Token lexToken;      // the token of lexeme (aka nextToken)
    private enum Token {
        // Character tokens
        LETTER,
        DIGIT,
        SYMBOL,
        // Lexeme tokens
        IDENT,
        INT_LIT,
        ASSIGN_OP,
        ADD_OP,
        SUB_OP,
        MULT_OP,
        DIV_OP,
        SEMICOLON,
        LEFT_PAREN,
        RIGHT_PAREN,
        IF_KEYWORD,
        END_KEYWORD,
        THEN_KEYWORD,
        READ_KEYWORD,
        PRINT_KEYWORD,
        PROGRAM_KEYWORD,
        UKNOWN,
        END_OF_LINE,
        END_OF_FILE
    }


    /** Main driver **/


    public static void main(String[] args) {
        try {
            File inputFile = new File("sourceProgram.txt");
            scan = new Scanner(inputFile);
            writer = new FileWriter("parseOut.txt");
            
            println("Luke Jarvis, CSCI4200, Fall 2022, Syntax Analyzer");
            println("********************************************************************************");
            while (scan.hasNextLine())
                println(scan.nextLine());
            scan = new Scanner(inputFile);
            println("********************************************************************************");
            
            syn();
            
            scan.close();
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    } // End of main()


    // Prints output to terminal and file
    private static void println(String output) {
        System.out.println(output);
        try {
            writer.write(output+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // End of println()


    /** Lexical analyzer **/


    // Identifies a symbol token from myChar
    private static void symbol() {
        addChar();
        lexToken = switch (myChar) {
            case '(' -> Token.LEFT_PAREN;
            case ')' -> Token.RIGHT_PAREN;
            case '+' -> Token.ADD_OP;
            case '-' -> Token.SUB_OP;
            case '*' -> Token.MULT_OP;
            case '/' -> Token.DIV_OP;
            case '=' -> Token.ASSIGN_OP;
            case ';' -> Token.SEMICOLON;
            default -> Token.UKNOWN;
        };
    } // End of symbol()


    // Identifies a keyword token from lexeme
    private static void keyword() {
        switch(lexeme.toUpperCase()) {
            case "PROGRAM" -> lexToken = Token.PROGRAM_KEYWORD;
            case "PRINT" -> lexToken = Token.PRINT_KEYWORD;
            case "THEN" -> lexToken = Token.THEN_KEYWORD;
            case "READ" -> lexToken = Token.READ_KEYWORD;
            case "END" -> lexToken = Token.END_KEYWORD;
            case "IF" -> lexToken = Token.IF_KEYWORD;
        }
    } // End of keyword()


    // Adds myChar to the lexeme
    private static void addChar() {
        if (lexeme.length() < MAX_LEXEME_LEN-1)
            lexeme += myChar;
        else
            println("** Lexeme exception **\n\tLexeme length has exceeded MAX_LEXEME_LEN");
    } // End of addChar()


    // Sets myChar equal to the next character in the line (aka getChar())
    private static void setMyChar() {
        if (charIndex >= line.length()) {       // checks if charIndex exceeds line
            if (scan.hasNextLine())             // checks if there are more lines
                charToken = Token.END_OF_LINE;  // breaks conditions that check charToken
            else
                charToken = Token.END_OF_FILE;
        }
        else {
            myChar = line.charAt(charIndex++);
            // Tokenizes myChar
            if (Character.isAlphabetic(myChar))
                charToken = Token.LETTER;
            else if (Character.isDigit(myChar))
                charToken = Token.DIGIT;
            else
                charToken = Token.SYMBOL;
        }
    } // End of setMyChar()


    // Sets line to the next line
    private static void setLine() {
        line = scan.nextLine();
        charIndex = 0;
        setMyChar();
    } // End of setLine()


    // Sets myChar to the next non-blank character (aka getNonBlank())
    private static void setNonBlank() {
        while (Character.isWhitespace(myChar))
            setMyChar();
    } // End of setNonBlank()


    // Does lexical analysis
    private static void lex() {
        if (charToken == Token.END_OF_LINE)
            setLine();
        lexeme = "";
        setNonBlank();
        // Character token cases
        switch (charToken) {
            case LETTER -> {
                lexToken = Token.IDENT;
                addChar();
                setMyChar();
                while (charToken == Token.LETTER || charToken == Token.DIGIT) {
                    addChar();
                    setMyChar();
                }
            }
            case DIGIT -> {
                lexToken = Token.INT_LIT;
                addChar();
                setMyChar();
                while (charToken == Token.DIGIT) {
                    addChar();
                    setMyChar();
                }
            }
            case SYMBOL -> {
                symbol();
                setMyChar();
            }
        }
        if (lexToken == Token.UKNOWN) {
            println("** Lexeme exception **\n\tUnknown symbol: " + lexeme);
            lex();
        }
        else {
            keyword();
            println("Next token: " + lexToken);
        }
    } // End of lex()


    /** Syntax analyzer **/


    // Prints a new parse error
    private static void parseError(String... legalTokens) {
        println("** Syntax exception **" +
                "\n\tIllegal token: " + lexToken +
                "\n\tMissing token: " + String.join(", ", legalTokens));
    } // End of parseError


    // <program> -> PROGRAM <statement> { ; <statement> } END
    private static void program() {
        if (lexToken == Token.PROGRAM_KEYWORD) {
            println("Enter <program>");
            do {
                lex();
                if (lexToken == Token.IDENT || lexToken == Token.IF_KEYWORD || lexToken == Token.READ_KEYWORD || lexToken == Token.PRINT_KEYWORD)
                    statement();
            } while (lexToken == Token.SEMICOLON);
            if (lexToken != Token.END_KEYWORD)
                parseError("END_KEYWORD");
        }
        else
            parseError("PROGRAM_KEYWORD");
        println("Exit <program>");
    } // End of program()


    // <statement> -> <assign> | <selection> | <input> | <output>
    private static void statement() {
        println("Enter <statement>");
        switch (lexToken) {
            case IDENT -> {
                lex();
                assign();
            }
            case IF_KEYWORD -> {
                lex();
                selection();
            }
            case READ_KEYWORD -> {
                lex();
                input();
            }
            case PRINT_KEYWORD -> {
                lex();
                output();
            }
            default -> parseError("IDENT", "IF_KEYWORD", "READ_KEYWORD", "PRINT_KEYWORD");
        }
        println("Exit <statement>");
    } // End of statement()


    // <assign> -> IDENT = <expr>
    private static void assign() {
        println("Enter <assign>");
        if (lexToken == Token.ASSIGN_OP) {
            lex();
            expr();
        }
        else
            parseError("ASSIGN_OP");
        println("Exit <assign>");
    } // End of assign()


    // <expr> -> <term> { ( + | - ) <term> }
    private static void expr() {
        println("Enter <expr>");
        term();
        while(lexToken == Token.ADD_OP || lexToken == Token.SUB_OP) {
            lex();
            term();
        }
        println("Exit <expr>");
    } // End of expr()


    // <term> -> <factor> { ( * | / ) <factor> }
    private static void term() {
        println("Enter <term>");
        factor();
        while (lexToken == Token.MULT_OP || lexToken == Token.DIV_OP) {
            lex();
            factor();
        }
        println("Exit <term>");
    } // End of term()


    // <factor> -> IDENT | INT_LIT | (<expr>)
    private static void factor() {
        println("Enter <factor>");
        if (lexToken == Token.IDENT || lexToken == Token.INT_LIT)
            lex();
        else if (lexToken == Token.LEFT_PAREN) {
            lex();
            expr();
            if (lexToken == Token.RIGHT_PAREN)
                lex();
            else
                parseError("RIGHT_PAREN");
        }
        else
            parseError("IDENT", "INT_LIT", "LEFT_PAREN");
        println("Exit <factor>");
    } // End of factor()


    // <selection> -> if (<expr>) then <statement>
    private static void selection() {
        println("Enter <selection>");
        if (lexToken == Token.LEFT_PAREN) {
            lex();
            expr();
        }
        else
            parseError("LEFT_PAREN");
        if (lexToken == Token.RIGHT_PAREN) {
            lex();
            if (lexToken == Token.THEN_KEYWORD) {
                lex();
                statement();
            }
            else
                parseError("THEN");
        }
        else
            parseError("RIGHT_PAREN");
        println("Exit <selection>");
    } // End of selection()


    // <input> -> read (IDENT)
    private static void input() {
        println("Enter <input>");
        if (lexToken == Token.LEFT_PAREN) {
            lex();
            if (lexToken == Token.IDENT) {
                lex();
                if (lexToken == Token.RIGHT_PAREN)
                    lex();
                else
                    parseError("RIGHT_PAREN");
            } else
                parseError("IDENT");
        }
        else
            parseError("LEFT_PAREN");
        println("Exit <input>");
    } // End of input()


    // <output> -> print (<expr>)
    private static void output() {
        println("Enter <output>");
        if (lexToken == Token.LEFT_PAREN) {
            lex();
            expr();
        }
        else
            parseError("LEFT_PAREN");
        if (lexToken == Token.RIGHT_PAREN)
            lex();
        else
            parseError("RIGHT_PAREN");
        println("Exit <output>");
    } // End of output()


    // Does syntax analysis
    private static void syn() {
        setLine();
        lex();
        program();
        println("Syntax analysis of the program is complete!");
    } // End of syn()
} // End of class Parse
