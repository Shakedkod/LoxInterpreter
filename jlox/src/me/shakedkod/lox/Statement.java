package me.shakedkod.lox;

import java.util.List;

abstract class Statement
{
	interface Visitor<R>
	{
		R visitExprStatement(Expr statement);
		R visitPrintStatement(Print statement);
	}

	abstract <R> R accept(Visitor<R> visitor);

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

}
