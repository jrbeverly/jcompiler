package ca.uwaterloo.joos1wc.diagnostics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Diagnostics {

  private final List<JoosException> exceptions;

  public Diagnostics() {
    exceptions = new ArrayList<JoosException>();
  }

  public void add(JoosException exc) {
    exceptions.add(exc);
  }

  public void addAll(List<JoosException> exc) {
    exceptions.addAll(exc);
  }

  public Collection<JoosException> getErrors() {
    return Collections.unmodifiableCollection(exceptions);
  }

  public boolean isEmpty() {
    return exceptions.isEmpty();
  }

  public int getCount() {
    return exceptions.size();
  }
}
