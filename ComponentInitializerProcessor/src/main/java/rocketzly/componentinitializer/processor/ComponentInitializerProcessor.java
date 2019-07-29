package rocketzly.componentinitializer.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import rocketzly.componentinitializer.IInitializer;
import rocketzly.componentinitializer.annotation.Init;
import rocketzly.componentinitializer.InitConstant;
import rocketzly.componentinitializer.annotation.ThreadMode;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * 组件初始化注解处理器
 * Created by rocketzly on 2019/7/17.
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("rocketzly.componentinitializer.annotation.Init")
public class ComponentInitializerProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Types typeUtils;
    private Elements elementUtils;
    private String APPLICATION_QUALIFIED_NAME = "android.app.Application";
    private List<InitMethodInfo> syncList = new ArrayList<>(20);
    private List<InitMethodInfo> asyncList = new ArrayList<>(20);
    private HashSet<InitClassInfo> classList = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.size() == 0) {
            return false;
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Init.class)) {
            if (element == null) {
                continue;
            }
            check(element);
            analyze(element);
        }
        Collections.sort(syncList);
        Collections.sort(asyncList);
        generate();
        return true;
    }

    /**
     * 检查element合法性
     * 1. 检查element是否为ExecutableElement
     * 2. 检查方法参数类型要么空参，要么只有一个参数Application
     * 3. 检查element外层元素是否为类
     * 4. 检查外层的类是否有空参构造方法
     */
    private void check(Element methodElement) {
        //1.检查element是否为ExecutableElement
        if (ElementKind.METHOD != methodElement.getKind()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@init只能用在方法上");
        }
        //2.检查方法参数类型要么空参，要么只有一个参数Application
        List<? extends VariableElement> parameters = ((ExecutableElement) methodElement).getParameters();
        if (parameters.size() > 1) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@init最多只支持一个参数的方法");
        }
        if (parameters.size() != 0) {
            TypeElement typeElement = elementUtils.getTypeElement(APPLICATION_QUALIFIED_NAME);
            if (!typeUtils.isSameType(parameters.get(0).asType(), typeElement.asType())) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@init标注的方法参数类型必须为Application");
            }
        }
        //3.检查element外层元素是否为类
        if (ElementKind.CLASS != methodElement.getEnclosingElement().getKind()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@init只能用在成员方法上");
        }
        //4.检查外层的类是否有空参构造方法
        List<? extends Element> allMembers = elementUtils.getAllMembers((TypeElement) methodElement.getEnclosingElement());
        boolean hasEmptyConstructor = false;
        for (Element e : allMembers) {
            if (ElementKind.CONSTRUCTOR == e.getKind() && ((ExecutableElement) e).getParameters().size() == 0) {
                hasEmptyConstructor = true;
                break;
            }
        }
        if (!hasEmptyConstructor)
            messager.printMessage(Diagnostic.Kind.ERROR, "@init所在类必须有空参构造方法");
    }

    /**
     * 分析被标注的方法，获取相关信息
     * 1. 该方法所在类信息用来实例化供后面方法调用
     * 2. 根据类名生成类的成员变量名
     * 3. 方法的名字
     * 4. 方法是否参数
     * 5. 方法上注解中的调用线程和优先级信息
     * 封装为 {@link InitMethodInfo}
     * 再根据线程区分是存储在同步或者异步list中
     *
     * @param element
     */
    private void analyze(Element element) {
        //1.该方法所在类信息用来实例化供后面方法调用
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        //2.根据类名生成类的成员变量名
        char[] chars = classElement.getSimpleName().toString().toCharArray();
        chars[0] += 32;
        String variableName = String.valueOf(chars);
        classList.add(new InitClassInfo(classElement, variableName));
        //3.方法的名字
        String methodName = element.getSimpleName().toString();
        //4.确定方法是否有参数
        boolean isParams = ((ExecutableElement) element).getParameters().size() > 0;
        //5.获取到方法上的注解
        Init annotation = element.getAnnotation(Init.class);
        //拿到调用线程
        ThreadMode thread = annotation.thread();
        //拿到调用优先级
        int priority = annotation.priority();
        if (ThreadMode.MAIN.equals(thread)) {
            syncList.add(new InitMethodInfo(variableName, methodName, isParams, priority, thread));
        } else {
            asyncList.add(new InitMethodInfo(variableName, methodName, isParams, priority, thread));
        }
    }

    /**
     * 生成代码
     * <p>
     * 例如：
     * public final class _ComponentInitializerHelper implements IInitializer {
     *     private AppInit appInit;
     *
     *     private boolean isDebug;
     *
     *     public _ComponentInitializerHelper(boolean isDebug) {
     *         this.isDebug = isDebug;
     *     }
     *
     *     private void init() {
     *         this.appInit = new AppInit();
     *     }
     *
     *     @Override
     *     public void start(Object application) {
     *         init();
     *         execute((Application) application);
     *     }
     *
     *     private void execute(final Application application) {
     *         Executors.newSingleThreadExecutor().execute(new Runnable() {
     *             public void run() {
     *                 appInit.async1(application);
     *                 appInit.async30(application);
     *                 appInit.async100(application);
     *             }
     *         });
     *         appInit.sync1(application);
     *         appInit.sync3(application);
     *         appInit.sync10(application);
     *     }
     * }
     */
    private void generate() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(InitConstant.GENERATE_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(IInitializer.class);

        //生成字段
        for (InitClassInfo info : classList) {
            builder.addField(generateField(info.element, info.variableName));
        }
        //isDebug
        builder.addField(TypeName.BOOLEAN, InitConstant.GENERATE_FIELD_ISDEBUG, Modifier.PRIVATE);
        //生成构造方法
        builder.addMethod(generateConstructor());
        //生成init方法
        builder.addMethod(generatorInitMethod());
        //实现接口start方法
        builder.addMethod(generatorImplMethod());
        //生成execute方法
        builder.addMethod(generatorExecuteMethod());

        TypeSpec typeSpec = builder.build();
        JavaFile javaFile = JavaFile.builder(InitConstant.GENERATE_PACKAGE_NAME, typeSpec)
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成字段
     * <p>
     * 例如：
     * private AppInit appInit;
     */
    private FieldSpec generateField(TypeElement classElement, String fieldName) {
        return FieldSpec.builder(ClassName.get(classElement), fieldName)
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    /**
     * 生成构造函数
     * <p>
     * 例如：
     * public _ComponentInitializerHelper(boolean isDebug) {
     *     this.isDebug = isDebug;
     * }
     */
    private MethodSpec generateConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.BOOLEAN, InitConstant.GENERATE_FIELD_ISDEBUG)
                .addStatement("this.$N = $N", InitConstant.GENERATE_FIELD_ISDEBUG, InitConstant.GENERATE_FIELD_ISDEBUG)
                .build();
    }

    /**
     * 生成init方法初始化需要调用的类
     * <p>
     * 例如：
     *private void init() {
     *    this.appInit = new AppInit();
     *}
     */
    private MethodSpec generatorInitMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(InitConstant.GENERATE_METHOD_INIT)
                .addModifiers(Modifier.PRIVATE);
        for (InitClassInfo info : classList) {
            builder.addStatement("this.$N = new $T()", info.variableName, ClassName.get(info.element));
        }
        return builder.build();
    }

    /**
     * 实现接口方法
     * <p>
     * 例如：
     * @Override
     * public void start(Object application) {
     *     init();
     *     execute((Application) application);
     * }
     */
    private MethodSpec generatorImplMethod() {
        return MethodSpec.methodBuilder(InitConstant.GENERATE_METHOD_START)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, InitConstant.GENERATE_VARIABLE_APPLICATION)
                .addStatement("$N()", InitConstant.GENERATE_METHOD_INIT)
                .addStatement("$N(($T)$N)",
                        InitConstant.GENERATE_METHOD_EXECUTE,
                        ClassName.get(elementUtils.getTypeElement(APPLICATION_QUALIFIED_NAME)),
                        InitConstant.GENERATE_VARIABLE_APPLICATION)
                .build();
    }

    /**
     * 生成execute方法
     * <p>
     * 例如：
     * private void execute(final Application application) {
     *     Executors.newSingleThreadExecutor().execute(new Runnable() {
     *         public void run() {
     *             appInit.async1(application);
     *             appInit.async30(application);
     *             appInit.async100(application);
     *         }
     *     });
     *     appInit.sync1(application);
     *     appInit.sync3(application);
     *     appInit.sync10(application);
     * }
     */
    private MethodSpec generatorExecuteMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(InitConstant.GENERATE_METHOD_EXECUTE)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ClassName.get(elementUtils.getTypeElement(APPLICATION_QUALIFIED_NAME)), InitConstant.GENERATE_VARIABLE_APPLICATION, Modifier.FINAL);

        //生成初始化异步方法代码
        CodeBlock.Builder codeBuilder = CodeBlock.builder()
                .add("$T.$L().$L(new $T(){ public void run() {\n",
                        Executors.class,
                        "newSingleThreadExecutor",
                        "execute",
                        Runnable.class);
        for (InitMethodInfo info : asyncList) {
            codeBuilder.add("$N.$N(" + (info.isParams ? "$N" : "") + ");\n",
                    info.isParams ? new Object[]{info.classVariableName, info.methodName, InitConstant.GENERATE_VARIABLE_APPLICATION}
                            : new Object[]{info.classVariableName, info.methodName}
            );
        }
        codeBuilder.add("}})");
        builder.addStatement(codeBuilder.build());

        //生成初始化同步方法代码
        for (InitMethodInfo info : syncList) {
            builder.addStatement("$N.$N(" + (info.isParams ? "$N" : "") + ")",
                    info.isParams ? new Object[]{info.classVariableName, info.methodName, InitConstant.GENERATE_VARIABLE_APPLICATION}
                            : new Object[]{info.classVariableName, info.methodName}
            );
        }

        return builder.build();
    }
}
