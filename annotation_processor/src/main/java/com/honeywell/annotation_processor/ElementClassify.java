package com.honeywell.annotation_processor;

import com.google.auto.service.AutoService;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * 内部包含一个类中的所有带注解的元素
 */
public class ElementClassify {

    //view的节点list
    public List<VariableElement> viewElements;
    //onclick的节点list
    public List<ExecutableElement> methodElements;
    //string的节点list
    public List<VariableElement> stringElements;

    public List<VariableElement> getStringElements() {
        return stringElements;
    }

    public void setStringElements(List<VariableElement> stringElements) {
        this.stringElements = stringElements;
    }

    public List<VariableElement> getViewElements() {
        return viewElements;
    }

    public void setViewElements(List<VariableElement> viewElements) {
        this.viewElements = viewElements;
    }

    public List<ExecutableElement> getMethodElements() {
        return methodElements;
    }

    public void setMethodElements(List<ExecutableElement> methodElements) {
        this.methodElements = methodElements;
    }
}
