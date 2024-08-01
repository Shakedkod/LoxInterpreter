package me.shakedkod.lox;

import java.util.List;

abstract class Expression
{
	interface Visitor<R>
	{
		R visitBinaryExpression(Binary expression);
		R visitGroupingExpression(Grouping expression);
		R visitLiteralExpression(Literal expression);
		R visitUnaryExpression(Unary expression);
		R visitTernaryExpression(Ternary expression);
	}

	abstract <R> R accept(Visitor<R> visitor);

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

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitBinaryExpression(this);
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

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitGroupingExpression(this);
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

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitLiteralExpression(this);
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

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitUnaryExpression(this);
		}

		public Token getOperator() { return _operator; }
		public Expression getRight() { return _right; }
	}

	static class Ternary extends Expression
	{
		private final Token _operator;
		private final Expression _condition;
		private final Expression _ifTrue;
		private final Expression _ifFalse;

		public Ternary(Token operator, Expression condition, Expression ifTrue, Expression ifFalse)
		{
			_operator = operator;
			_condition = condition;
			_ifTrue = ifTrue;
			_ifFalse = ifFalse;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitTernaryExpression(this);
		}

		public Token getOperator() { return _operator; }
		public Expression getCondition() { return _condition; }
		public Expression getIfTrue() { return _ifTrue; }
		public Expression getIfFalse() { return _ifFalse; }
	}

}
