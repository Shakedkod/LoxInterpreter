package me.shakedkod.lox;

import java.util.List;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void>
{
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

    // STATEMENTS
    @Override
    public Void visitExprStatement(Statement.Expr statement)
    {
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
}
