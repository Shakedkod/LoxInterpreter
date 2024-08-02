package me.shakedkod.lox;

import java.util.List;

public class LoxFunction implements LoxCallable
{
    private final Environment _closure;
    private final Statement.Function _declaration;
    private final boolean _isInitializer;

    public LoxFunction(Statement.Function declaration, Environment closure, boolean isInitializer)
    {
        _closure = closure;
        _declaration = declaration;
        _isInitializer = isInitializer;
    }

    public LoxFunction bind(LoxInstance instance)
    {
        Environment environment = new Environment(_closure);
        environment.define("this", instance);
        return new LoxFunction(_declaration, environment, _isInitializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments)
    {
        Environment environment = new Environment(_closure);

        for (int i = 0; i < _declaration.getParams().size(); i++)
            environment.define(_declaration.getParams().get(i).getLexeme(), arguments.get(i));

        try
        {
            interpreter.executeBlock(_declaration.getBody(), environment);
        }
        catch (Return returnValue)
        {
            if (_isInitializer) return _closure.getAt(0, "this");
            return returnValue.getValue();
        }

        if (_isInitializer) return _closure.getAt(0, "this");
        return null;
    }

    @Override
    public int arity()
    {
        return _declaration.getParams().size();
    }

    @Override
    public String toString()
    {
        return "<fn " + _declaration.getName().getLexeme() + ">";
    }
}
