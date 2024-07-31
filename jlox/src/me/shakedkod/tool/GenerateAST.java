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
                "Binary   : Expression left, Token operator, Expression right",
                "Grouping : Expression expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expression right"
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
            writer.println("\t\tprivate final " + type + " _" + name + ";");
        }

        // constructor
        writer.println();
        writer.println("\t\tpublic " + className + "(" + fieldList + ")");
        writer.println("\t\t{");

        // store the parameters in fields
        for (String field : fields)
        {
            String name = field.split( " ")[1];
            writer.println("\t\t\t_" + name + " = " + name + ";");
        }

        writer.println("\t\t}");

        // getters
        writer.println();
        for (String field : fields)
        {
            String type = field.split(" ")[0];
            String name = field.split(" ")[1];
            String upperCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);
            writer.println("\t\tpublic " + type + " get" + upperCaseName + "() { return _" + name + "; }");
        }

        // class end
        writer.println("\t}\n");
    }
}
