package com.honeywell.annotation_processor;

import com.google.auto.service.AutoService;
import com.honeywell.annotation.BindString;
import com.honeywell.annotation.BindView;
import com.honeywell.annotation.OnClick;

import java.io.IOError;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

    private Filer filer;

    public void logUtil(String message) {
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        logUtil("processor init ============================");
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        logUtil("processor process ============================");
        Map<TypeElement, ElementClassify> parseTargets = classifyElementWithClass(roundEnvironment);
        //如果map为空说明没有注解 什么都不用做
        if (parseTargets.size() <= 0) {
            return false;
        }
        Iterator<TypeElement> iterator = parseTargets.keySet().iterator();
        String newClassName;
        String packageName;
        Writer writer = null;
        //遍历map  为每一个acitvity生成$$ViewBinder文件
        while (iterator.hasNext()) {
            TypeElement classElement = iterator.next();
            ElementClassify elementClassify = parseTargets.get(classElement);
            newClassName = classElement.getSimpleName().toString();
            newClassName = newClassName + "$$ViewBinder";
            packageName = getPackageName(classElement);
            try {
                JavaFileObject javaFileObject = filer.createSourceFile(packageName + "." + newClassName);
                writer = javaFileObject.openWriter();
                StringBuffer stringBuffer = getStringBuffer(packageName, newClassName, classElement, elementClassify);
                writer.write(stringBuffer.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     * 生成新的class文件
     * @param packageName
     * @param newClassName
     * @param classElement
     * @param elementClassify
     * @return
     */
    private StringBuffer getStringBuffer(String packageName, String newClassName, TypeElement classElement,
                                         ElementClassify elementClassify) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("package " + packageName + ";\n");
        stringBuffer.append("import android.view.View;\n");
        stringBuffer.append("import android.util.Log;\n");
        stringBuffer.append("public class " + newClassName + "{\n");
        stringBuffer.append("\tpublic " + newClassName + "(final " + classElement.getQualifiedName() + " target){\n");
        stringBuffer.append("\t\tLog.d(\"gsy\",\"constractor\");\n");
        if (elementClassify != null && elementClassify.getViewElements() != null && elementClassify.getViewElements().size() > 0) {
            List<VariableElement> viewElements = elementClassify.getViewElements();
            for (VariableElement variableElement : viewElements) {
                TypeMirror typeMirror = variableElement.asType();
                Name name = variableElement.getSimpleName();
                int resId = variableElement.getAnnotation(BindView.class).value();
                stringBuffer.append("\t\ttarget." + name + " =(" + typeMirror + ")target.findViewById(" + resId + ");\n");
            }
        }
        if (elementClassify != null && elementClassify.getMethodElements() != null && elementClassify.getMethodElements().size() > 0) {
            List<ExecutableElement> methodElements = elementClassify.getMethodElements();
            for (ExecutableElement executableElement : methodElements) {
                int[] resIds = executableElement.getAnnotation(OnClick.class).value();
                String methodName = executableElement.getSimpleName().toString();
                for (int id : resIds) {
                    stringBuffer.append("\t\t(target.findViewById(" + id + ")).setOnClickListener(new View.OnClickListener() {\n");
                    stringBuffer.append("\t\t\tpublic void onClick(View p0) {\n");
                    stringBuffer.append("\t\t\t\ttarget." + methodName + "(p0);\n");
                    stringBuffer.append("\t\t\t}\n\t\t});\n");
                }
            }
        }

        if(elementClassify != null && elementClassify.getStringElements() != null && elementClassify.getStringElements().size() > 0){
            List<VariableElement> stringElements = elementClassify.getStringElements();
            for (VariableElement variableElement:stringElements){
                int id = variableElement.getAnnotation(BindString.class).value();
                Name name = variableElement.getSimpleName();
                stringBuffer.append("\t\ttarget."+name+" = target.getResources().getString("+id+");\n");
            }
        }
        stringBuffer.append("\t}\n}\n");
        return stringBuffer;
    }

    /**
     * 获取包名
     * @param classElement
     * @return
     */
    private String getPackageName(Element classElement) {
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(classElement);
        return packageElement.getQualifiedName().toString();
    }

    /**
     * 声明注解器支持的java版本
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion();
    }

    /**
     * 声明要处理的注解
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationSet = new HashSet<>();
        annotationSet.add(BindView.class.getCanonicalName());
        annotationSet.add(OnClick.class.getCanonicalName());
        annotationSet.add(BindString.class.getCanonicalName());
        return annotationSet;
    }

    //TypeElement 对应class ElementClassify 对应class中的注解元素对应的方法或者变量
    private Map<TypeElement, ElementClassify> classifyElementWithClass(RoundEnvironment roundEnvironment) {
        //建立类和方法、变量的对应关系 便于生成新的class文件
        Map<TypeElement, ElementClassify> classElementMap = new HashMap<>();
        //通过roundEnvironment获取到添加了BindView注解的所有元素
        Set<? extends Element> viewElementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        //通过roundEnvironment获取到添加了OnClick注解的所有方法
        Set<? extends Element> methodElementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(OnClick.class);
        //通过roundEnvironment获取到添加了BindString注解的所有元素
        Set<? extends Element> stringElementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(BindString.class);

        //处理view 首先遍历view元素 然后封装到ElementClassify中
        for (Element viewElement : viewElementsAnnotatedWith) {
            //VariableElement可以理解为一个变量元素
            VariableElement variableElement = (VariableElement) viewElement;
            //获取到变量所在的class节点
            TypeElement classElement = (TypeElement) variableElement.getEnclosingElement();
            //先从map中取ElementClassify
            ElementClassify elementClassify = classElementMap.get(classElement);
            //List用来存放注解的对象节点
            List<VariableElement> viewElements;
            if (elementClassify != null) {
                //取出
                viewElements = elementClassify.getViewElements();
                logUtil("view list size="+viewElements.size());
                //如果list为空 新建一个 并放入ElementClassify
                if (viewElements == null) {
                    viewElements = new ArrayList<>();
                    elementClassify.setViewElements(viewElements);
                }
            } else {
                elementClassify = new ElementClassify();
                viewElements = new ArrayList<>();
                elementClassify.setViewElements(viewElements);
                if (!classElementMap.containsKey(classElement)) {
                    //将activity节点和list对应起来
                    logUtil("viewlist size ="+elementClassify.getViewElements().size());
                    classElementMap.put(classElement, elementClassify);
                }
            }
            logUtil(variableElement.getSimpleName().toString());
            //将节点放入List
            viewElements.add(variableElement);
        }

        //处理method
        for (Element methodElement : methodElementsAnnotatedWith) {
            //ExecutableElement元素对应method
            ExecutableElement executableElement = (ExecutableElement) methodElement;
            TypeElement typeElement = (TypeElement) methodElement.getEnclosingElement();
            List<ExecutableElement> methodList;
            ElementClassify elementClassify = classElementMap.get(typeElement);
            if (elementClassify != null) {
                methodList = elementClassify.getMethodElements();
                if (methodList == null) {
                    methodList = new ArrayList<>();
                    elementClassify.setMethodElements(methodList);
                }
            } else {
                elementClassify = new ElementClassify();
                methodList = new ArrayList<>();
                elementClassify.setMethodElements(methodList);
                if (!classElementMap.containsKey(typeElement)) {
                    classElementMap.put(typeElement, elementClassify);
                }
            }
            methodList.add(executableElement);
        }

        //处理string
        for (Element stringElement : stringElementsAnnotatedWith) {
            //VariableElement
            VariableElement variableElement = (VariableElement) stringElement;
            //获得所在的类
            TypeElement typeElement = (TypeElement) stringElement.getEnclosingElement();
            List<VariableElement> stringList;
            ElementClassify elementClassify = classElementMap.get(typeElement);
            if (elementClassify != null) {
                stringList = elementClassify.getStringElements();
                if (stringList == null) {
                    stringList = new ArrayList<>();
                    elementClassify.setStringElements(stringList);
                }
            } else {
                stringList = new ArrayList<>();
                elementClassify = new ElementClassify();
                elementClassify.setStringElements(stringList);
                if(!classElementMap.containsKey(typeElement)){
                    classElementMap.put(typeElement,elementClassify);
                }
            }
            stringList.add(variableElement);
        }

        return classElementMap;
    }

}
