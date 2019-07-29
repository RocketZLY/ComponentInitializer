package rocketzly.componentinitializer.processor;

import javax.lang.model.element.TypeElement;

/**
 * 初始化方法所在类信息
 * Created by rocketzly on 2019/7/26.
 */
public class InitClassInfo {
    /**
     * 初始化方法所在类的TypeElement对象
     */
    public TypeElement element;
    /**
     * 此类在生成的代码中的变量名
     */
    public String variableName;

    public InitClassInfo(TypeElement element, String variableName) {
        this.element = element;
        this.variableName = variableName;
    }

    @Override
    public int hashCode() {
        return element == null ? -1 : element.getQualifiedName().toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InitClassInfo)) {
            return false;
        }
        if (element == null) {
            return ((InitClassInfo) obj).element == null;
        }
        return element.getQualifiedName().equals(((InitClassInfo) obj).element.getQualifiedName());
    }
}
