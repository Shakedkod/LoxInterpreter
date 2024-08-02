package me.shakedkod.lox;

import java.util.List;

abstract class Expression
{
	interface Visitor<R>
	{
		R visitAssignExpression(Assign expression);
		R visitBinaryExpression(Binary expression);
		R visitCallExpression(Call expression);
		R visitGetExpression(Get expression);
		R visitGroupingExpression(Grouping expression);
		R visitLiteralExpression(Literal expression);
		R visitLogicalExpression(Logical expression);
		R visitSetExpression(Set expression);
		R visitThisExpression(This expression);
		R visitUnaryExpression(Unary expression);
		R visitTernaryExpression(Ternary expression);
		R visitVariableExpression(Variable expression);
	}

	abstract <R> R accept(Visitor<R> visitor);

	static class Assign extends Expression
	{
		private final Token _name;
		private final Expression _value;

		public Assign(Token name, Expression value)
		{
			_name = name;
			_value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitAssignExpression(this);
		}

		public Token getName() { return _name; }
		public Expression getValue() { return _value; }
	}

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

	static class Call extends Expression
	{
		private final Expression _callee;
		private final Token _paren;
		private final List<Expression> _arguments;

		public Call(Expression callee, Token paren, List<Expression> arguments)
		{
			_callee = callee;
			_paren = paren;
			_arguments = arguments;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitCallExpression(this);
		}

		public Expression getCallee() { return _callee; }
		public Token getParen() { return _paren; }
		public List<Expression> getArguments() { return _arguments; }
	}

	static class Get extends Expression
	{
		private final Expression _object;
		private final Token _name;

		public Get(Expression object, Token name)
		{
			_object = object;
			_name = name;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitGetExpression(this);
		}

		public Expression getObject() { return _object; }
		public Token getName() { return _name; }
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

	static class Logical extends Expression
	{
		private final Expression _left;
		private final Token _operator;
		private final Expression _right;

		public Logical(Expression left, Token operator, Expression right)
		{
			_left = left;
			_operator = operator;
			_right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitLogicalExpression(this);
		}

		public Expression getLeft() { return _left; }
		public Token getOperator() { return _operator; }
		public Expression getRight() { return _right; }
	}

	static class Set extends Expression
	{
		private final Expression _object;
		private final Token _name;
		private final Expression _value;

		public Set(Expression object, Token name, Expression value)
		{
			_object = object;
			_name = name;
			_value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitSetExpression(this);
		}

		public Expression getObject() { return _object; }
		public Token getName() { return _name; }
		public Expression getValue() { return _value; }
	}

	static class This extends Expression
	{
		private final Token _keyword;

		public This(Token keyword)
		{
			_keyword = keyword;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitThisExpression(this);
		}

		public Token getKeyword() { return _keyword; }
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

	static class Variable extends Expression
	{
		private final Token _name;

		public Variable(Token name)
		{
			_name = name;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitVariableExpression(this);
		}

		public Token getName() { return _name; }
	}

}
