package ca.uwaterloo.joos1wc.diagnostics;

public final class Exceptions {

  // Lexing Exceptions
  public static final String ILLEGAL_TOKEN = "Illegal token found in file at line %d, column %d as [%s]";
  public static final String TOKEN_NOT_FOUND = "Unable to find token of name \"%s\"";

  // Parser Exception
  public static final String TERMINAL_NOT_FOUND = "InvalidFileFormat: Terminal not found of name \"%s\"";
  public static final String OUT_OF_BOUNDS_STATE = "InvalidFileFormat: Referenced origin state is out-of-bounds in file \"%d\" (max %d)";
  public static final String OUT_OF_BOUNDS_SHIFT = "InvalidFileFormat: Referenced rule is out-of-bounds for reduce \"%d\" (max %d)";
  public static final String OUT_OF_BOUNDS_REDUCE = "InvalidFileFormat: Referenced state is out-of-bounds for shift \"%d\" (max %d)";

  public static final String ROOT_REDUCTION = "Encountered unexpected format by [%s]";
  public static final String SHIFT_EXPECTED = "Encountered unexpected token \"%s\" in text [%s] \nIn file at line %d, column %d";
  public static final String SHIFT_FAILURE = "Encountered unexpected token \"%s\" of type [%s] \nIn file at line %d, column %d";

  public static final String UNIMPLEMENTED_MODIFIER = "Unimplemented modifier found in weeding";
  public static final String MISSING_FILE = "File missing in token";

  // AST Exceptions
  public static final String ABSTRACT_BODY = "The abstract method %s cannot have a body\nIn file at line %d, column %d";
  public static final String FILENAME_CLASS = "The public type %s must be defined in its own file\nIn file at line %d, column %d";
  public static final String INTERFACE_FINAL_METHOD = "The interface %s cannot declare final methods\nIn file at line %d, column %d";
  public static final String INTERFACE_STATIC_METHOD = "The interface %s cannot declare static methods\nIn file at line %d, column %d";
  public static final String CLASS_CONSTRUCTOR = "The class %s must contain a constructor\nIn file at line %d, column %d";
  public static final String INTEGER_RANGE = "The literal %s is out of range\nIn file %s at line %d, column %d";
  public static final String INVALID_PROTECTED_MODIFIER = "The protected modifer is not valid on expression\nIn file at line %d, column %d";
  public static final String INVALID_STATIC_MODIFIER = "The static modifer is not valid on expression\nIn file at line %d, column %d";
  public static final String INVALID_FINAL_MODIFIER = "The final modifer is not valid on expression\nIn file at line %d, column %d";
  public static final String INVALID_NATIVE_MODIFIER = "The native modifer is not valid on expression\nIn file at line %d, column %d";
  public static final String INVALID_MODIFIER_COUNT = "Expected modifiers on expression but found none\nIn file at line %d, column %d";
  public static final String CONFLICT_ABSTRACT_FINAL = "An expression cannot be both abstract and final\nIn file at line %d, column %d";
  public static final String CONFLICT_ABSTRACT_STATIC = "An expression cannot be both abstract and static\nIn file at line %d, column %d";
  public static final String CONFLICT_STATIC_FINAL = "An expression cannot be both static and final\nIn file at line %d, column %d";
  public static final String CONFLICT_NATIVE_REQ = "The native modifier is only allowed when expression is public static\nIn file at line %d, column %d";
  public static final String CONFLICT_DUPLICATE = "An expression cannot have repeated modifiers\nIn file at line %d, column %d";
  public static final String ILLEGAL_TYPE_COMPARISON = "Illegal type comparison between '%s' and '%s'";
  public static final String ILLEGAL_COMPARISON_OPERATOR = "Illegal comparison operator '%s' on type '%s'";
  public static final String DIVIDE_BY_ZERO = "Attempt to divide by zero!";

