package ca.uwaterloo.joos1wc.diagnostics;

public class ValidationException extends RuntimeException {
  private static final long serialVersionUID = 0L;

  public ValidationException(String message) {
    super(message);
  }
}
