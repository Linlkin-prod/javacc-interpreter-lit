/**
 * Abstract base class for all AST nodes
 */
abstract class ASTNode {
}

/**
 * Expression nodes
 */
abstract class Expression extends ASTNode {
    abstract Object evaluate(Environment env);
}

class IntegerLiteral extends Expression {
    int value;
    
    IntegerLiteral(int value) {
        this.value = value;
    }
    
    Object evaluate(Environment env) {
        return value;
    }
}

class FloatLiteral extends Expression {
    double value;
    
    FloatLiteral(double value) {
        this.value = value;
    }
    
    Object evaluate(Environment env) {
        return value;
    }
}

class BooleanLiteral extends Expression {
    boolean value;
    
    BooleanLiteral(boolean value) {
        this.value = value;
    }
    
    Object evaluate(Environment env) {
        return value;
    }
}

class StringLiteral extends Expression {
    String value;
    
    StringLiteral(String value) {
        this.value = value;
    }
    
    Object evaluate(Environment env) {
        return value;
    }
}

class Variable extends Expression {
    String name;
    
    Variable(String name) {
        this.name = name;
    }
    
    Object evaluate(Environment env) {
        return env.getVariable(name);
    }
}

class BinaryOp extends Expression {
    Expression left;
    String operator;
    Expression right;
    
    BinaryOp(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    Object evaluate(Environment env) {
        Object leftVal = left.evaluate(env);
        Object rightVal = right.evaluate(env);
        
        if (leftVal == null || rightVal == null) {
            throw new RuntimeException("Null operand in " + operator + " operation: left=" + leftVal + ", right=" + rightVal);
        }
        
        // Arithmetic operations
        if (operator.equals("+")) {
            if (leftVal instanceof Integer && rightVal instanceof Integer) {
                return (Integer)leftVal + (Integer)rightVal;
            } else {
                double l = toDouble(leftVal);
                double r = toDouble(rightVal);
                return l + r;
            }
        } else if (operator.equals("-")) {
            if (leftVal instanceof Integer && rightVal instanceof Integer) {
                return (Integer)leftVal - (Integer)rightVal;
            } else {
                double l = toDouble(leftVal);
                double r = toDouble(rightVal);
                return l - r;
            }
        } else if (operator.equals("*")) {
            if (leftVal instanceof Integer && rightVal instanceof Integer) {
                return (Integer)leftVal * (Integer)rightVal;
            } else {
                double l = toDouble(leftVal);
                double r = toDouble(rightVal);
                return l * r;
            }
        } else if (operator.equals("/")) {
            if (leftVal instanceof Integer && rightVal instanceof Integer) {
                return (Integer)leftVal / (Integer)rightVal;
            } else {
                double l = toDouble(leftVal);
                double r = toDouble(rightVal);
                return l / r;
            }
        }
        // Comparison operations
        else if (operator.equals(">")) {
            double l = toDouble(leftVal);
            double r = toDouble(rightVal);
            return l > r;
        } else if (operator.equals("<")) {
            double l = toDouble(leftVal);
            double r = toDouble(rightVal);
            return l < r;
        } else if (operator.equals("==")) {
            return leftVal.equals(rightVal);
        } else if (operator.equals("=>")) {
            double l = toDouble(leftVal);
            double r = toDouble(rightVal);
            return l >= r;
        } else if (operator.equals("=<")) {
            double l = toDouble(leftVal);
            double r = toDouble(rightVal);
            return l <= r;
        }
        // Logical operations
        else if (operator.equals("&")) {
            return (Boolean)leftVal && (Boolean)rightVal;
        } else if (operator.equals("||")) {
            return (Boolean)leftVal || (Boolean)rightVal;
        }
        
        return null;
    }
    
    private double toDouble(Object val) {
        if (val == null) {
            throw new RuntimeException("Null value in arithmetic operation");
        }
        if (val instanceof Integer) {
            return ((Integer)val).doubleValue();
        }
        if (val instanceof Double) {
            return (Double)val;
        }
        throw new RuntimeException("Cannot convert " + val.getClass() + " to double");
    }
}

class UnaryOp extends Expression {
    String operator;
    Expression operand;
    
    UnaryOp(String operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }
    
    Object evaluate(Environment env) {
        Object val = operand.evaluate(env);
        
        if (operator.equals("!")) {
            return !(Boolean)val;
        } else if (operator.equals("-")) {
            if (val instanceof Integer) {
                return -(Integer)val;
            } else {
                return -(Double)val;
            }
        }
        
        return null;
    }
}

class FunctionCall extends Expression {
    String name;
    java.util.List<Expression> arguments;
    
    FunctionCall(String name, java.util.List<Expression> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
    
    Object evaluate(Environment env) {
        // Handle built-in functions
        if (name.equals("print")) {
            if (arguments.isEmpty()) {
                System.out.println();
            } else {
                for (int i = 0; i < arguments.size(); i++) {
                    Object val = arguments.get(i).evaluate(env);
                    System.out.print(val);
                    if (i < arguments.size() - 1) {
                        System.out.print(" ");
                    }
                }
                System.out.println();
            }
            return null;
        }
        
        FunctionDef func = env.getFunction(name);
        if (func == null) {
            throw new RuntimeException("Undefined function: " + name);
        }
        
        // Create new environment for function execution
        Environment funcEnv = new Environment();
        
        // Bind parameters
        if (func.parameters.size() != arguments.size()) {
            throw new RuntimeException("Function " + name + " expects " + func.parameters.size() + 
                                     " arguments but got " + arguments.size());
        }
        
        for (int i = 0; i < func.parameters.size(); i++) {
            Parameter param = func.parameters.get(i);
            Object argValue = arguments.get(i).evaluate(env);
            funcEnv.declareVariable(param.name, param.type, argValue);
        }
        
        // Execute function body
        for (Statement stmt : func.body) {
            stmt.execute(funcEnv);
            if (funcEnv.hasReturnValue()) {
                Object result = funcEnv.getReturnValue();
                return result;
            }
        }
        
        // No explicit return
        throw new RuntimeException("Function " + name + " did not return a value");
    }
}

/**
 * Statement nodes
 */
abstract class Statement extends ASTNode {
    abstract void execute(Environment env);
}

class Assignment extends Statement {
    String varName;
    Expression value;
    
