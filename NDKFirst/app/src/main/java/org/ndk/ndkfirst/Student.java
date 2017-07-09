package org.ndk.ndkfirst;

import android.util.Log;

public class Student {

    private static final String TAG = "Student";

    private int age = 233;

    private String name ="undefine";

    public Student() {
        Log.i(TAG, "Student: 无参构造函数");
    }

    public Student(int age, String name) {
        this.age = age;
        this.name = name;
        Log.i(TAG, "Student: age = " + age + ",name = " + name);
    }

    @Override
    public String toString() {
        return "Student{" +
                "age=" + age +
                ", name='" + name + '\'' +
                '}';
    }
}
