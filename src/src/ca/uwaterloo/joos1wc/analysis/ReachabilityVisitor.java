package ca.uwaterloo.joos1wc.analysis;

import ca.uwaterloo.joos1wc.ast.Block;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.RecursiveVisitor;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.literal.BooleanLiteral;
import ca.uwaterloo.joos1wc.ast.statement.ForStatement;
import ca.uwaterloo.joos1wc.ast.statement.IfThenStatement;
import ca.uwaterloo.joos1wc.ast.statement.ReturnStatement;
import ca.uwaterloo.joos1wc.ast.statement.Statement;
import ca.uwaterloo.joos1wc.ast.statement.WhileStatement;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;
import ca.uwaterloo.joos1wc.diagnostics.Diagnostics;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.diagnostics.UnreachableException;

public class ReachabilityVisitor extends RecursiveVisitor {
  
  // An attempt at returning multiple errors and avoiding the need to throw unchecked exceptions
  private final Diagnostics errors;

  public ReachabilityVisitor(Diagnostics diagnostic) {
    errors = diagnostic;
    // empty for now
  }
  
  public Diagnostics getErrors() {
    return errors;
  }
  
  /**
   * This section asks if the statement is reachable.
   */
  
  public void preVisit(MethodDeclaration node) {
    /*
     * The block that is the body of a constructor or method is reachable. 
     */
    if (node.body != null) {
      node.body.isReachable = true;
    }
  }
  
  public void visit(Block node) {
    stack.push(node);
    
    // if we get here and we're not reachable, that's a problem
    if (!node.isReachable) {
      errors.add(new UnreachableException(String.format(Exceptions.IS_NOT_REACHABLE, "Block"), node.token));
    }
    
    /*
     * The first statement in a nonempty block is reachable iff the block is reachable. 
     * Every other statement S in a nonempty block is reachable iff the statement preceding S can complete normally. 
     */
    Statement lastChild = null;
    for (Statement child : node.statements) {
      if (lastChild == null) {
        child.isReachable = node.isReachable;
      } else {
        child.isReachable = lastChild.canCompleteNormally;
        
        // if the child statement isn't reachable, that's a problem
        if (!child.isReachable) {
          errors.add(new UnreachableException(String.format(Exceptions.IS_NOT_REACHABLE, "Body contents"), node.token));
        }
      }
      child.accept(this);
      lastChild = child;
    }
    
    /*
     * An empty block can complete normally iff it is reachable. 
     * A nonempty block can complete normally iff the last statement in it can complete normally. 
     */
    if (node.statements.isEmpty()) {
      node.canCompleteNormally = node.isReachable;
    } else {
      // lastChild is still correct
      node.canCompleteNormally = lastChild.canCompleteNormally;
    }
    
    stack.pop();
  }
  
  public void preVisit(WhileStatement node) {
    // if we get here and we're not reachable, that's a problem
    if (!node.isReachable) {
      errors.add(new UnreachableException(String.format(Exceptions.IS_NOT_REACHABLE, "While"), node.token));
    }
    
    /*
     * The contained statement is reachable iff the while statement is reachable and the condition expression is 
     * not a constant expression whose value is false. 
     */
    boolean expressionIsFalse = false;
    Literal constant = null;
    try {
      constant = node.expr.constantValue();
    } catch (ConstantEvaluationException cee) {
      // catch the error, add it to the list and continue...
      errors.add(cee);
    } catch (CastException ce) {
      // catch the error, add it to the list and continue...
      errors.add(ce);
    }
    if (constant instanceof BooleanLiteral) {
      expressionIsFalse = !((BooleanLiteral)constant).valueOf();
    }
    
    node.statement.isReachable = node.isReachable && !expressionIsFalse;
    
    // if the contained statement isn't reachable, that's a problem
    if (!node.statement.isReachable) {
      errors.add(new UnreachableException(String.format(Exceptions.IS_NOT_REACHABLE, "While Body"), node.token));
    }
  }
  
  public void preVisit(ForStatement node) {
    // if we get here and we're not reachable, that's a problem
    if (!node.isReachable) {
      errors.add(new UnreachableException(String.format(Exceptions.IS_NOT_REACHABLE, "For"), node.token));
    }
    
    /*
     * The contained statement is reachable iff the for statement is reachable and the condition 
     * expression is not a constant expression whose value is false. 
     */
    boolean expressionIsFalse = false;
    if (node.condExpr != null) {
      Literal constant = null;
      try {
        constant = node.condExpr.constantValue();
      } catch (ConstantEvaluationException cee) {
        // catch the error, add it to the list and continue...
        errors.add(cee);
      } catch (CastException ce) {
        // catch the error, add it to the list and continue...
        errors.add(ce);
      }
      if (constant instanceof BooleanLiteral) {
        expressionIsFalse = !((BooleanLiteral)constant).valueOf();
      }
    }
    
    node.statement.isReachable = node.isReachable && !expressionIsFalse;
    
    // if the contained statement isn't reachable, that's a problem
    if (!node.statement.isReachable) {
      errors.add(new UnreachableException(String.format(Exceptions.IS_NOT_REACHABLE, "For Body"), node.token));
    }
  }
  
