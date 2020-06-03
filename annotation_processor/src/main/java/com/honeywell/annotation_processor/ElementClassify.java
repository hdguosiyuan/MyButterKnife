package com.honeywell.annotation_processor;

import com.google.auto.service.AutoService;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;


public class ElementClassify {

    public List<VariableElement> viewElements;
    public List<ExecutableElement> methodElements;
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
