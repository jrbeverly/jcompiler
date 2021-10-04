package ca.uwaterloo.joos1wc.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;

/*
 * Just a glorified map
 */
public class PackageTable {

  private boolean isDefault;
  private String name, fullname;
  private Map<String, PackageTable> subpackages = new HashMap<>();
  private Map<String, TypeDeclaration> types = new HashMap<>();

  public PackageTable() {
    isDefault = true;
    name = fullname = "";
  }

  public PackageTable(String prefix, String name) {
    isDefault = false;
    this.name = name;
    this.fullname = prefix.length() > 0 ? prefix + "." + name : name;
  }

  public PackageTable addSubpackage(String name) {
    PackageTable t = new PackageTable(fullname, name);
    subpackages.put(name, t);
    return t;
  }

  public void addType(String name, TypeDeclaration node) {
    types.put(name, node);
  }

  public PackageTable getSubpackage(String name) {
    return subpackages.get(name);
  }

  public TypeDeclaration getType(String name) {
    return types.get(name);
  }

  public int getCount() {
    int num = types.size();
    for (PackageTable table : subpackages.values()) {
      num += table.getCount();
    }
    return num;
  }

  public Collection<TypeDeclaration> getAllTypes() {
    List<TypeDeclaration> decls = new ArrayList<TypeDeclaration>();
    decls.addAll(types.values());
    for (PackageTable table : subpackages.values()) {
      decls.addAll(table.getAllTypes());
    }
    return decls;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public String getSubpackageName() {
    return name;
  }

  public String getFullName() {
    return fullname;
  }

  public Collection<PackageTable> getSubpackages() {
    return subpackages.values();
  }

  public Collection<TypeDeclaration> getTypeDecls() {
    return types.values();
  }

}
