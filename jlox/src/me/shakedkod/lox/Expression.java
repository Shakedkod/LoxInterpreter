package me.shakedkod.lox;

import java.util.List;

abstract class Expression
{
	static class Binary extends Expression
	{
		private final Expression _left;
		private final Token _operator;
		private final Expression _right;

		public Binary(Expression left, Token operator, Expression right)
		{
			_left = left;
			_operator = operator;
			_right = right;
		}

		public Expression getLeft() { return _left; }
		public Token getOperator() { return _operator; }
		public Expression getRight() { return _right; }
	}

	static class Grouping extends Expression
	{
		private final Expression _expression;

		public Grouping(Expression expression)
		{
			_expression = expression;
		}

		public Expression getExpression() { return _expression; }
	}

	static class Literal extends Expression
	{
		private final Object _value;

		public Literal(Object value)
		{
			_value = value;
		}

		public Object getValue() { return _value; }
	}

	static class Unary extends Expression
	{
		private final Token _operator;
		private final Expression _right;

		public Unary(Token operator, Expression right)
		{
			_operator = operator;
			_right = right;
		}

		public Token getOperator() { return _operator; }
		public Expression getRight() { return _right; }
	}

}
