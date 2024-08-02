package me.shakedkod.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment
{
    private final Environment _enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment()
    {
        _enclosing = null;
    }

    public Environment(Environment enclosing)
    {
        _enclosing = enclosing;
    }

    public void define(String name, Object value)
    {
        values.put(name, value);
    }

    public Object get(Token name)
    {
        if (values.containsKey(name.getLexeme()))
            return values.get(name.getLexeme());

        if (_enclosing != null) return _enclosing.get(name);

        throw new RuntimeError(name,
                "Undefined variable '" + name.getLexeme() + "'.");
    }

    public Object getAt(int distance, String name)
    {
        return ancestor(distance).values.get(name);
    }

    Environment ancestor(int distance)
    {
        Environment environment = this;

        for (int i = 0; i < distance; i++)
            environment = environment.getEnclosing();

        return environment;
    }

    public void assign(Token name, Object value)
    {
        if (values.containsKey(name.getLexeme()))
        {
            values.put(name.getLexeme(), value);
            return;
        }

        if (_enclosing != null)
        {
            _enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.getLexeme() + "'.");
    }

    public void assignAt(int distance, Token name, Object value)
    {
        ancestor(distance).values.put(name.getLexeme(), value);
    }

    // getters
    public Environment getEnclosing()
    {
        return _enclosing;
    }
}
