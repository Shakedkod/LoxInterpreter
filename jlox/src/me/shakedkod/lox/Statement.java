package me.shakedkod.lox;

import java.util.List;

abstract class Statement
{
	interface Visitor<R>
	{
		R visitBlockStatement(Block statement);
		R visitClassStatement(Class statement);
		R visitExprStatement(Expr statement);
		R visitFunctionStatement(Function statement);
		R visitIfStatement(If statement);
		R visitPrintStatement(Print statement);
		R visitReturnStatement(Return statement);
		R visitVarStatement(Var statement);
		R visitWhileStatement(While statement);
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

	static class Class extends Statement
	{
		private final Token _name;
		private final Expression.Variable _superclass;
		private final List<Statement.Function> _staticMethods;
		private final List<Statement.Function> _methods;

		public Class(Token name, Expression.Variable superclass, List<Statement.Function> staticMethods, List<Statement.Function> methods)
		{
			_name = name;
			_superclass = superclass;
			_staticMethods = staticMethods;
			_methods = methods;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitClassStatement(this);
		}

		public Token getName() { return _name; }
		public Expression.Variable getSuperclass() { return _superclass; }
		public List<Statement.Function> getStaticMethods() { return _staticMethods; }
		public List<Statement.Function> getMethods() { return _methods; }
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

	static class Function extends Statement
	{
		private final Token _name;
		private final List<Token> _params;
		private final List<Statement> _body;

		public Function(Token name, List<Token> params, List<Statement> body)
		{
			_name = name;
			_params = params;
			_body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitFunctionStatement(this);
		}

		public Token getName() { return _name; }
		public List<Token> getParams() { return _params; }
		public List<Statement> getBody() { return _body; }
	}

	static class If extends Statement
	{
		private final Expression _condition;
		private final Statement _thenBranch;
		private final Statement _elseBranch;

		public If(Expression condition, Statement thenBranch, Statement elseBranch)
		{
			_condition = condition;
			_thenBranch = thenBranch;
			_elseBranch = elseBranch;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitIfStatement(this);
		}

		public Expression getCondition() { return _condition; }
		public Statement getThenBranch() { return _thenBranch; }
		public Statement getElseBranch() { return _elseBranch; }
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

	static class Return extends Statement
	{
		private final Token _keyword;
		private final Expression _value;

		public Return(Token keyword, Expression value)
		{
			_keyword = keyword;
			_value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitReturnStatement(this);
		}

		public Token getKeyword() { return _keyword; }
		public Expression getValue() { return _value; }
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

	static class While extends Statement
	{
		private final Expression _condition;
		private final Statement _body;

		public While(Expression condition, Statement body)
		{
			_condition = condition;
			_body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitWhileStatement(this);
		}

		public Expression getCondition() { return _condition; }
		public Statement getBody() { return _body; }
	}

}
