package net.ccbluex.liquidbounce.injection;

import com.google.gson.JsonParser;
import net.ccbluex.liquidbounce.utils.HttpUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public abstract class Transformer implements Opcodes {

    public boolean computeFrame = false;

    public abstract Class<?> getTransformTarget();

    public abstract void transform(ClassNode classNode);

    protected MethodNode method(ClassNode classNode, String name, String desc) {
        try {
            name = new JsonParser().parse(HttpUtils.getFromURL(new URL("http://localhost:6666/api/getMethod?name=" + Base64.getEncoder().encodeToString((classNode.name + "/" + name).getBytes(StandardCharsets.UTF_8))))).getAsJsonObject().get("result").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String finalName = name;
        return classNode.methods.stream().filter(mn -> mn.name.equals(finalName) && mn.desc.equals(desc)).findFirst().orElse(null);
    }

    protected String getClassSignature(Class<?> clazz) {
        return "L" + Type.getInternalName(clazz) + ";";
    }
}