  // Semantic analysis exceptions
  public static final String DUPLICATE_FIELD = "Duplicate field '%s'";
  public static final String DUPLICATE_LOCAL_VAR = "Duplicate local variable '%s'";
  public static final String PACKAGE_NAME_COLLISION = "Type '%s' has already been declared in package '%s'";
  public static final String PACKAGE_NOT_FOUND = "Package '%s' has not been declared";
  public static final String TYPE_NOT_FOUND = "Type '%s' not found in '%s'";
  public static final String TYPE_AS_PACKAGE_PREFIX = "Package prefix '%s' resolves to a type";
  public static final String IMPORT_COLLISION = "The import '%s' collides with another import";
  public static final String AMBIGUOUS_ON_DEMAND_IMPORT = "The type '%s' can resolve to multiple on-demand imports";
  public static final String NO_RESOLUTION = "Could not resolve '%s' to a declaration";
  public static final String PRIMITIVE_NO_RESOLUTION = "Primitive type does not have member '%s'";
  public static final String CLASS_EXTENDS_INTERFACE = "Class '%s' extends an interface '%s'";
  public static final String INTERFACE_EXTENDS_CLASS = "Interface '%s' extends a class '%s'";
  public static final String CLASS_IMPLEMENTS_CLASS = "Class '%s' implements a class '%s'";
  public static final String IMPLEMENTS_REPEATED = "Class '%s' implements interface '%s' multiple times";
  public static final String EXTENDS_REPEATED = "Interface '%s' extends interface '%s' multiple times";
  public static final String CLASS_EXTENDS_FINAL = "Class '%s' extends a final class '%s'";
  public static final String CYCLE_IN_HIERARCHY = "Cycle detected in inheritance hierarchy, ('%s' appeared in its own hierarchy)";
  public static final String METHOD_DUPLICATE = "Duplicate method '%s' found, conflicts with method of same signature";
  public static final String CONSTRUCTOR_DUPLICATE = "Duplicate constructor '%s' found, conflicts with constructor of same signature";
  public static final String STATIC_METHOD_OVERRIDEN_BY_INSTANCE = "The instance method '%s' cannot override the static method in the parent type";
  public static final String INSTANCE_METHOD_OVERRIDEN_BY_STATIC = "The static method '%s' cannot override the instance method in the parent type";
  public static final String CANNOT_REDUCE_VISIBILITY = "Cannot reduce the visibility of the inherited method '%s'";
  public static final String CANNOT_CHANGE_RETURN_TYPE = "Cannot change the return type of the inherited method '%s'";
  public static final String ABSTRACT_METHOD_CLASS = "The abstract method '%s' is not within an abstract class";
  public static final String ABSTRACT_ACCESS = "The method usage '%s' has access conflict";
  public static final String ABSTRACT_NOT_IMPLEMENTED = "The abstract method '%s' is not implemented";
  public static final String FINAL_OVERRIDEN = "A method '%s' marked final is overriden";
  public static final String CONSTRUCTOR_MISMATCH = "Constructor mismatch defined within class '%s'";
  public static final String INTERFACE_CONSTRUCT = "Cannot construct instance of interface '%s'";
  public static final String ABSTRACT_CONSTRUCT = "Cannot construct instance of abstract class '%s'";
  public static final String EXPRESSION_IS_VOID = "Expression returns void '%s'";
  public static final String CONSTRUCTOR_NOT_FOUND = "Constructor mismatch defined within class '%s'";
  public static final String METHOD_NOT_FOUND = "Method '%s('%s') not found in class '%s'";
  public static final String AMBIGUOUS_METHOD_INVOCATION = "Invocation of method '%s' is ambiguous";
  public static final String BANG_NOT_BOOLEAN = "The negation operator cannot be applied to the non-boolean '%s'";
  public static final String NEGATE_NOT_NUMERIC = "The negation operator cannot be applied to a numeric '%s'";
  public static final String BAD_OPERATOR_USAGE = "Usage of operator '%s' is invalid on types '%s' and '%s'";
  public static final String NO_DEFAULT_CONSTRUCTOR = "Superclass '%s' of class '%s' does not have default constructor";
  public static final String INSTANCEOF_PRIMITIVE = "Instanceof cannot work with primitive type '%s'";
  public static final String INSTANCEOF_INCOMPATIBLE_TYPES = "Incompatible types in instanceof expression";
  public static final String RETURN_MISTYPE = "The method '%s' expected return type '%s', encountered return of type '%s'";
  public static final String VARIABLE_ASSIGNMENT_MISMATCH = "The variable '%s' of type '%s' cannot be assigned value of type '%s'";
  public static final String THIS_STATIC_METHOD = "Static method contains usage of 'this'";
  public static final String IF_ASSIGNABLE = "An if condition requires a 'Boolean' expression, found type '%s' on condition '%s'";
  public static final String WHILE_ASSIGNABLE = "A while condition requires a 'Boolean' expression, found type '%s' on condition '%s'";
  public static final String METHOD_PROTECTED_ACCESS = "The method '%s' cannot be invoked in this context due to its access modifier.";
  public static final String CANNOT_CAST = "Cannot cast expression of type '%s' to type '%s'";
  public static final String CANNOT_INSTANCEOF = "Cannot use instanceof expression as '%s' instanceof '%s'";
  public static final String NONSTATIC_STATIC_FIELD_ACCESS = "Non-static access to static field '%s' is not allowed";
  public static final String STATIC_NONSTATIC_FIELD_ACCESS = "Static access to non-static field '%s' is not allowed";
  public static final String METHOD_RETURN_EMPTY = "The method '%s' expects value to be returned, encountered empty return statement.";
  public static final String METHOD_RETURN_VOID = "The method '%s' is of void type, encountered non-empty return statement";
  public static final String PRIMITIVE_METHOD_CALL = "Method invocation cannot occur on a primitive type '%s'.";
  public static final String FORWARD_REFERENCE = "Forward reference to field '%s'";
  public static final String EQUALITY_NOT_VOID = "Equality check requires non-void comparisons";
  public static final String EQUALITY_MISMATCH = "Equality check cannot compare types '%s' and '%s'";
  public static final String FIELD_PROTECTED_ACCESS = "The field '%s' is protected";
  public static final String TYPE_IN_PAREN = "Dot invocation cannot occur on parenthesized type '%s'";
  public static final String EXPECTED_TYPE = "Expected '%s' type for '%s', given type '%s'";
  public static final String STATIC_METHOD_ACCESS = "Cannot access static method '%s' without class qualifier";
  public static final String STATIC_REF_CONTEXT = "Cannot access non-static declaration '%s' from static context '%s'";
  public static final String FINAL_FIELD_ASSIGNMENT = "Cannot modify final field";
  public static final String LOCAL_VARIABLE_IN_OWN_INITIALIZER = "Local variable '%s' cannot be used in its own initializer";

  // static analysis
  public static final String IS_NOT_REACHABLE = "%s is not reachable";
  public static final String NON_VOID_NO_RETURN = "Non-void method '%s' can complete without a return statement";
  public static final String OWN_INITIALIZER = "'%s' appears in its own initializer";

}