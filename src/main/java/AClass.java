import java.util.*;
import java.util.regex.Matcher;

/**
 *
 *
 * <h1>{@link AClass}</h1>
 *
 * <p>Một AClass. Yep. Bạn mong chờ điều gì chứ :))). <br>
 * Giống như {@link java.lang.Class}
 *
 * @apiNote checked
 */
public final class AClass extends Definition {
    /**
     * Tìm kiếm/Tạo {@link AClass} phù hợp với tên {@code name} trong {@code declared}. <br>
     * <i> aka tìm bố mẹ cho trẻ lạc :> </i>
     *
     * @param name Tên cần tìm
     * @param declared Danh sách các {@link AClass} đã định nghĩa
     * @param fallback Sử dụng {@link Definition} này làm parent nếu không thấy parent
     * @return AClass phù hợp với {@code name}
     */
    public static AClass find(
            String name, ExtendedLinkedHashSet<Definition> declared, Definition fallback) {
        // Đã là Full name
        boolean isFullName = Utilities.isClassExisted(name);

        // Nằm trong đống defined
        for (Definition defined : declared.view()) {
            if (!(defined instanceof AClass)) {
                continue;
            }
            if (isFullName) {
                if (name.equals(defined.getFullName())) {
                    return (AClass) defined;
                }
            } else if (name.equals(defined.getSimpleName())) {
                return (AClass) defined;
            }
        }

        // Vẫn ko thấy, dành thử với với class sẵn có thường thấy trong java
        for (String common : Utilities.commonPackages) {
            final String possibleName = common + name;
            if (Utilities.isClassExisted(possibleName)) {
                final AClass newClass =
                        new AClass(possibleName); // không quan tâm tới package ở đây
                declared.getBase().add(newClass);
                return newClass;
            }
        }

        // thua, hết cứu, vậy là lần cuối đi bên nhau cay đắng nhưng ko đau :<
        final AClass newClass = new AClass(name);
        // ngược lại -> kiểu nguyên thủy
        if (Character.isUpperCase(name.charAt(0))) {
            newClass.parent = fallback;
        }
        declared.getBase().add(newClass);
        return newClass;
    }

    @Override
    protected void readSignature(
            String signature, ExtendedLinkedHashSet<Definition> externalDefinition, Definition fallback) {
        Matcher externalMatch = Patterns.IMPORT.matcher(signature);
        if (externalMatch.matches()) {
            final String parentName = externalMatch.group(1);
            simpleName = externalMatch.group(2);

            // parent của class có thể là package, hoặc class
            if (!parentName.isEmpty()) {
                for (Definition definition : externalDefinition.view()) {
                    if (definition.getFullName().equals(parentName)) {
                        parent = definition;
                        return;
                    }
                }
                parent = new APackage(parentName);
                externalDefinition.getBase().add(parent);
            }
            return;
        }

        Matcher internalMatch = Patterns.CLASS.matcher(signature);
        if (internalMatch.find()) {
            simpleName = internalMatch.group(1);
            return;
        }

        throw new IllegalArgumentException("Không đọc được AClass: " + signature);
    }

    @Override
    public void readCodeBlock(
            Scanner source, ExtendedLinkedHashSet<Definition> externalDefinition, Definition fallback) {
        LinkedHashMap<AMethod, String> holder = new LinkedHashMap<>();
        final ExtendedLinkedHashSet<Definition> definedClassAndPackage = getDeclared(); // lazy
        if (source.nextLine().contains("{")) {
            int balance = 0;
            do {
                final String line = source.nextLine();

                // TH AClass lồng class (bỏ qua)
                //                if (Patterns.CLASS.matcher(line).matches()) {
                //                    final AClass clazz = new AClass(this, line, source,
                // definedClassAndPackage);
                //                    localDeclared.add(clazz);
                //                    definedClassAndPackage.add(clazz); // lazy update
                //                } else
                if (Patterns.METHOD.matcher(line).find()) {
                    final AMethod method = new AMethod(this);
                    localDefinition.add(method);
                    holder.put(method, line);

                    method.readCodeBlock(source, fallback);
                }

                // update balance
                balance += Utilities.countChar(line, '{');
                balance -= Utilities.countChar(line, '}');
            } while (source.hasNextLine() && balance >= 0);
        }

        for (Map.Entry<AMethod, String> hold : holder.entrySet()) {
            final AMethod method = hold.getKey();
            final String signature = hold.getValue();
            method.readSignature(signature, this.getDeclared(), fallback);
        }
    }

    @Override
    public String getFullName() {
        if (parent == null) {
            return getSimpleName();
        }
        return String.format("%s.%s", parent.getFullName(), simpleName);
    }

    private AClass(String simpleName) {
        this.simpleName = simpleName;
    }

    public AClass(
            Definition parent,
            String signature,
            ExtendedLinkedHashSet<Definition> externalDeclaration,
            Definition fallback) {
        this.parent = parent;
        readSignature(signature, externalDeclaration, fallback);
    }

    public AClass(
            Definition parent,
            String signature,
            Scanner source,
            ExtendedLinkedHashSet<Definition> externalDeclaration,
            Definition fallback) {
        this(parent, signature, externalDeclaration, fallback);
        readCodeBlock(source, externalDeclaration, fallback);
    }
}
