package dncli.utils;

import jdk.nashorn.api.scripting.JSObject;

import javax.script.*;
import java.io.File;
import java.io.FileReader;

/**
 * Created by Benjamin Lei on 11/2/2015.
 */
public class JSUtils {
    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("nashorn");
    private static final Invocable invocable = (Invocable) scriptEngine;
    static {
        try {
            Compilable compilable = (Compilable)scriptEngine;
            compilable.compile("function newJSObject() {return {}}\nfunction newJSArray() {return []}").eval();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static JSObject newObject() throws Exception {
        return (JSObject)scriptEngine.eval("({})");
    }

    public static JSObject newArray() throws Exception {
        return (JSObject)scriptEngine.eval("([])");
    }

    public static void push(JSObject arr, Object obj) throws Exception{
        invocable.invokeMethod(arr, "push", obj);
    }

    public static ScriptEngine compile(File script) throws Exception {
        CompiledScript compiledScript = ((Compilable)scriptEngine).compile(new FileReader(script));
        compiledScript.eval();
        return compiledScript.getEngine();
    }

    public static int sizeOf(JSObject object) {
        if (object.isArray()) {
            return (Integer)object.getMember("length");
        } else {
            return object.keySet().size();
        }
    }
}
