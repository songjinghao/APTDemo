package com.songjinghao.compiler;

import com.google.auto.service.AutoService;
import com.songjinghao.annotation.BindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by songjinghao on 2019/8/26.
 */
@AutoService(Processor.class)
// 允许处理的注解类型, 注意一定要写正确全路径
@SupportedAnnotationTypes({"com.songjinghao.annotation.BindView"})
// 注解支持的JDK版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ButterKnifeProcessor extends AbstractProcessor {

    private Messager messager; // 用来报告错误、警告、提示
    private Elements elementsUtils; // 包含了很多的操作Elements的工具方法
    private Filer filer; // 用来创建新的源文件、class文件
    private Types typeUtils; // 包含用于操作TypeMirror工具方法

    // key: 类节点, value: 被注解的属性集合
    private Map<TypeElement, List<Element>> tmpBindViewMap = new HashMap<>();

    private String INTERFACE_CANONICAL_NAME = "com.songjinghao.library.ViewBinder";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementsUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        typeUtils = processingEnv.getTypeUtils();
        messager.printMessage(Diagnostic.Kind.NOTE, "初始化完成，开始注解处理");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!annotations.isEmpty()) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
            if (!elements.isEmpty()) {
                try {
                    buildMap(elements);
                    createJavaFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void createJavaFile() throws IOException {
        if (tmpBindViewMap.isEmpty()) return;

        TypeElement viewBinderType = elementsUtils.getTypeElement(INTERFACE_CANONICAL_NAME);

        for (Map.Entry<TypeElement, List<Element>> entry : tmpBindViewMap.entrySet()) {
            // 下面的代码作用是生成中间 Java 类文件
            // 从下往上看更易于理解
            ClassName className = ClassName.get(entry.getKey());
            // 接口泛型
            ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get(viewBinderType),
                    ClassName.get(entry.getKey()));
            // 方法实参: (MainActivity target)
            String parameterName = "target";
            ParameterSpec parameterSpec = ParameterSpec.builder(ClassName.get(entry.getKey()),
                    parameterName)
                    .build();

            // 方法体: public void bind(MainActivity target) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class) // 默认返回void
                    .addParameter(parameterSpec);

            for (Element fieldElement : entry.getValue()) {

                String fieldName = fieldElement.getSimpleName().toString();

                int annotationValue = fieldElement.getAnnotation(BindView.class).value();

                String methodContentFormat = "$N." + fieldName + " = $N.findViewById($L)";
                methodBuilder.addStatement(methodContentFormat,
                        parameterName,
                        parameterName,
                        annotationValue);
            }

            // 必须是同一个包
            JavaFile.builder(className.packageName(),
                    TypeSpec.classBuilder(className.simpleName() + "$$ViewBinder")
                            .addSuperinterface(typeName)
                            .addMethod(methodBuilder.build())
                            .build())
                    .build()
                    .writeTo(filer);

        }

    }

    private void buildMap(Set<? extends Element> elements) {
        if (!elements.isEmpty()) {
            for (Element element : elements) {
                // 属性注解， 其父节点是类注解
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                if (tmpBindViewMap.containsKey(enclosingElement)) {
                    tmpBindViewMap.get(enclosingElement).add(element);
                } else {
                    List<Element> fields = new ArrayList<>();
                    fields.add(element);
                    tmpBindViewMap.put(enclosingElement, fields);
                }
            }
        }
    }
}