  public void preVisit(IfThenStatement node) {
    // if we get here and we're not reachable, that's a problem
    if (!node.isReachable) {
      errors.add(new UnreachableException(String.format(Exceptions.IS_NOT_REACHABLE, "IfThen"), node.token));
    }
    
    /*
     * The then-statement is reachable iff the if-then statement is reachable.
     * The then-statement is reachable iff the if-then-else statement is reachable. 
     * The else-statement is reachable iff the if-then-else statement is reachable.
     */
    node.trueStatement.isReachable = node.isReachable;
    
    // if the true statement isn't reachable, that's a problem
    if (!node.trueStatement.isReachable) {
      errors.add(new UnreachableException(String.format(Exceptions.IS_NOT_REACHABLE, "Then"), node.token));
    }

    if (node.falseStatement != null) {
      node.falseStatement.isReachable = node.isReachable;
      
      // if the false statement isn't reachable, that's a problem
      if (!node.falseStatement.isReachable) {
        errors.add(new UnreachableException(String.format(Exceptions.IS_NOT_REACHABLE, "Else"), node.token));
      }
    }
  }
  
  /**
   * This section asks if the statement can complete normally.
   */
  
  public void postVisit(ReturnStatement node) {
    /*
     * A return statement cannot complete normally. 
     */
    node.canCompleteNormally = false;
  }
  
  public void postVisit(Statement node) {
    /*
     * A local variable declaration statement can complete normally iff it is reachable. 
     * An empty statement can complete normally iff it is reachable. 
     * An expression statement can complete normally iff it is reachable. 
     */
    node.canCompleteNormally = node.isReachable;
    
    // if we get here and we cannot complete, we should have already thrown an exception for reachability
  }
  
  public void postVisit(WhileStatement node) {
    /*
     * A while statement can complete normally iff the while statement is reachable and 
     * the condition expression is not a constant expression with value true.
     */
    boolean expressionIsTrue = false;
    Literal constant = null;
    try {
      constant = node.expr.constantValue();
    } catch (ConstantEvaluationException cee) {
      // catch the error, add it to the list and continue...
      errors.add(cee);
    } catch (CastException ce) {
      // catch the error, add it to the list and continue...
      errors.add(ce);
    }
    if (constant instanceof BooleanLiteral) {
      expressionIsTrue = ((BooleanLiteral)constant).valueOf();
    }
    
    node.canCompleteNormally = node.isReachable && !expressionIsTrue;
  }
  
  public void postVisit(ForStatement node) {
    /*
     * A for statement can complete normally iff the for statement is reachable, there is 
     * a condition expression, and the condition expression is not a constant expression 
     * with value true.
     */
    if (node.condExpr == null) {
      node.canCompleteNormally = false;
      
    } else {
      boolean expressionIsTrue = false;
      Literal constant = null;
      try {
        constant = node.condExpr.constantValue();
      } catch (ConstantEvaluationException cee) {
        // catch the error, add it to the list and continue...
        errors.add(cee);
      } catch (CastException ce) {
        // catch the error, add it to the list and continue...
        errors.add(ce);
      }
      if (constant instanceof BooleanLiteral) {
        expressionIsTrue = ((BooleanLiteral)constant).valueOf();
      }
    
      node.canCompleteNormally = node.isReachable && !expressionIsTrue;
    }
  }
  
  public void postVisit(IfThenStatement node) {
    /*
     * An if-then statement can complete normally iff it is reachable. 
     * An if-then-else statement can complete normally iff the then-statement can complete 
     * normally or the else-statement can complete normally.
     */
    if (node.falseStatement == null) {
      node.canCompleteNormally = node.isReachable;
    } else {
      node.canCompleteNormally = node.trueStatement.canCompleteNormally || node.falseStatement.canCompleteNormally;
    }
  }
  
  public void postVisit(MethodDeclaration node) {
    if (node.type != null && node.body != null) {
      if (node.body.canCompleteNormally) {
        errors.add(new UnreachableException(String.format(Exceptions.NON_VOID_NO_RETURN, node.name), node.token));
      }
    }
  }
}
