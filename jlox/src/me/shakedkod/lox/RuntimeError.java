package me.shakedkod.lox;

public class RuntimeError extends RuntimeException
{
    private final Token _token;

    public RuntimeError(Token token, String message)
    {
        super(message);
        _token = token;
    }

    // getter
    public Token getToken()
    {
        return _token;
    }
}
