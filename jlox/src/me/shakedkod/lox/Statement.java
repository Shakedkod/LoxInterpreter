package me.shakedkod.lox;

import java.util.List;

abstract class Statement
{
	interface Visitor<R>
	{
		R visitBlockStatement(Block statement);
		R visitExprStatement(Expr statement);
		R visitPrintStatement(Print statement);
		R visitVarStatement(Var statement);
	}

	abstract <R> R accept(Visitor<R> visitor);

	static class Block extends Statement
	{
		private final List<Statement> _statements;

		public Block(List<Statement> statements)
		{
			_statements = statements;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitBlockStatement(this);
		}

		public List<Statement> getStatements() { return _statements; }
	}

	static class Expr extends Statement
	{
		private final Expression _expression;

		public Expr(Expression expression)
		{
			_expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitExprStatement(this);
		}

		public Expression getExpression() { return _expression; }
	}

	static class Print extends Statement
	{
		private final Expression _expression;

		public Print(Expression expression)
		{
			_expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitPrintStatement(this);
		}

		public Expression getExpression() { return _expression; }
	}

	static class Var extends Statement
	{
		private final Token _name;
		private final Expression _initializer;

		public Var(Token name, Expression initializer)
		{
			_name = name;
			_initializer = initializer;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitVarStatement(this);
		}

		public Token getName() { return _name; }
		public Expression getInitializer() { return _initializer; }
	}

}
