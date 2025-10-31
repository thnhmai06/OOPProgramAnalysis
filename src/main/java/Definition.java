import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Scanner;

/**
 *
 *
 * <h1>{@link Definition}</h1>
 *
 * <p>Một Định nghĩa. Yep. Nó là thành phần để tương tác khi code.
 *
 * @apiNote checked
 */
public abstract class Definition {
    protected Definition parent = null;
    protected final LinkedHashSet<Definition> localDefinition = new LinkedHashSet<>();
    protected String simpleName;

    /**
     * Đọc Chữ kí của {@link Definition} tương ứng.
     *
     * @param signature Chữ kí
     * @param externalDefinition Các {@link Definition} đã được khai báo bên ngoài
     * @param fallback Sử dụng {@link Definition} này làm parent nếu không thấy parent
     * @see #readAll(String, Scanner, ExtendedLinkedHashSet, Definition)
     */
    protected abstract void readSignature(
            String signature, ExtendedLinkedHashSet<Definition> externalDefinition, Definition fallback);

    /**
     * Đọc Code block của {@link Definition} tương ứng.
     *
     * @param source {@link Scanner} trỏ tới nơi bắt đầu Code block
     * @param externalDefinition Các {@link Definition} đã được khai báo bên ngoài
     * @param fallback Sử dụng {@link Definition} này làm parent nếu không thấy parent
     * @see #readAll(String, Scanner, ExtendedLinkedHashSet, Definition)
     */
    protected abstract void readCodeBlock(
            Scanner source, ExtendedLinkedHashSet<Definition> externalDefinition, Definition fallback);

    /**
     * Đọc Chữ kí và Code block của {@link Definition} tương ứng.
     *
     * @param signature Chữ kí
     * @param source {@link Scanner} trỏ tới nơi bắt đầu Code block
     * @param externalDefinition Các {@link Definition} đã được khai báo bên ngoài
     * @see #readSignature(String, ExtendedLinkedHashSet, Definition)
     * @see #readCodeBlock(Scanner, ExtendedLinkedHashSet, Definition)
     */
    protected final void readAll(
            String signature,
            Scanner source,
            ExtendedLinkedHashSet<Definition> externalDefinition,
            Definition fallback) {
        readSignature(signature, externalDefinition, fallback);
        readCodeBlock(source, externalDefinition, fallback);
    }

    /**
     * Lấy tất cả {@link Definition} có thể truy cập được ở {@link Definition} này.
     *
     * @return Các khai báo có thể dùng được
     */
    public final ExtendedLinkedHashSet<Definition> getDeclared() {
        ExtendedLinkedHashSet<Definition> declared = new ExtendedLinkedHashSet<>(localDefinition);
        if (parent != null) {
            declared.extend(parent.getDeclared()); // parent đã chứa nó
        } else {
            // trường hợp không tự chứa nó
            declared.extend(new LinkedHashSet<>(Collections.singletonList(this)));
        }
        declared.extend(localDefinition);
        return declared;
    }

    public final LinkedHashSet<Definition> getLocalDefinition() {
        return localDefinition;
    }

    /**
     * Tên Đầy đủ của Định nghĩa code (giống như {@link java.lang.Class#getCanonicalName()}). <br>
     * Ví dụ: {@code java.util.List}
     *
     * @return Tên đầy đủ
     * @see #getSimpleName()
     */
    public abstract String getFullName();

    /**
     * Tên Đơn giản của Định nghĩa code (giống như {@link java.lang.Class#getSimpleName()}). <br>
     * Ví dụ: {@code List}
     *
     * @return Tên Đơn giản
     * @see #getFullName()
     */
    public final String getSimpleName() {
        return simpleName;
    }

    public final void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public final Definition getParent() {
        return parent;
    }

    public final void setParent(Definition parent) {
        this.parent = parent;
    }
}
