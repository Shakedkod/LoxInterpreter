package me.shakedkod.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void>
{
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expression, Integer> locals = new HashMap<>();

    public Interpreter()
    {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments)
            {
                return System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }

    public void interpret(List<Statement> statements)
    {
        try
        {
            for (Statement statement : statements)
                execute(statement);
        }
        catch (RuntimeError error)
        {
            Lox.runtimeError(error);
        }
    }

    private String stringify(Object object)
    {
        if (object == null) return "nil";

        if (object instanceof Double)
        {
            boolean isInt = (double)((Double) object).intValue() == (double)((Double) object);
            int dotIndex = object.toString().indexOf('.');
            String text = object.toString();
            if (isInt)
                text = text.substring(0, dotIndex);
            return text;
        }

        return object.toString();
    }

    // Functions & Classes & More
    @Override
    public Void visitClassStatement(Statement.Class statement)
    {
        environment.define(statement.getName().getLexeme(), null);

        Map<String, LoxFunction> methods = new HashMap<>();
        for (Statement.Function method : statement.getMethods())
        {
            LoxFunction function = new LoxFunction(method, environment,
                    method.getName().getLexeme().equals("init"));
            methods.put(method.getName().getLexeme(), function);
        }

        Map<String, LoxFunction> staticMethods = new HashMap<>();
        for (Statement.Function method : statement.getStaticMethods())
        {
            LoxFunction function = new LoxFunction(method, environment, false);
            staticMethods.put(method.getName().getLexeme(), function);
        }

        LoxClass klass = new LoxClass(statement.getName().getLexeme(), staticMethods, methods);
        environment.assign(statement.getName(), klass);
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement)
    {
        LoxFunction function = new LoxFunction(statement, environment, false);
        environment.define(statement.getName().getLexeme(), function);
        return null;
    }

    // STATEMENTS
    @Override
    public Void visitBlockStatement(Statement.Block statement)
    {
        executeBlock(statement.getStatements(), new Environment(environment));
        return null;
    }

    @Override
    public Void visitExprStatement(Statement.Expr statement)
    {
        if (Lox._isREPL) return visitPrintStatement(new Statement.Print(statement.getExpression()));

        evaluate(statement.getExpression());
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement)
    {
        Object value = evaluate(statement.getExpression());
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement)
    {
        Object value = null;
        if (statement.getValue() != null) value = evaluate(statement.getValue());

        throw new Return(value);
    }

    @Override
    public Void visitVarStatement(Statement.Var statement)
    {
        Object value = null;
        if (statement.getInitializer() != null)
            value = evaluate(statement.getInitializer());

        environment.define(statement.getName().getLexeme(), value);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement)
    {
        if (isTruthy(evaluate(statement.getCondition())))
            execute(statement.getThenBranch());
        else if (statement.getElseBranch() != null)
            execute(statement.getElseBranch());

        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement)
    {
        while (isTruthy(evaluate(statement.getCondition())))
            execute(statement.getBody());
        return null;
    }

    // EXPRESSIONS
    @Override
    public Object visitLiteralExpression(Expression.Literal expression)
    {
        return expression.getValue();
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression)
    {
        return evaluate(expression.getExpression());
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression)
    {
        Object right = evaluate(expression.getRight());

        switch (expression.getOperator().getType())
        {
            case MINUS:
                checkNumberOperand(expression.getOperator(), right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression)
    {
        Object left = evaluate(expression.getLeft());
        Object right = evaluate(expression.getRight());

        switch (expression.getOperator().getType())
        {
            // equality
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
            // comparison operators
            case GREATER:
                checkNumberOperands(expression.getOperator(), left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expression.getOperator(), left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expression.getOperator(), left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expression.getOperator(), left, right);
                return (double)left <= (double)right;
            // arithmetic operators
            case MINUS:
                checkNumberOperands(expression.getOperator(), left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double)left + (double)right;
                if (left instanceof String && right instanceof String)
                    return (String)left + (String)right;

                throw new RuntimeError(expression.getOperator(),
                        "Operands must be two numbers or two strings");
            case SLASH:
                checkNumberOperands(expression.getOperator(), left, right);
                if ((double)right == (double)0) throw new RuntimeError(expression.getOperator(),
                        "Dividing by 0 is not allowed.");
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expression.getOperator(), left, right);
                return (double)left * (double)right;
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitTernaryExpression(Expression.Ternary expression)
    {
        Object condition = evaluate(expression.getCondition());
        Object truthy = evaluate(expression.getIfTrue());
        Object falsy = evaluate(expression.getIfFalse());

        if ((boolean)condition)
            return truthy;
        return falsy;
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical expression)
    {
        Object left = evaluate(expression.getLeft());

        if (expression.getOperator().getType() == TokenType.OR)
        {
            if (isTruthy(left)) return left;
        }
        else
            if (!isTruthy(left)) return left;

        return evaluate(expression.getRight());
    }

    @Override
    public Object visitSetExpression(Expression.Set expression)
    {
        Object object = evaluate(expression.getObject());

        if (!(object instanceof LoxInstance))
            throw new RuntimeError(expression.getName(),
                    "Only instances have fields.");

        Object value = evaluate(expression.getValue());
        ((LoxInstance)object).set(expression.getName(), value);
        return value;
    }

    @Override
    public Object visitThisExpression(Expression.This expression)
    {
        return lookUpVariable(expression.getKeyword(), expression);
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expression)
    {
        return lookUpVariable(expression.getName(), expression);
    }

    @Override
    public Object visitAssignExpression(Expression.Assign expression)
    {
        Object value = evaluate(expression.getValue());
        Integer distance = locals.get(expression);

        if (distance != null)
            environment.assignAt(distance, expression.getName(), value);
        else
            globals.assign(expression.getName(), value);

        return value;
    }

    @Override
    public Object visitCallExpression(Expression.Call expression)
    {
        Object callee = evaluate(expression.getCallee());

        List<Object> arguments = new ArrayList<>();
        for (Expression argument : expression.getArguments())
            arguments.add(evaluate(argument));

        if (!(callee instanceof LoxCallable))
            throw new RuntimeError(expression.getParen(),
                    "Can only call functions and classes.");

        LoxCallable function = (LoxCallable)callee;
        if (arguments.size() != function.arity())
            throw new RuntimeError(expression.getParen(),
                "Expected " + function.arity() + " arguments but got " + arguments.size() + "."
            );

        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpression(Expression.Get expression)
    {
        Object object = evaluate(expression.getObject());
        if (object instanceof LoxInstance)
            return ((LoxInstance)object).get(expression.getName());


        throw new RuntimeError(expression.getName(),
                "Only instances have properties.");
    }

    //----------------------//
    //    helper methods    //
    //----------------------//
    private Object evaluate(Expression expression)
    {
        return expression.accept(this);
    }

    private void execute(Statement statement)
    {
        statement.accept(this);
    }

    public void resolve(Expression expression, int depth)
    {
        locals.put(expression, depth);
    }

    public void executeBlock(List<Statement> statements, Environment environment)
    {
        Environment previous = this.environment;
        try
        {
            this.environment = environment;

            for (Statement statement : statements)
                execute(statement);
        }
        finally {
            this.environment = previous;
        }
    }

    private boolean isTruthy(Object object)
    {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b)
    {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand)
    {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right)
    {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private Object lookUpVariable(Token name, Expression expression)
    {
        Integer distance = locals.get(expression);

        if (distance != null)
            return environment.getAt(distance, name.getLexeme());
        else
            return globals.get(name);
    }
}
