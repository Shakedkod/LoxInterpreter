package me.shakedkod.lox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance
{
    private LoxClass _klass;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass klass)
    {
        _klass = klass;
    }

    public void set(Token name, Object value)
    {
        fields.put(name.getLexeme(), value);
    }

    public Object get(Token name)
    {
        if (fields.containsKey(name.getLexeme()))
            return fields.get(name.getLexeme());

        LoxFunction method = _klass.findMethod(name.getLexeme());
        if (method != null) return method.bind(this);

        throw new RuntimeError(name,
                "Undefined property '" + name.getLexeme() + "'.");
    }

    @Override
    public String toString() {
        return _klass.getName() + " instance";
    }
}
