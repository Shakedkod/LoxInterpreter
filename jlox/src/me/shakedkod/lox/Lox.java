package me.shakedkod.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox
{
    private static final Interpreter interpreter = new Interpreter();
    static boolean _isREPL = false;
    static boolean _hadError = false;
    static boolean _hadRuntimeError = false;

    public static void main(String[] args) throws IOException
    {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1)
            runFile(args[0]);
        else
            runPrompt();
    }

    public static void runFile(String path) throws IOException
    {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (_hadError) System.exit(65);
        if (_hadRuntimeError) System.exit(70);
    }

    public static void runPrompt() throws IOException
    {
        _isREPL = true;
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            String line = reader.readLine();

            if (line == null)
                break;

            run(line);
            _hadError = false;
        }
    }

    private static void run(String source)
    {
        // Scanning / Lexing
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // Parsing
        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        // For debugging -> print Tokens
        //for (Token token : tokens)
        //    System.out.println(token);

        // Resolving
        if (_hadError) return;
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        // Interpreting
        if (_hadError) return;
        interpreter.interpret(statements);
    }

    // ERROR HANDLING
    static void error(int line, String message)
    {
        report(line, "", message);
    }

    static void error(Token token, String message)
    {
        if (token.getType() == TokenType.EOF)
            report(token.getLine(), " at end", message);
        else
            report(token.getLine(), " at '" + token.getLexeme() + "'", message);
    }

    static void runtimeError(RuntimeError error)
    {
        System.err.println(error.getMessage() +
                "\n[line " + error.getToken().getLine() + "]");
        _hadRuntimeError = true;
    }

    private static void report(int line, String where, String message)
    {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message
        );
        _hadError = true;
    }
}
