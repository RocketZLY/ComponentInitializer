package rocketzly.componentinitializer.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import rocketzly.componentinitializer.annotation.Init;
import rocketzly.componentinitializer.IInitMethodContainer;
import rocketzly.componentinitializer.InitConstant;
import rocketzly.componentinitializer.InitMethodInfo;
import rocketzly.componentinitializer.annotation.ThreadMode;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

/**
 * 组件初始化注解处理器
 * Created by rocketzly on 2019/7/17.
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(InitConstant.INITIALIZER_MODULE_NAME)
@SupportedAnnotationTypes(InitConstant.SUPPORT_ANNOTATION_QUALIFIED_NAME)
public class ComponentInitializerProcessor extends AbstractProcessor {

    private String moduleName;
    private Filer filer;
    private Messager messager;
    private Types typeUtils;
    private Elements elementUtils;
    private final String APPLICATION_QUALIFIED_NAME = "android.app.Application";
    private List<InitMethodInfo> syncList = new ArrayList<>(20);
    private List<InitMethodInfo> asyncList = new ArrayList<>(20);

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        if (processingEnv.getOptions() == null) {
            return;
        }
        moduleName = processingEnv.getOptions().get(InitConstant.INITIALIZER_MODULE_NAME);
        if (moduleName == null || moduleName.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.ERROR, InitConstant.NO_MODULE_NAME_TIPS);
        }
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
        generate();
        return true;
    }

    /**
     * 检查element合法性
     * 1. 检查element是否为ExecutableElement
     * 2. 检查element是否为成员方法
     * 3. 检查方法参数类型要么空参，要么只有一个参数Application
     * 4. 检查方法所在类是否有空参构造方法
     */
    private void check(Element methodElement) {
        //1.检查element是否为ExecutableElement
        if (ElementKind.METHOD != methodElement.getKind()) {
            messager.printMessage(Diagnostic.Kind.ERROR, methodElement.getSimpleName() + "使用错误，@init只能用在方法上", methodElement);
        }
        //2.检查element是否为成员方法
        if (ElementKind.CLASS != methodElement.getEnclosingElement().getKind()) {
            messager.printMessage(Diagnostic.Kind.ERROR, methodElement.getSimpleName() + "方法无法使用，@init只能用在成员方法上", methodElement);
        }
        //3.检查方法参数类型要么空参，要么只有一个参数Application
        List<? extends VariableElement> parameters = ((ExecutableElement) methodElement).getParameters();
        if (parameters.size() > 1) {
            messager.printMessage(Diagnostic.Kind.ERROR, methodElement.getSimpleName() + "方法参数个数有误，@init最多只支持一个参数的方法", methodElement);
        }
        if (parameters.size() != 0) {
            TypeElement typeElement = elementUtils.getTypeElement(APPLICATION_QUALIFIED_NAME);
            if (!typeUtils.isSameType(parameters.get(0).asType(), typeElement.asType())) {
                messager.printMessage(Diagnostic.Kind.ERROR, methodElement.getSimpleName() + "方法参数类型有误，@init标注的方法只支持一个参数并且类型必须为Application", methodElement);
            }
        }
        //4.检查方法所在类是否有空参构造方法
        List<? extends Element> allMembers = elementUtils.getAllMembers((TypeElement) methodElement.getEnclosingElement());
        boolean hasEmptyConstructor = false;
        for (Element e : allMembers) {
            if (ElementKind.CONSTRUCTOR == e.getKind() && ((ExecutableElement) e).getParameters().size() == 0) {
                hasEmptyConstructor = true;
                break;
            }
        }
        if (!hasEmptyConstructor)
            messager.printMessage(Diagnostic.Kind.ERROR, methodElement.getEnclosingElement().getSimpleName() + "没有空参构造方法，@init标注方法所在类必须有空参构造方法", methodElement.getEnclosingElement());
    }

    /**
     * 分析被标注的方法，获取相关信息
     * 1. 方法所在类的完全路径名
     * 2. 方法的名字
     * 3. 方法是否有参数
     * 4. 方法上注解中的调用线程和优先级信息
     * 封装为 {@link InitMethodInfo}
     * 再根据线程区分是存储在同步或者异步list中
     *
     * @param element
     */
    private void analyze(Element element) {
        //获取该方法所在类的完全路径名
        String className = ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString();
        //返回该方法的名字
        String methodName = element.getSimpleName().toString();
        //确定方法是否有参数
        boolean isParams = ((ExecutableElement) element).getParameters().size() > 0;
        //获取到方法上的注解
        Init annotation = element.getAnnotation(Init.class);
        //拿到调用线程
        ThreadMode thread = annotation.thread();
        //拿到调用优先级
        int priority = annotation.priority();
        if (ThreadMode.MAIN.equals(thread)) {
            syncList.add(new InitMethodInfo(className, methodName, isParams, priority, thread));
        } else {
            asyncList.add(new InitMethodInfo(className, methodName, isParams, priority, thread));
        }
    }

    /**
     * 生成代码
     * <p>
     * 例如：
     * public final class ComponentInitializerHelper_moduleA implements IInitMethodContainer {
     * private List syncList = new ArrayList<InitMethodInfo>();
     * <p>
     * private List asyncList = new ArrayList<InitMethodInfo>();
     * <p>
     * public ComponentInitializerHelper_moduleA() {
     * syncList.add(new InitMethodInfo("com.rocketzly.modulea.ModuleAInit","sync10",true,10,ThreadMode.MAIN));
     * asyncList.add(new InitMethodInfo("com.rocketzly.modulea.ModuleAInit","async30",true,30,ThreadMode.BACKGROUND));
     * }
     *
     * @Override public List<InitMethodInfo> getSyncInitMethodList() {
     * return syncList;
     * }
     * @Override public List<InitMethodInfo> getAsyncInitMethodList() {
     * return asyncList;
     * }
     * }
     */
    private void generate() {
        //生成字段
        FieldSpec fieldSyncList = generateField(InitConstant.GENERATE_FIELD_SYNC_LIST);
        FieldSpec fieldAsyncList = generateField(InitConstant.GENERATE_FIELD_ASYNC_LIST);

        //初始化构造方法
        MethodSpec.Builder constructorBuild = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        initConstructor(constructorBuild, InitConstant.GENERATE_FIELD_SYNC_LIST);
        initConstructor(constructorBuild, InitConstant.GENERATE_FIELD_ASYNC_LIST);
        MethodSpec constructorMethod = constructorBuild.build();

        //生成方法
        MethodSpec syncMethod = generatorMethod(InitConstant.GENERATE_METHOD_GET_SYNC_NAME);
        MethodSpec asyncMethod = generatorMethod(InitConstant.GENERATE_METHOD_GET_ASYNC_NAME);

        TypeSpec typeSpec = TypeSpec.classBuilder(InitConstant.GENERATE_CLASS_NAME_PREFIX + moduleName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(fieldSyncList)
                .addField(fieldAsyncList)
                .addMethod(syncMethod)
                .addMethod(asyncMethod)
                .addMethod(constructorMethod)
                .addSuperinterface(ClassName.get(IInitMethodContainer.class))
                .build();

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
     * private List syncList = new ArrayList<InitMethodInfo>();
     */
    private FieldSpec generateField(String fieldName) {
        return FieldSpec.builder(List.class, fieldName)
                .addModifiers(Modifier.PRIVATE)
                .initializer("new $T<$T>()", ArrayList.class, InitMethodInfo.class)
                .build();
    }

    /**
     * 初始化构造函数
     * <p>
     * 例如：
     * public ComponentInitializerHelper_moduleA() {
     * syncList.add(new InitMethodInfo("com.rocketzly.modulea.ModuleAInit","sync10",true,10,ThreadMode.MAIN));
     * asyncList.add(new InitMethodInfo("com.rocketzly.modulea.ModuleAInit","async30",true,30,ThreadMode.BACKGROUND));
     * }
     */
    private void initConstructor(MethodSpec.Builder builder, String initFieldName) {
        for (InitMethodInfo methodInfo : initFieldName.equals(InitConstant.GENERATE_FIELD_SYNC_LIST) ? syncList : asyncList) {
            builder.addStatement("$N.add(new $T($S,$S,$L,$L,$T.$L))",
                    initFieldName,
                    InitMethodInfo.class,
                    methodInfo.className,
                    methodInfo.methodName,
                    methodInfo.isParams,
                    methodInfo.priority,
                    ThreadMode.class,
                    methodInfo.thread
            );
        }
    }

    /**
     * 生成方法
     * <p>
     * 例如：
     * @Override public List<InitMethodInfo> getSyncInitMethodList() {
     * return syncList;
     * }
     */
    private MethodSpec generatorMethod(String methodName) {
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(InitMethodInfo.class)))
                .addStatement("return $N", methodName.equals(InitConstant.GENERATE_METHOD_GET_SYNC_NAME) ? InitConstant.GENERATE_FIELD_SYNC_LIST : InitConstant.GENERATE_FIELD_ASYNC_LIST)
                .build();
    }

}
