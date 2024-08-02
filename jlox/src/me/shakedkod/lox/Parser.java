package me.shakedkod.lox;

import javax.lang.model.type.ReferenceType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.shakedkod.lox.TokenType.*;

public class Parser
{
    private static class ParseError extends RuntimeException {}

    private final List<Token> _tokens;
    private int _current = 0;

    public Parser(List<Token> tokens) {
        _tokens = tokens;
    }

    public List<Statement> parse()
    {
        List<Statement> statements = new ArrayList<>();

        while (!isAtEnd())
        {
            statements.add(declaration());
        }

        return statements;
    }

    // ------------------------------ //
    //          Declarations          //
    // ------------------------------ //
    private Statement declaration()
    {
        try
        {
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();

            return statement();
        }
        catch (ParseError error)
        {
            synchronize();
            return null;
        }
    }

    private Statement varDeclaration()
    {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expression initializer = null;
        boolean isInit = false;
        if (match(EQUAL))
        {
            initializer = expression();
            isInit = true;
        }


        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.Var(name, initializer);
    }

    private Statement classDeclaration()
    {
        Token name = consume(IDENTIFIER, "Expect class name.");

        Expression.Variable superclass = null;
        if (match(LESS))
        {
            consume(IDENTIFIER, "Expect superclass name.");
            superclass = new Expression.Variable(previous());
        }

        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Statement.Function> methods = new ArrayList<>();
        List<Statement.Function> staticMethods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd())
        {
            if (match(CLASS)) staticMethods.add(function("static"));
            else methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return new Statement.Class(name, superclass, staticMethods, methods);
    }

    private Statement.Function function(String kind)
    {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();

        if (!check(RIGHT_PAREN))
        {
            do
            {
                if (parameters.size() >= 255)
                    error(peek(), "Can't have more than 255 parameters.");
                parameters.add(
                        consume(IDENTIFIER, "Expect parameter name.")
                );
            }
            while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Statement> body = block();
        return new Statement.Function(name, parameters, body);
    }

    // ---------------------------- //
    //          Statements          //
    // ---------------------------- //
    private Statement statement()
    {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Statement.Block(block());

        return expressionStatement();
    }

    private List<Statement> block()
    {
        List<Statement> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd())
            statements.add(declaration());

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Statement returnStatement()
    {
        Token keyword = previous();
        Expression value = null;

        if (!check(SEMICOLON))
            value = expression();

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Statement.Return(keyword, value);
    }

    private Statement forStatement()
    {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Statement initializer;
        if (match(SEMICOLON))
            initializer = null;
        else if (match(VAR))
            initializer = varDeclaration();
        else
            initializer = expressionStatement();

        Expression condition = null;
        if (!check(SEMICOLON))
            condition = expression();
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expression increment = null;
        if (!check(RIGHT_PAREN))
            increment = expression();
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        Statement body = statement();

        if (increment != null)
            body = new Statement.Block(Arrays.asList(
                    body,
                    new Statement.Expr(increment)
            ));

        if (condition == null) condition = new Expression.Literal(true);
        body = new Statement.While(condition, body);

        if (initializer != null)
            body = new Statement.Block(Arrays.asList(initializer, body));

        return body;
    }

    private Statement ifStatement()
    {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(ELSE))
            elseBranch = statement();

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement whileStatement()
    {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Statement body = statement();

        return new Statement.While(condition, body);
    }

    private Statement printStatement()
    {
        Expression value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(value);
    }

    private Statement expressionStatement()
    {
        Expression expression = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Statement.Expr(expression);
    }

    // ----------------------------- //
    //          Expressions          //
    // ----------------------------- //
    private Expression expression()
    {
        return assignment();
    }

    private Expression assignment()
    {
        Expression expression = ternary();

        if (match(EQUAL))
        {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Expression.Variable)
            {
                Token name = ((Expression.Variable)expression).getName();
                return new Expression.Assign(name, value);
            }
            else if (expression instanceof Expression.Get)
            {
                Expression.Get get = (Expression.Get)expression;
                return new Expression.Set(get.getObject(), get.getName(), value);
            }

            error(equals, "Invalid assignment target."); // [no-throw]
        }

        return expression;
    }

    private Expression ternary()
    {
        Expression expression = or();

        if (match(QUESTION_MARK))
        {
            Token token = previous();
            if (!isComparison(expression))
                throw error(
                        previous(),
                        "There must be a boolean result at the left side of the ternary expression."
                );

            Expression ifTrue = expression();

            if (match(COLON))
            {
                Expression ifFalse = expression();
                expression = new Expression.Ternary(token, expression, ifTrue, ifFalse);
            }
            else
                throw error(
                        peek(),
                        "Ternary expressions require and else block that start after a ':'."
                );
        }

        return expression;
    }

    private Expression or()
    {
        Expression expression = and();

        while (match(OR))
        {
            Token operator = previous();
            Expression right = and();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression and()
    {
        Expression expression = equality();

        while (match(AND))
        {
            Token operator = previous();
            Expression right = equality();
            expression = new Expression.Logical(expression, operator, right);
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

        return call();
    }

    private Expression call()
    {
        Expression expression = primary();

        while (true)
        {
            if (match(LEFT_PAREN))
                expression = finishCall(expression);
            else if (match(DOT))
            {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expression = new Expression.Get(expression, name);
            }
            else
                break;
        }

        return expression;
    }

    private Expression primary()
    {
        if (match(FALSE)) return new Expression.Literal(false);
        if (match(TRUE)) return new Expression.Literal(true);
        if (match(NIL)) return new Expression.Literal(null);

        if (match(NUMBER, STRING))
            return new Expression.Literal(previous().getLiteral());

        if (match(SUPER))
        {
            Token keyword = previous();
            consume(DOT, "Expect '.' after 'super'.");
            Token method = consume(IDENTIFIER,
                    "Expect superclass method name.");
            return new Expression.Super(keyword, method);
        }

        if (match(THIS)) return new Expression.This(previous());

        if (match(IDENTIFIER))
            return new Expression.Variable(previous());

        if (match(LEFT_PAREN))
        {
            Expression expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        throw error(peek(), "Expect expression.");
    }

    // -------------------------------------------- //
    //          unspecified helper methods          //
    // -------------------------------------------- //
    private Expression finishCall(Expression callee)
    {
        List<Expression> arguments = new ArrayList<>();

        if (!check(RIGHT_PAREN))
        {
            do
            {
                if (arguments.size() >= 255)
                    error(peek(), "Can't have more than 255 arguments.");
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expression.Call(callee, paren, arguments);
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
        if (expression instanceof Expression.Literal)
        {
            Object value = ((Expression.Literal) expression).getValue();
            if (value instanceof Boolean)
                return true;
        }


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
