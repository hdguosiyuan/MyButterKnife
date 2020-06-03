package com.honeywell.butterknife;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class MyButterKnife {

    /**
     * 通过反射的方式运行***$$ViewBinder
     * 达到运行findViewById方法
     * @param activity
     */
    public static void bind(Object activity){
        String name = activity.getClass().getName();
        String bindName = name+"$$ViewBinder";
        try {
            Class<?> clazz = Class.forName(bindName);
            Constructor<?> constructor = clazz.getConstructor(activity.getClass());
            constructor.newInstance(activity);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
