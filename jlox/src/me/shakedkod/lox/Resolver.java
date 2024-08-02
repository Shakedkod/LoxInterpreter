package me.shakedkod.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void>
{
    private final Interpreter _interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    public Resolver(Interpreter interpreter)
    {
        _interpreter = interpreter;
    }

    private enum FunctionType
    {
        NONE,
        FUNCTION,
        INITIALIZER,
        METHOD
    }

    private enum ClassType
    {
        NONE,
        CLASS,
        SUBCLASS
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement)
    {
        beginScope();
        resolve(statement.getStatements());
        endScope();
        return null;
    }

    @Override
    public Void visitClassStatement(Statement.Class statement)
    {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(statement.getName());
        define(statement.getName());

        if (statement.getSuperclass() != null && statement.getName().getLexeme().equals(
                statement.getSuperclass().getName().getLexeme())
        )
            Lox.error(statement.getSuperclass().getName(),
                    "A class can't inherit from itself.");

        if (statement.getSuperclass() != null)
        {
            currentClass = ClassType.SUBCLASS;
            resolve(statement.getSuperclass());
        }

        if (statement.getSuperclass() != null)
        {
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for (Statement.Function method : statement.getMethods())
        {
            FunctionType declaration = FunctionType.METHOD;
            if (method.getName().getLexeme().equals("init"))
                declaration = FunctionType.INITIALIZER;
            resolveFunction(method, declaration);
        }

        for (Statement.Function method : statement.getStaticMethods())
        {
            FunctionType declaration = FunctionType.FUNCTION;
            resolveFunction(method, declaration);
        }

        endScope();
        if (statement.getSuperclass() != null) endScope();
        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var statement)
    {
        declare(statement.getName());

        if (statement.getInitializer() != null)
            resolve(statement.getInitializer());

        define(statement.getName());
        return null;
    }

    @Override
    public Void visitVariableExpression(Expression.Variable expression)
    {
        if (!scopes.isEmpty() && scopes.peek().get(expression.getName().getLexeme()) == Boolean.FALSE)
            Lox.error(expression.getName(),
                    "Can't read local variable in its own initializer.");

        resolveLocal(expression, expression.getName());
        return null;
    }

    @Override
    public Void visitAssignExpression(Expression.Assign expression)
    {
        resolve(expression.getValue());
        resolveLocal(expression, expression.getName());
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement)
    {
        declare(statement.getName());
        define(statement.getName());

        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitExprStatement(Statement.Expr statement)
    {
        resolve(statement.getExpression());
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement)
    {
        resolve(statement.getCondition());
        resolve(statement.getThenBranch());
        if (statement.getElseBranch() != null) resolve(statement.getElseBranch());
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        resolve(statement.getExpression());
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement)
    {
        if (currentFunction == FunctionType.NONE)
            Lox.error(statement.getKeyword(), "Can't return from top-level code.");

        if (statement.getValue() != null)
        {
            if (currentFunction == FunctionType.INITIALIZER)
                Lox.error(statement.getKeyword(),
                        "Can't return a value from an initializer.");

            resolve(statement.getValue());
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        resolve(statement.getCondition());
        resolve(statement.getBody());
        return null;
    }

    @Override
    public Void visitBinaryExpression(Expression.Binary expression) {
        resolve(expression.getLeft());
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visitCallExpression(Expression.Call expression)
    {
        resolve(expression.getCallee());

        for (Expression argument : expression.getArguments())
            resolve(argument);

        return null;
    }

    @Override
    public Void visitGetExpression(Expression.Get expression) {
        resolve(expression.getObject());
        return null;
    }

    @Override
    public Void visitGroupingExpression(Expression.Grouping expression) {
        resolve(expression.getExpression());
        return null;
    }

    @Override
    public Void visitLiteralExpression(Expression.Literal expression) {
        return null;
    }

    @Override
    public Void visitLogicalExpression(Expression.Logical expression) {
        resolve(expression.getLeft());
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visitSetExpression(Expression.Set expression)
    {
        resolve(expression.getValue());
        resolve(expression.getObject());
        return null;
    }

    @Override
    public Void visitSuperExpression(Expression.Super expression)
    {
        if (currentClass == ClassType.NONE)
            Lox.error(expression.getKeyword(),
                    "Can't use 'super' outside of a class.");
        else if (currentClass != ClassType.SUBCLASS)
            Lox.error(expression.getKeyword(),
                    "Can't use 'super' in a class with no superclass.");

        resolveLocal(expression, expression.getKeyword());
        return null;
    }

    @Override
    public Void visitThisExpression(Expression.This expression)
    {
        if (currentClass == ClassType.NONE)
        {
            Lox.error(expression.getKeyword(),
                    "Can't use 'this' outside of a class.");
            return null;
        }

        resolveLocal(expression, expression.getKeyword());
        return null;
    }

    @Override
    public Void visitUnaryExpression(Expression.Unary expression) {
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visitTernaryExpression(Expression.Ternary expression)
    {
        resolve(expression.getCondition());
        resolve(expression.getIfTrue());
        resolve(expression.getIfFalse());
        return null;
    }

    // helpers
    void resolve(List<Statement> statements)
    {
        for (Statement statement : statements)
            resolve(statement);
    }

    private void resolve(Statement statement)
    {
        statement.accept(this);
    }

    private void resolve(Expression expression)
    {
        expression.accept(this);
    }

    private void resolveLocal(Expression expression, Token name)
    {
        for (int i = scopes.size() - 1; i >=0; i--)
            if (scopes.get(i).containsKey(name.getLexeme()))
            {
                _interpreter.resolve(expression, scopes.size() - 1 - i);
                return;
            }
    }

    private void resolveFunction(Statement.Function function, FunctionType type)
    {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.getParams())
        {
            declare(param);
            define(param);
        }
        resolve(function.getBody());

        endScope();
        currentFunction = enclosingFunction;
    }

    private void beginScope()
    {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope()
    {
        scopes.pop();
    }

    private void declare(Token name)
    {
        if (scopes.isEmpty()) return;
        Map<String, Boolean> scope = scopes.peek();

        if (scope.containsKey(name.getLexeme()))
            Lox.error(name,
                    "Already a variable with this name in this scope.");

        scope.put(name.getLexeme(), false);
    }

    private void define(Token name)
    {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.getLexeme(), true);
    }
}