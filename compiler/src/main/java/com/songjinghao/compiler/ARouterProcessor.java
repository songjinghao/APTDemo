package com.songjinghao.compiler;

import com.google.auto.service.AutoService;
import com.songjinghao.annotation.ARouter;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
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
import javax.tools.JavaFileObject;

/**
 * Created by songjinghao on 2019/8/26.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.songjinghao.annotation.ARouter"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ARouterProcessor extends AbstractProcessor {

    private Messager messager; // 用来报告错误、警告、提示
    private Elements elementsUtils; // 包含了很多的操作Elements的工具方法
    private Filer filer; // 用来创建新的源文件、class文件（造币技术）
    private Types typeUtils; // 包含用于操作TypeMirror工具方法

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementsUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(ARouter.class.getCanonicalName());
        // 也可以对类@SupportedAnnotationTypes({"com.songjinghao.annotation.ARouter"})
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // 返回注解支持的最新版本，JDK
        // 也可以对类@SupportedSourceVersion(SourceVersion.RELEASE_8)
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) return false;

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);
        for (Element element : elements) {
            String packageName = elementsUtils.getPackageOf(element).getQualifiedName().toString();
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被 ARouter 注解的类有：" + className);
            String finalClassName = className + "$$ARouter";

            try {
                MethodSpec hello = MethodSpec.methodBuilder("hello")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class)
                        .addParameter(String.class, "path")
                        .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                        .build();

                TypeSpec typeSpec = TypeSpec.classBuilder(finalClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(hello)
                        .build();

                JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                        .build();

                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*try {
                // 创建一个新的源文件（Class），并返回一个对象以允许写入它
                JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + finalClassName);
                // 定义writer对象，开启写入
                Writer writer = sourceFile.openWriter();

                // 设置包名
                writer.write("package " + packageName + ";\n");

                writer.write("import android.util.Log;\n");
                // 生成类
                writer.write("public class " + finalClassName + " {\n");

                writer.write("public void hello(String path) {");

                writer.write("Log.e(\"sjh\", \"APT~~~\");");

                writer.write("\n}\n}");

                // 别忘记关闭writer
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
        return true;
    }
}
