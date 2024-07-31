package me.shakedkod.lox;

import java.util.List;
import java.util.Queue;

import static me.shakedkod.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> _tokens;
    private int _current = 0;

    public Parser(List<Token> tokens) {
        _tokens = tokens;
    }

    public Expression prase()
    {
        try
        {
            return expression();
        }
        catch (ParseError error)
        {
            return null;
        }
    }

    // ----------------------- //
    //          Rules          //
    // ----------------------- //
    private Expression expression()
    {
        return ternary();
    }

    private Expression ternary()
    {
        Expression expression = equality();

        if (match(QUESTION_MARK))
        {
            if (!isComparison(expression))
                throw error(
                        previous(),
                        "There must be a boolean result at the left side of the ternary expression."
                );

            Expression ifTrue = expression();

            if (match(COLON))
            {
                Expression ifFalse = expression();
                expression = new Expression.Ternary(expression, ifTrue, ifFalse);
            }
            else
                throw error(
                        peek(),
                        "Ternary expressions require and else block that start after a ':'."
                );
        }

        return expression;
    }

    private Expression equality()
    {
        Expression expression = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL))
        {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression comparison()
    {
        Expression expression = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL))
        {
            Token operator = previous();
            Expression right = term();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression term()
    {
        Expression expression = factor();

        while (match(MINUS, PLUS))
        {
            Token operator = previous();
            Expression right = factor();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression factor()
    {
        Expression expression = unary();

        while (match(SLASH, STAR))
        {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression unary()
    {
        if (match(BANG, MINUS))
        {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }

        return primary();
    }

    private Expression primary()
    {
        if (match(FALSE)) return new Expression.Literal(false);
        if (match(TRUE)) return new Expression.Literal(true);
        if (match(NIL)) return new Expression.Literal(null);

        if (match(NUMBER, STRING))
            return new Expression.Literal(previous().getLiteral());

        if (match(LEFT_PAREN))
        {
            Expression expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        throw error(peek(), "Expect expression.");
    }

    // ----------------------------------------- //
    //          source checking methods          //
    // ----------------------------------------- //
    private Token consume(TokenType type, String message)
    {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean match(TokenType... types)
    {
        for (TokenType type : types)
            if (check(type))
            {
                advance();
                return true;
            }

        return false;
    }

    private boolean check(TokenType type)
    {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private boolean isAtEnd()
    {
        return peek().getType() == EOF;
    }

    // ---------------------------------- //
    //          moving functions          //
    // ---------------------------------- //
    private Token advance()
    {
        if (!isAtEnd()) _current++;
        return previous();
    }

    private Token peek()
    {
        return _tokens.get(_current);
    }

    private Token previous()
    {
        return _tokens.get(_current - 1);
    }

    // -------------------------------- //
    //          error handling          //
    // -------------------------------- //
    private ParseError error(Token token, String message)
    {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize()
    {
        advance();

        while (!isAtEnd())
        {
            if (previous().getType() == SEMICOLON) return;

            switch (peek().getType())
            {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    // ------------------------------- //
    //          type checking          //
    // ------------------------------- //
    private boolean isComparison(Expression expression)
    {
        if (expression instanceof Expression.Binary)
        {
            TokenType type = ((Expression.Binary) expression).getOperator().getType();
            return (type == EQUAL_EQUAL)
                    || (type == BANG_EQUAL)
                    || (type == GREATER)
                    || (type == GREATER_EQUAL)
                    || (type == LESS)
                    || (type == LESS_EQUAL);
        }
        return false;
    }
}
