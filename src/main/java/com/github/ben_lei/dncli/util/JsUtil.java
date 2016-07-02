package com.github.ben_lei.dncli.util;

import jdk.nashorn.api.scripting.JSObject;

import javax.script.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by blei on 6/19/16.
 */
public class JsUtil {
    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("nashorn");
    private static final Invocable invocable = (Invocable) scriptEngine;

    /**
     * <p>A helper method to compile a javascript file and return
     * an engine that is common.</p>
     *
     * @param script the javascript file
     * @return the script engine
     * @throws FileNotFoundException if file was not found
     * @throws ScriptException       if cannot eval the javascript file
     */
    public static Invocable compileAndEval(File script) throws ScriptException, FileNotFoundException {
        CompiledScript compiledScript = ((Compilable) scriptEngine).compile(new FileReader(script));
        compiledScript.eval();
        return (Invocable) compiledScript.getEngine();
    }

    /**
     * <p>Gets a new JS dictionary</p>
     *
     * @return the javascript dictionary from a base script engine
     */
    public static JSObject newObject() {
        try {
            return (JSObject) scriptEngine.eval("({})");
        } catch (ScriptException e) {
            return null;
        }
    }

    /**
     * <p>Gets a new JS array</p>
     *
     * @return the javascript array from a base script engine
     */
    public static JSObject newArray() {
        try {
            return (JSObject) scriptEngine.eval("([])");
        } catch (ScriptException e) {
            return null;
        }
    }

    /**
     * <p>Pushes an object into a javascript array</p>
     *
     * @param arr the javascrpt array from a base script engine
     * @param obj the object to push into the array
     * @throws ScriptException       if cannot push object into array
     * @throws NoSuchMethodException if the push method doesn't existe in the array
     */
    public static void push(JSObject arr, Object obj) throws ScriptException, NoSuchMethodException {
        invocable.invokeMethod(arr, "push", obj);
    }

    /**
     * <p>Converts a number to an integer</p>
     *
     * @param o the js object
     * @return the number
     */
    public static int numberToInt(Object o) {
        if (o instanceof Long) {
            return ((Long) o).intValue();
        } else {
            return (Integer) o;
        }
    }

    /**
     * <p>Gets the size of a map/array from nashorn.</p>
     *
     * @param object the js object from nashorn
     * @return the size of the js object
     */
    public static int sizeOf(JSObject object) {
        if (object.isArray()) {
            return numberToInt(object.getMember("length"));
        } else {
            return object.keySet().size();
        }
    }

    /**
     * <p>Copies the fields of the provided method based on its
     * getters.</p>
     *
     * @param o the object
     * @return the js object
     */
    public static JSObject reflect(Object o) {
        Class clazz = o.getClass();
        Method[] methods = clazz.getMethods();
        JSObject jso = newObject();

        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("get")) {
                name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                try {
                    jso.setMember(name, method.invoke(o));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        return jso;
    }

    /**
     * <p>Wraps invoke such that the exception is silent.</p>
     *
     * @param invocable the invocable object
     * @param funcName  the function name
     * @param args      the args for the function
     * @return the return value of the function, or null if it fails
     */
    public static Object invoke(Invocable invocable, String funcName, Object... args) {
        try {
            return invocable.invokeFunction(funcName, args);
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }
}
