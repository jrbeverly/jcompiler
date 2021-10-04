package ca.uwaterloo.joos1wc.ast.declaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.analysis.PackageTable;
import ca.uwaterloo.joos1wc.ast.ASTNode;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode;
import ca.uwaterloo.joos1wc.ast.Modifier;
import ca.uwaterloo.joos1wc.ast.ModifierList;
import ca.uwaterloo.joos1wc.ast.SimpleType;
import ca.uwaterloo.joos1wc.ast.SimpleTypeList;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.ast.Modifier.ModifierKeyword;
import ca.uwaterloo.joos1wc.ast.Type.FormalType;
import ca.uwaterloo.joos1wc.codegen.CodeGenUtils;
import ca.uwaterloo.joos1wc.codegen.VTable;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;

public class TypeDeclaration extends ASTNode implements INamedEntityNode {
  public final List<Modifier> modifiers;
  public final String name;
  public final SimpleType superclass;
  public final List<SimpleType> ifaces;
  public final List<SimpleType> superifaces;

  // We refine the representation of the type body as compilation progresses: we start with a list of all declarations
  // during AST construction, map fields and methods separately by name before type linking, and after linking resolve
  // the methods by signature
  private List<BodyDeclaration> body;
  private Map<String, FieldDeclaration> fields;
  private Map<String, List<MethodDeclaration>> methods;
  private Map<MethodDeclaration.Signature, MethodDeclaration> declaredMethods;
  private Map<MethodDeclaration.Signature, MethodDeclaration> inheritedMethods;

  private VTable vtable;

  public final boolean isClass;
  public final boolean isInterface;

  public final Type.FormalType formalType;

  // For convenience during semantic analysis
  private TypeDeclaration superclassDecl;
  private Collection<TypeDeclaration> ifaceDecls;
  private Collection<TypeDeclaration> superifaceDecls;
  private PackageTable pkg;

  // For code gen. Should probably be constructed at some other stage, but oh well. (If you need something, just throw
  // more memory at the problem?)
  private Set<TypeDeclaration> supertypes;

  public TypeDeclaration(Token token, List<Modifier> modifiers, String name, SimpleType superclass,
      List<SimpleType> ifaces, List<SimpleType> superifaces, List<BodyDeclaration> body, boolean isClass,
      boolean isInterface) {
    super(token);
    this.modifiers = modifiers;
    this.name = name;
    this.superclass = (superclass == null || superclass.name == null) ? null : superclass;
    this.ifaces = ifaces;
    this.superifaces = superifaces;
    this.body = body;
    this.isClass = isClass;
    this.isInterface = isInterface;
    this.formalType = new Type.FormalType(false, this);

  }

  public VTable getVTable() {
    assert this.isClass;
    if (this.vtable == null) {
      this.vtable = new VTable(this);
    }
    return this.vtable;
  }

  public int getInstanceSize() {
    return CodeGenUtils.DWORD * this.fields.size() + CodeGenUtils.DWORD;
  }

  /**
   * This could be done a couple different ways, but for now we can figure it out each time.
   * 
   * @return whether or not this type is declared final
   */
  public boolean isFinal() {
    return isModifier(Modifier.ModifierKeyword.FINAL);
  }

  public boolean isAbstract() {
    return isModifier(Modifier.ModifierKeyword.ABSTRACT);
  }

