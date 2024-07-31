package me.shakedkod.lox;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import static me.shakedkod.lox.TokenType.*;

public class Scanner
{
    private final String _source;
    private final List<Token> _tokens = new ArrayList<>();
    private int _start = 0;
    private int _current = 0;
    private int _line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    public Scanner(String source)
    {
        _source = source;
    }

    public List<Token> scanTokens()
    {
        while (!isAtEnd())
        {
            _start = _current;
            scanToken();
        }

        _tokens.add(new Token(EOF, "", null, _line));
        return _tokens;
    }

    private void scanToken()
    {
        char c = advance();

        switch (c)
        {
            // single character tokens
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '?': addToken(QUESTION_MARK); break;
            case ':': addToken(COLON); break;

            // double/single char tokens
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            // slashes & comments
            case '/':
                if (match('/'))
                    // a comment is from a starting point(//) to the end of the line
                    while (peek() != '\n' && !isAtEnd()) advance();
                else if (match('*'))
                    multiLineComments();
                else
                    addToken(SLASH);
                break;

            // meaningless characters
            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                _line++;
                break;

            // strings
            case '"': string(); break;

            // lexer error
            default:
                if (isDigit(c))
                    number();
                else if (isAlpha(c))
                    identifier();
                else
                    Lox.error(_line, "Unexpected character.");
                break;
        }
    }

    // -------------------------------------- //
    //          source checking func          //
    // -------------------------------------- //
    private boolean isDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c)
    {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c)
    {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAtEnd()
    {
        return _current >= _source.length();
    }

    // ------------------------------------- //
    //          token specific func          //
    // ------------------------------------- //
    // functions that are used in a specific token identifying
    private void string()
    {
        while (peek() != '"' && !isAtEnd())
        {
            if (peek() == '\n') _line++;
            advance();
        }

        if (isAtEnd())
        {
            Lox.error(_line, "Unterminated string.");
            return;
        }

        // the closing "
        advance();

        // trim the quotes
        String value = _source.substring(_start + 1, _current - 1);

        // find special symbols (added by me)
        value = value.replaceAll("\\t", "\t");
        value = value.replaceAll("\\r", "\r");
        value = value.replaceAll("\\n", "\n");

        // save token
        addToken(STRING, value);
    }

    private void multiLineComments()
    {
        while (!peek2Next().equals("*/") && !isAtEnd())
        {
            if (peek() == '\n') _line++;
            advance();
        }

        if (isAtEnd())
        {
            Lox.error(_line, "Unterminated multi-line comment.");
            return;
        }

        // advance over the * and the /
        advance();
        advance();
        advance();
    }

    private void number()
    {
        while (isDigit(peek())) advance();

        // look for a fractional part
        if (peek() == '.' && isDigit(peekNext()))
        {
            // consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(_source.substring(_start, _current)));
    }

    private void identifier()
    {
        while (isAlphaNumeric(peek())) advance();

        String text = _source.substring(_start, _current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    // ---------------------------------- //
    //          moving functions          //
    // ---------------------------------- //
    private char advance()
    {
        return _source.charAt(_current++);
    }

    private boolean match(char expected)
    {
        if (isAtEnd()) return false;
        if (_source.charAt(_current) != expected) return false;

        _current++;
        return true;
    }

    private char peek()
    {
        if (isAtEnd()) return '\0';
        return _source.charAt(_current);
    }

    private char peekNext()
    {
        if (_current + 1 >= _source.length()) return '\0';
        return _source.charAt(_current + 1);
    }

    private String peek2Next()
    {
        if (_current + 2 >= _source.length()) return "\0";
        return "" + _source.charAt(_current + 1) + _source.charAt(_current + 2);
    }

    // -------------------------------- //
    //          token handlers          //
    // -------------------------------- //
    private void addToken(TokenType type)
    {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal)
    {
        String text = _source.substring(_start, _current);
        _tokens.add(new Token(type, text, literal, _line));
    }
}
