package ca.uwaterloo.joos1wc.parse;

import java.io.File;
import java.util.List;

import ca.uwaterloo.joos1wc.ast.Modifier;
import ca.uwaterloo.joos1wc.ast.RecursiveVisitor;
import ca.uwaterloo.joos1wc.ast.declaration.BodyDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.literal.IntLiteral;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.diagnostics.ValidationException;

public class Weeder extends RecursiveVisitor {

  public Weeder() {
  }

  protected void preVisit(IntLiteral node) {
    if (!validateInt(node.getLiteralValue(), node.isNegative())) {
      File file = node.token.getFile();
      int lineNum = node.token.getLineNumber();
      int colNum = node.token.getPosition();
      throw new ValidationException(String.format(Exceptions.INTEGER_RANGE, node.token.getImage(), file.getName(), lineNum, colNum));
    }
  }

  protected void preVisit(FieldDeclaration node) {
    int lineNum = node.token.getLineNumber();
    int colNum = node.token.getPosition();
    validateModifiers(node.modifiers, true, true, false, true, true, lineNum, colNum);
  }

  protected void preVisit(MethodDeclaration node) {
    int lineNum = node.token.getLineNumber();
    int colNum = node.token.getPosition();
    boolean isAbstract = validateModifiers(node.modifiers, true, true, true, true, false, lineNum, colNum);

    // validate that there is no body on an abstract method
    if (isAbstract) {
      if (node.body != null) {
        throw new ValidationException(String.format(Exceptions.ABSTRACT_BODY, node.name, node.token.getLineNumber(),
            node.token.getPosition()));
      }
    }
  }

  @Override
  protected void preVisit(TypeDeclaration node) {
    int lineNum = node.token.getLineNumber();
    int colNum = node.token.getPosition();

    boolean isAbstract = validateModifiers(node.modifiers, !(node.isClass || node.isInterface), false, node.isClass,
        node.isClass, false, lineNum, colNum);

    // some checks that only apply to classes and interfaces
    if (node.isClass || node.isInterface) {

      // first check to see if the filename matches the public declaration
      File sourceFile = node.token.getFile();
      if (sourceFile == null) {
        throw new ValidationException(String.format(Exceptions.MISSING_FILE, lineNum, colNum));
      }
      String fileName = sourceFile.getName();
      String className = node.name + ".java";

      // make sure the first part of the file name matches and the extension is .java
      char[] fileChars = fileName.toCharArray();
      char[] classChars = className.toCharArray();

      for (int i = 0; i < fileChars.length; i++) {
        if (i >= classChars.length || fileChars[i] != classChars[i]) {
          throw new ValidationException(String.format(Exceptions.FILENAME_CLASS, className, lineNum, colNum));
        }
      }

      // then check to see that the methods meet requirements depending on whether this is
      // a class or interface
      boolean hasConstructor = false;
      boolean hasStaticNative = false;

      for (BodyDeclaration declaration : node.getBody()) {
        // only applies for methods
        if (declaration instanceof MethodDeclaration) {
          MethodDeclaration method = (MethodDeclaration) declaration;
          boolean hasStatic = false;
          boolean hasNative = false;

          // but the modifiers are available from the abstract BodyDeclaration class
          for (Modifier modifier : declaration.modifiers) {
            // interfaces cannot have final or static methods
            if (node.isInterface) {
              switch (modifier.keyword) {
              case FINAL:
                throw new ValidationException(String.format(Exceptions.INTERFACE_FINAL_METHOD, className, lineNum,
                    colNum));
              case STATIC:
                throw new ValidationException(String.format(Exceptions.INTERFACE_STATIC_METHOD, className, lineNum,
                    colNum));
              default:
              }
            } else {
              switch (modifier.keyword) {
              case NATIVE:
                hasNative = true;
              case STATIC:
                hasStatic = true;
              default:
              }
            }
          }

          if (hasNative && hasStatic) {
            hasStaticNative = true;
          }
          if (method.isConstructor) {
            hasConstructor = true;
          }
        }
      }

      if (node.isClass && !hasConstructor && !hasStaticNative && !isAbstract) {
        throw new ValidationException(String.format(Exceptions.CLASS_CONSTRUCTOR, className, lineNum, colNum));
      }
    }
  }

  protected Boolean validateInt(byte[] value, boolean negative) {
    if (!negative && (value[0] & 0x80) != 0) {
      return false;
    }
    return true;
  }

  /**
   * 
   * @param modifiers
   * @param protectedAllowed
   * @param staticAllowed
   * @param finalAllowed
   * @param nativeAllowed
   * @param staticFinalAllowed
   * @return if the modifiers included the ABSTRACT keyword
   * @throws ValidationException
   */
  protected boolean validateModifiers(List<Modifier> modifiers, boolean protectedAllowed, boolean staticAllowed,
      boolean finalAllowed, boolean nativeAllowed, boolean staticFinalAllowed, int lineNum, int colNum) {

    int publicCount = 0;
    int protectedCount = 0;
    int staticCount = 0;
    int finalCount = 0;
    int abstractCount = 0;
    int nativeCount = 0;

    for (Modifier modifier : modifiers) {
      switch (modifier.keyword) {
      case PUBLIC:
        publicCount++;
        break;
      case PROTECTED:
        protectedCount++;
        break;
      case STATIC:
        staticCount++;
        break;
      case FINAL:
        finalCount++;
        break;
      case ABSTRACT:
        abstractCount++;
        break;
      case NATIVE:
        nativeCount++;
        break;
      default:
        throw new UnsupportedOperationException(Exceptions.UNIMPLEMENTED_MODIFIER);
      }
    }

    if (!protectedAllowed && protectedCount > 0) {
      throw new ValidationException(String.format(Exceptions.INVALID_PROTECTED_MODIFIER, lineNum, colNum));
    }
    if (!staticAllowed && staticCount > 0) {
      throw new ValidationException(String.format(Exceptions.INVALID_STATIC_MODIFIER, lineNum, colNum));
    }
    if (!finalAllowed && finalCount > 0) {
      throw new ValidationException(String.format(Exceptions.INVALID_FINAL_MODIFIER, lineNum, colNum));
    }
    if (!nativeAllowed && nativeCount > 0) {
      throw new ValidationException(String.format(Exceptions.INVALID_NATIVE_MODIFIER, lineNum, colNum));
    }

    if (publicCount + protectedCount != 1) {
      throw new ValidationException(String.format(Exceptions.INVALID_MODIFIER_COUNT, lineNum, colNum));
    }
    if (abstractCount + finalCount > 1) {
      throw new ValidationException(String.format(Exceptions.CONFLICT_ABSTRACT_FINAL, lineNum, colNum));
    }
    if (abstractCount + staticCount > 1) {
      throw new ValidationException(String.format(Exceptions.CONFLICT_ABSTRACT_STATIC, lineNum, colNum));
    }
    if (!staticFinalAllowed && (staticCount + finalCount > 1)) {
      throw new ValidationException(String.format(Exceptions.CONFLICT_STATIC_FINAL, lineNum, colNum));
    }
    if (nativeCount == 1 && (staticCount != 1 || abstractCount > 0 || finalCount > 0)) {
      throw new ValidationException(String.format(Exceptions.CONFLICT_NATIVE_REQ, lineNum, colNum));
    }

    if (publicCount > 1 || protectedCount > 1 || staticCount > 1 || finalCount > 1 || nativeCount > 1) {
      throw new ValidationException(String.format(Exceptions.CONFLICT_DUPLICATE, lineNum, colNum));
    }

    return abstractCount > 0;
  }

}