  public boolean isModifier(Modifier.ModifierKeyword keyword) {
    if (this.modifiers != null) {
      for (Modifier modifier : this.modifiers) {
        if (modifier.keyword == keyword) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean hasDefaultConstructor() {
    MethodDeclaration.Signature sig = new MethodDeclaration.Signature(true, name, new ArrayList<FormalType>());
    return null != getDeclaredMethod(sig);
  }

  public void setSuperclassDecl(TypeDeclaration superclassDecl) {
    assert (isClass && !isInterface);
    this.superclassDecl = superclassDecl;
  }

  public TypeDeclaration getSuperclassDecl() {
    assert (isClass && !isInterface);
    return superclassDecl;
  }

  // TODO copy?
  public void setIfaceDecls(Collection<TypeDeclaration> ifaceDecls) {
    assert (isClass && !isInterface);
    this.ifaceDecls = ifaceDecls;
  }

  public Collection<TypeDeclaration> getIfaceDecls() {
    assert (isClass && !isInterface);
    return Collections.unmodifiableCollection(ifaceDecls);
  }

  public Set<TypeDeclaration> getSupertypes() {
    if (supertypes == null) {
      supertypes = new HashSet<>();
      if (isClass) {
        if (superclassDecl != null) {
          supertypes.add(superclassDecl);
          supertypes.addAll(superclassDecl.getSupertypes());
        }
        for (TypeDeclaration ifaceDecl : ifaceDecls) {
          supertypes.add(ifaceDecl);
          supertypes.addAll(ifaceDecl.getSupertypes());
        }
      } else {
        for (TypeDeclaration superifaceDecl : superifaceDecls) {
          supertypes.add(superifaceDecl);
          supertypes.addAll(superifaceDecl.getSupertypes());
        }
      }
      supertypes.add(this);
    }
    return Collections.unmodifiableSet(supertypes);
  }

  // TODO copy?
  public void setSuperifaceDecls(Collection<TypeDeclaration> superifaceDecls) {
    assert (!isClass && isInterface);
    this.superifaceDecls = superifaceDecls;
  }

  public Collection<TypeDeclaration> getSuperifaceDecls() {
    assert (!isClass && isInterface);
    return Collections.unmodifiableCollection(superifaceDecls);
  }

  public List<BodyDeclaration> getBody() {
    return Collections.unmodifiableList(body);
  }

  public void setFields(Map<String, FieldDeclaration> fields) {
    this.fields = fields;
  }

  public void setMethods(Map<String, List<MethodDeclaration>> methods) {
    this.methods = methods;
  }

  public FieldDeclaration getField(String name) {
    assert (fields != null && methods != null);
    return fields.get(name);
  }

  public Collection<MethodDeclaration> getMethod(String name) {
    assert (fields != null && methods != null);
    return Collections.unmodifiableCollection(methods.get(name));
  }

  public boolean isMethodDeclared(String name) {
    assert (fields != null && methods != null);
    return methods.get(name) != null;
  }

  public boolean isAssignable(TypeDeclaration type) {
    if (type.isClass)
      return isSubclassOf(type);

    return ifaceDecls.contains(type) || superifaceDecls.contains(type);
  }

  public boolean isSubclassOf(TypeDeclaration ancestor) {
    assert fields != null && methods != null && isClass && ancestor.isClass;
    TypeDeclaration cdecl = this;
    while (cdecl != null) {
      if (cdecl == ancestor)
        return true;
      cdecl = cdecl.getSuperclassDecl();
    }
    return false;
  }

  public Collection<FieldDeclaration> getFields() {
    assert (fields != null && methods != null);
    return Collections.unmodifiableCollection(fields.values());
  }

  public Collection<List<MethodDeclaration>> getMethods() {
    assert (fields != null && methods != null);
    return Collections.unmodifiableCollection(methods.values());
  }

  public Collection<List<MethodDeclaration>> getConstructors() {
    assert (fields != null && methods != null);
    List<List<MethodDeclaration>> constructors = new ArrayList<List<MethodDeclaration>>();
    for (List<MethodDeclaration> mdecls : methods.values()) {
      List<MethodDeclaration> decls = new ArrayList<MethodDeclaration>();
      for (MethodDeclaration decl : mdecls) {
        if (decl.isConstructor) {
          decls.add(decl);
        }
      }
      constructors.add(decls);
    }
    return Collections.unmodifiableCollection(constructors);
  }

  public void setDeclaredMethods(Map<MethodDeclaration.Signature, MethodDeclaration> declaredMethods) {
    this.declaredMethods = declaredMethods;
  }

  public Map<MethodDeclaration.Signature, MethodDeclaration> getDeclaredMethods() {
    return declaredMethods;
  }

  public boolean declaresMethod(MethodDeclaration.Signature signature) {
    assert (declaredMethods != null);
    return declaredMethods.containsKey(signature);
  }

  public MethodDeclaration getDeclaredMethod(MethodDeclaration.Signature signature) {
    return declaredMethods.get(signature);
  }

  public MethodDeclaration getInheritedMethod(MethodDeclaration.Signature signature) {
    return inheritedMethods.get(signature);
  }

  public void setInheritedMethods(Map<MethodDeclaration.Signature, MethodDeclaration> inheritedMethods) {
    this.inheritedMethods = inheritedMethods;
  }

  public Map<MethodDeclaration.Signature, MethodDeclaration> getInheritedMethods() {
    return inheritedMethods;
  }

  public void setPackage(PackageTable pkg) {
    this.pkg = pkg;
  }

  public PackageTable getPackage() {
    return pkg;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public EntityType getEntityType() {
    return EntityType.TYPE;
  }

  @Override
  public Type getType() {
    return null;
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", isClass ? "Class" : "Interface", name);
  }

  public String getCanonicalName() {
    String fullName = pkg.getFullName();
    if (fullName.length() > 0) {
      return String.format("%s.%s", pkg.getFullName(), name);
    } else {
      return name;
    }
  }

  public String getGlobalName() {
    return String.format("C%s", getCanonicalName());
  }

  public static TypeDeclaration getObjectType() {
    return Joos1Wc.DEFAULT_PKG.getSubpackage("java").getSubpackage("lang").getType("Object");
  }

  public static TypeDeclaration newInstance(Token token, TypeDeclaration o) {
    return new TypeDeclaration(token, o.modifiers, o.name, o.superclass, o.ifaces, o.superifaces, o.body, o.isClass,
        o.isInterface);
  }

  public static TypeDeclaration newInstance(Token token, ModifierList modListNode, Terminal n0, Terminal nameNode,
      SimpleType superclass, SimpleTypeList ifaces, BodyDeclarationList body) {
    return new TypeDeclaration(token, modListNode.getModifiers(), nameNode.token.getImage(), superclass,
        ifaces.getTypes(), null, body.getBodyDeclarations(), true, false);
  }

  public static TypeDeclaration newInstance(Token token, ModifierList modListNode, Terminal whatIs, Terminal nameNode,
      SimpleTypeList superifaces, BodyDeclarationList body) {
    return new TypeDeclaration(token, modListNode.getModifiers(), nameNode.token.getImage(), null, null,
        superifaces.getTypes(), body.getBodyDeclarations(), false,
        (whatIs.token.getKind() == TerminalTokenKind.INTERFACE));
  }

}
