package com.songjinghao.library;

/**
 * Created by songjinghao on 2019/8/26.
 */
public class ButterKnife {

    public static void bind(Object object) {
        String className = object.getClass().getName() + "$$ViewBinder";

        try {
            // 类加载
            Class<?> clazz = Class.forName(className);

            // 初始化APT生成类
            ViewBinder viewBinder = (ViewBinder) clazz.newInstance();

            // 执行其中的bind方法
            viewBinder.bind(object);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
