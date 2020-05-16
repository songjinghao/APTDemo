package com.songjinghao.library;

/**
 * Created by songjinghao on 2019/8/26.
 */
public interface ViewBinder<T extends Object> {

    void bind(T target);
}
