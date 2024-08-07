package me.shakedkod.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAST
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        String  outputDir = args[0];
        defineAST(outputDir, "Expression", Arrays.asList(
                "Assign   : Token name, Expression value",
                "Binary   : Expression left, Token operator, Expression right",
                "Call     : Expression callee, Token paren, List<Expression> arguments",
                "Get      : Expression object, Token name",
                "Grouping : Expression expression",
                "Literal  : Object value",
                "Logical  : Expression left, Token operator, Expression right",
                "Set      : Expression object, Token name, Expression value",
                "Super    : Token keyword, Token method",
                "This     : Token keyword",
                "Unary    : Token operator, Expression right",
                "Ternary  : Token operator, Expression condition, Expression ifTrue, Expression ifFalse",
                "Variable : Token name"
        ));
        defineAST(outputDir, "Statement", Arrays.asList(
                "Block    : List<Statement> statements",
                "Class    : Token name, Expression.Variable superclass, List<Statement.Function> staticMethods, " +
                          "List<Statement.Function> methods",
                "Expr     : Expression expression",
                "Function : Token name, List<Token> params, List<Statement> body",
                "If       : Expression condition, Statement thenBranch, Statement elseBranch",
                "Print    : Expression expression",
                "Return   : Token keyword, Expression value",
                "Var      : Token name, Expression initializer",
                "While    : Expression condition, Statement body"
        ));
    }

    private static void defineAST(String outputDir, String baseName, List<String> types) throws IOException
    {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package me.shakedkod.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName);
        writer.println("{");

        defineVisitor(writer, baseName, types);

        // The base accept() method.
        writer.println();
        writer.println("\tabstract <R> R accept(Visitor<R> visitor);");

        // The AST classes.
        writer.println();
        for (String type : types)
        {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList)
    {
        writer.println("\tstatic class "+ className + " extends " + baseName);
        writer.println("\t{");

        // Fields
        String[] fields = fieldList.split(", ");
        for (String field : fields)
        {
            String type = field.split(" ")[0];
            String name = field.split(" ")[1];

            if (type.startsWith("!"))
            {
                type = type.substring(1);
                writer.println("\t\tprivate " + type + " _" + name + " = false;");
            }
            else
                writer.println("\t\tprivate final " + type + " _" + name + ";");
        }

        // constructor
        writer.println();

        boolean isNotFinal = false;
        for (String field : fields)
        {
            String type = field.split(" ")[0];
            if (type.startsWith("!"))
            {
                isNotFinal = true;
                break;
            }
        }

        if (!isNotFinal)
            writer.println("\t\tpublic " + className + "(" + fieldList + ")");
        else
        {
            writer.print("\t\tpublic " + className + "(");
            for (int i = 0; i < fields.length; i++)
            {
                String field = fields[i];
                if (field.startsWith("!"))
                    field = field.substring(1);
                String type = field.split(" ")[0];
                String name = field.split(" ")[1];
                if (i == fields.length - 1)
                    writer.print(type + " " + name + ")");
                else
                    writer.print(type + " " + name + ", ");
            }
            writer.println();
        }
        writer.println("\t\t{");

        // store the parameters in fields
        for (String field : fields)
        {
            String name = field.split( " ")[1];
            writer.println("\t\t\t_" + name + " = " + name + ";");
        }

        writer.println("\t\t}");

        // Visitor pattern.
        writer.println();
        writer.println("\t\t@Override");
        writer.println("\t\t<R> R accept(Visitor<R> visitor)");
        writer.println("\t\t{");
        writer.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
        writer.println("\t\t}");

        // getters
        writer.println();
        for (String field : fields)
        {
            String type = field.split(" ")[0];
            String name = field.split(" ")[1];
            String upperCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);

            if (type.startsWith("!"))
            {
                type = type.substring(1);
                writer.println("\t\tpublic void set" + upperCaseName + "() { _" + name + " = true; }");
                writer.println("\t\tpublic boolean " + name + "() { return _" + name + "; }");
            }
            else
                writer.println("\t\tpublic " + type + " get" + upperCaseName + "() { return _" + name + "; }");
        }

        // class end
        writer.println("\t}\n");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types)
    {
        writer.println("\tinterface Visitor<R>");
        writer.println("\t{");

        for (String type : types)
        {
            String typeName = type.split(":")[0].trim();
            writer.println("\t\tR visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("\t}");
    }
}
