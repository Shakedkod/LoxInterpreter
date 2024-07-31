package me.shakedkod.lox;

public class Token
{
    private final TokenType _type;
    private final String    _lexeme;
    private final Object    _literal;
    private final int       _line;

    public Token(TokenType type, String lexeme, Object literal, int line)
    {
        _type = type;
        _lexeme = lexeme;
        _literal = literal;
        _line = line;
    }

    public String toString()
    {
        return _type + " " + _lexeme + " " + _literal;
    }

    public TokenType getType()    { return _type;    }
    public String    getLexeme()  { return _lexeme;  }
    public Object    getLiteral() { return _literal; }
    public int       getLine()    { return _line;    }
}