    Assignment(String varName, Expression value) {
        this.varName = varName;
        this.value = value;
    }
    
    void execute(Environment env) {
        Object val = value.evaluate(env);
        env.setVariable(varName, val);
    }
}

class VariableDeclaration extends Statement {
    String type;
    String name;
    Expression initializer;
    
    VariableDeclaration(String type, String name, Expression initializer) {
        this.type = type;
        this.name = name;
        this.initializer = initializer;
    }
    
    void execute(Environment env) {
        Object val = (initializer != null) ? initializer.evaluate(env) : null;
        env.declareVariable(name, type, val);
    }
}

class IfStatement extends Statement {
    Expression condition;
    java.util.List<Statement> thenBlock;
    java.util.List<Statement> elseBlock;
    
    IfStatement(Expression condition, java.util.List<Statement> thenBlock, java.util.List<Statement> elseBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }
    
    void execute(Environment env) {
        boolean cond = (Boolean)condition.evaluate(env);
        if (cond) {
            for (Statement stmt : thenBlock) {
                stmt.execute(env);
            }
        } else if (elseBlock != null) {
            for (Statement stmt : elseBlock) {
                stmt.execute(env);
            }
        }
    }
}

class WhileStatement extends Statement {
    Expression condition;
    java.util.List<Statement> body;
    
    WhileStatement(Expression condition, java.util.List<Statement> body) {
        this.condition = condition;
        this.body = body;
    }
    
    void execute(Environment env) {
        while ((Boolean)condition.evaluate(env)) {
            for (Statement stmt : body) {
                stmt.execute(env);
            }
        }
    }
}

class ReturnStatement extends Statement {
    Expression value;
    
    ReturnStatement(Expression value) {
        this.value = value;
    }
    
    void execute(Environment env) {
        Object val = value.evaluate(env);
        env.setReturnValue(val);
    }
}

class ExpressionStatement extends Statement {
    Expression expr;
    
    ExpressionStatement(Expression expr) {
        this.expr = expr;
    }
    
    void execute(Environment env) {
        expr.evaluate(env);
    }
}

/**
 * Program structures
 */
class Program extends ASTNode {
    java.util.List<FunctionDef> functions;
    MainBlock main;
    
    Program(java.util.List<FunctionDef> functions, MainBlock main) {
        this.functions = functions;
        this.main = main;
    }
    
    void execute() {
        Environment env = new Environment();
        
        // Register functions
        for (FunctionDef func : functions) {
            env.defineFunction(func);
        }
        
        // Execute main
        if (main != null) {
            main.execute(env);
            
            // Print variable values for debugging
            System.out.println("\n=== Program State ===");
            for (String var : env.getVariables().keySet()) {
                Object val = env.getVariable(var);
                System.out.println(var + " = " + val);
            }
        }
    }
}

class FunctionDef extends ASTNode {
    String name;
    String returnType;
    java.util.List<Parameter> parameters;
    java.util.List<Statement> body;
    
    FunctionDef(String name, String returnType, java.util.List<Parameter> parameters, java.util.List<Statement> body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
    }
}

class Parameter extends ASTNode {
    String type;
    String name;
    
    Parameter(String type, String name) {
        this.type = type;
        this.name = name;
    }
}

class MainBlock extends ASTNode {
    java.util.List<Statement> statements;
    
    MainBlock(java.util.List<Statement> statements) {
        this.statements = statements;
    }
    
    void execute(Environment env) {
        for (Statement stmt : statements) {
            stmt.execute(env);
            if (env.hasReturnValue()) {
                break;
            }
        }
    }
}

/**
 * Environment for variable and function storage
 */
class Environment {
    java.util.Map<String, Object> variables = new java.util.HashMap<>();
    java.util.Map<String, java.util.Map<String, String>> variableTypes = new java.util.HashMap<>();
    java.util.Map<String, FunctionDef> functions = new java.util.HashMap<>();
    Object returnValue = null;
    
    void declareVariable(String name, String type, Object value) {
        variables.put(name, value);
        java.util.Map<String, String> typeMap = new java.util.HashMap<>();
        typeMap.put("type", type);
        variableTypes.put(name, typeMap);
    }
    
    void setVariable(String name, Object value) {
        if (!variables.containsKey(name)) {
            throw new RuntimeException("Undefined variable: " + name);
        }
        variables.put(name, value);
    }
    
    Object getVariable(String name) {
        if (!variables.containsKey(name)) {
            throw new RuntimeException("Undefined variable: " + name);
        }
        return variables.get(name);
    }
    
    void defineFunction(FunctionDef func) {
        functions.put(func.name, func);
    }
    
    FunctionDef getFunction(String name) {
        return functions.get(name);
    }
    
    void setReturnValue(Object value) {
        this.returnValue = value;
    }
    
    Object getReturnValue() {
        return returnValue;
    }
    
    boolean hasReturnValue() {
        return returnValue != null;
    }
    
    java.util.Map<String, Object> getVariables() {
        return variables;
    }
}
