package me.shakedkod.lox;

import java.util.List;
import java.util.Map;

public class LoxClass extends LoxInstance implements LoxCallable
{
    private final String _name;
    private final Map<String, LoxFunction> _methods;
    private final Map<String, LoxFunction> _staticMethods;

    public LoxClass(String name, Map<String, LoxFunction> staticMethods, Map<String, LoxFunction> methods)
    {
        super(null);
        _name = name;
        _staticMethods = staticMethods;
        _methods = methods;
    }

    public LoxFunction findMethod(String name)
    {
        if (_methods.containsKey(name))
            return _methods.get(name);
        return null;
    }

    public LoxFunction findStaticMethod(String name)
    {
        if (_staticMethods.containsKey(name))
            return _staticMethods.get(name);
        return null;
    }

    @Override
    public Object get(Token name)
    {
        LoxFunction method = findStaticMethod(name.getLexeme());
        if (method != null) return method.bind(this);

        throw new RuntimeError(name,
                "Undefined property '" + name.getLexeme() + "'.");
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments)
    {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");

        if (initializer != null)
            initializer.bind(instance).call(interpreter, arguments);

        return instance;
    }

    @Override
    public int arity()
    {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public String toString()
    {
        return _name;
    }

    public String getName()
    {
        return _name;
    }
}
