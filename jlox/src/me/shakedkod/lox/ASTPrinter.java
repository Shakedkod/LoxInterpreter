package me.shakedkod.lox;

public class ASTPrinter implements Expression.Visitor<String>
{
    String print(Expression expression)
    {
        return expression.accept(this);
    }

    @Override
    public String visitBinaryExpression(Expression.Binary expression)
    {
        return parenthesize(
                expression.getOperator().getLexeme(),
                expression.getLeft(),
                expression.getRight()
        );
    }

    @Override
    public String visitGroupingExpression(Expression.Grouping expression)
    {
        return parenthesize("group", expression.getExpression());
    }

    @Override
    public String visitLiteralExpression(Expression.Literal expression)
    {
        if (expression.getValue() == null) return "nil";
        return expression.getValue().toString();
    }

    @Override
    public String visitUnaryExpression(Expression.Unary expression)
    {
        return parenthesize(
                expression.getOperator().getLexeme(),
                expression.getRight()
        );
    }

    @Override
    public String visitTernaryExpression(Expression.Ternary expression)
    {
        return parenthesize(
                "?:",
                expression.getCondition(),
                expression.getIfTrue(),
                expression.getIfFalse()
        );
    }

    private String parenthesize(String name, Expression... expressions)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expression expression : expressions)
        {
            builder.append(" ");
            builder.append(expression.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    /*
    public static void main(String[] args)
    {
        Expression expression = new Expression.Binary(
                new Expression.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expression.Literal(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expression.Grouping(new Expression.Literal(45.67))
        );

        System.out.println(new ASTPrinter().print(expression));
    }
    */
}
