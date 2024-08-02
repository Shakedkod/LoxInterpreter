package me.shakedkod.lox;

public class Return extends RuntimeException
{
    private final Object _value;

    Return(Object value)
    {
        super(null, null, false, false);
        _value = value;
    }

    public Object getValue()
    {
        return _value;
    }
}
