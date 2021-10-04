package ca.uwaterloo.joos1wc.codegen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AssemblyNode {
  // Only leaf nodes are allowed to have actual assembly
  // TODO further abstract assembly instructions?
  private final String instruction;
  private String comment;
  private List<AssemblyNode> children = new ArrayList<AssemblyNode>();
  private Set<String> providedSymbols = new HashSet<String>();
  private Set<String> requiredSymbols = new HashSet<String>();

  public AssemblyNode() {
    this.instruction = null;
  }

  public AssemblyNode(String instruction) {
    this.instruction = instruction;
  }

  public AssemblyNode(String instruction, String label) {
    this.instruction = instruction;
    this.provides(label);
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void provides(String label) {
    providedSymbols.add(label);
  }

  public Set<String> provides() {
    return providedSymbols;
  }

  public void requires(String label) {
    requiredSymbols.add(label);
  }

  public Set<String> requires() {
    return requiredSymbols;
  }

  public void addChild(AssemblyNode child, String comment) {
    child.setComment(comment);
    addChild(child);
  }

  public void addChild(AssemblyNode child) {
    assert instruction == null;
    // Filter out empty leaves, e.g. from EmptyStatement
    if (child.instruction != null) {
      children.add(child);
    } else if (child.children.size() > 0) {
      // flatten the list
      children.addAll(child.children);
    }
    providedSymbols.addAll(child.provides());
    requiredSymbols.addAll(child.requires());
  }

  public void print(PrintStream stream) {
    assert instruction == null || children.size() == 0;
    for (String extern : this.requiredSymbols) {
      if (!this.providedSymbols.contains(extern)) {
        stream.println(String.format("  extern %s", extern));
      }
    }
    for (String global : this.providedSymbols) {
      if (global.charAt(0) != '.') {
        stream.println(String.format("  global %s", global));
      }
    }

    if (instruction != null) {
      printInstruction(stream);
    } else {
      for (AssemblyNode child : children) {
        child.printInstruction(stream);
      }
    }
  }

  private void printInstruction(PrintStream stream) {
    stream.print(instruction);
    if (comment != null) {
      stream.format(" ; %s", comment);
    }
    stream.println();
  }

  public static enum Register {
    AL, BL, CL, DL, AH, BH, CH, DH, AX, BX, CX, DX, SI, DI, SP, BP, IP, EAX, EBX, ECX, EDX, ESI, EDI, ESP, EBP, EIP;
    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }
  }

  public static enum Condition {
    E, G, GE, L, LE, NE;
    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }
  }

  // try to generate a valid unique label with the given suffix
  public static String generateLabel(String suffix) {
    UUID uuid = UUID.randomUUID();
    String lsb = "" + Math.abs(uuid.getLeastSignificantBits());
    String msb = "" + Math.abs(uuid.getMostSignificantBits());
    return String.format("._%s_%s$%s", lsb, msb, suffix);
  }

  public static String offset(Register r, int offset) {
    if (offset == 0) {
      return r.toString();
    } else {
      return offset > 0 ? String.format("%s - %d", r, 4 * offset) : String.format("%s + %d", r, -4 * offset);
    }
  }

  public static final AssemblyNode CBW = new AssemblyNode("  cbw");
  public static final AssemblyNode CDQ = new AssemblyNode("  cdq");
  public static final AssemblyNode CWDE = new AssemblyNode("  cwde");
  public static final AssemblyNode DD = AssemblyNode.dd("0");
  public static final AssemblyNode INT_EXIT = new AssemblyNode("  int 0x80");
  public static final AssemblyNode LEAVE = new AssemblyNode("  leave");
  public static final AssemblyNode RET = new AssemblyNode("  ret");
  public static final AssemblyNode SECTION_DATA = new AssemblyNode("section .data");
  public static final AssemblyNode SECTION_TEXT = new AssemblyNode("section .text");
  public static final AssemblyNode TYPE_TABLE = new AssemblyNode("typeTable");

  public static AssemblyNode add(Register register, Object from) {
    return new AssemblyNode(String.format("  add %s, %s", register, from));
  }

  public static AssemblyNode and(Register register, Object from) {
    return new AssemblyNode(String.format("  and %s, %s", register, from));
  }

  public static AssemblyNode call(Object label) {
    return new AssemblyNode(String.format("  call %s", label));
  }

  public static AssemblyNode cmp(Register to, Object from) {
    return new AssemblyNode(String.format("  cmp %s, %s", to, from));
  }

  public static AssemblyNode comment(String comment) {
    return new AssemblyNode(String.format("; %s", comment.replace('\n', ' ').replace('\r', ' ')));
  }

  // unless we think we need a db for something other than a string...
  public static AssemblyNode dbChar(String label) {
    return new AssemblyNode(String.format("  db '%s'", label));
  }

  public static AssemblyNode db(String value) {
    return new AssemblyNode(String.format("  db %s", value));
  }

  public static AssemblyNode dd(String label) {
    return new AssemblyNode(String.format("  dd %s", label));
  }

  public static AssemblyNode dd(int label) {
    return new AssemblyNode(String.format("  dd %d", label));
  }

  public static AssemblyNode idiv(Register divisor) {
    return new AssemblyNode(String.format("  idiv %s", divisor));
  }

  public static AssemblyNode imul(Register to, Object from) {
    return new AssemblyNode(String.format("  imul %s, %s", to, from));
  }

  public static AssemblyNode j(Condition condition, String label) {
    return new AssemblyNode(String.format("  j%s %s", condition, label));
  }

  public static AssemblyNode jmp(String label) {
    return new AssemblyNode(String.format("  jmp %s", label));
  }

  public static AssemblyNode label(String label) {
    return new AssemblyNode(String.format("%s:", label), label);
  }

  public static AssemblyNode lea(Register to, Object from, int offset) {
    return new AssemblyNode(String.format("  lea %s, [%s + %d]", to, from, offset));
  }

  public static AssemblyNode mov(Register to, Object from) {
    return new AssemblyNode(String.format("  mov %s, %s", to, from));
  }

  public static AssemblyNode movToMem(Object to, Object from) {
    return new AssemblyNode(String.format("  mov DWORD [%s], %s", to, from));
  }

  public static AssemblyNode movToMem(Object to, Object from, int offset) {
    return new AssemblyNode(String.format("  mov DWORD [%s + %d], %s", to, offset, from));
  }

  public static AssemblyNode movFromMem(Register to, Object from) {
    return new AssemblyNode(String.format("  mov %s, [%s]", to, from));
  }

  public static AssemblyNode movFromMem(Register to, Object from, int offset) {
    return new AssemblyNode(String.format("  mov %s, [%s + %d]", to, from, offset));
  }

  public static AssemblyNode neg(Register register) {
    return new AssemblyNode(String.format("  neg %s", register));
  }

  public static AssemblyNode or(Register to, Object from) {
    return new AssemblyNode(String.format("  or %s, %s", to, from));
  }

  public static AssemblyNode pop(Register register) {
    return new AssemblyNode(String.format("  pop %s", register));
  }

  public static AssemblyNode push(Register register) {
    return new AssemblyNode(String.format("  push %s", register));
  }

  public static AssemblyNode push(String value) {
    return new AssemblyNode(String.format("  push dword %s", value));
  }

  public static AssemblyNode set(Condition condition, Register register) {
    return new AssemblyNode(String.format("  set%s %s", condition, register));
  }

  public static AssemblyNode sub(Register register, Object from) {
    return new AssemblyNode(String.format("  sub %s, %s", register, from));
  }

  public static AssemblyNode xor(Register register, Object value) {
    return new AssemblyNode(String.format("  xor %s, %s", register, value));
  }

}
