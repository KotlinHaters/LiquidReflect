package net.ccbluex.liquidbounce.injection;

import net.ccbluex.liquidbounce.injection.transformers.*;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.tree.ClassNode;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.utils.reflect.ClassNodeUtils;

import java.util.HashMap;
import java.util.Map;

public class TransformerManager {

    private static final Map<String, Transformer> transformerMap = new HashMap<>();

    public void registerTransformers() {
        addTransformer(new EntityPlayerSPTransformer());
        addTransformer(new MinecraftTransformer());
        addTransformer(new GuiIngameTransformer());
        addTransformer(new KeyBindingTransformer());
        addTransformer(new EntityRendererTransformer());
        addTransformer(new NetworkManagerTransformer());
        addTransformer(new EntityTransformer());
        for (Transformer transformer : transformerMap.values()) {
            final Class<?> target;
            try {
                target = Class.forName(transformer.getTransformTarget().getName(),true, Minecraft.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            byte[] data = NativeWrapper.getClassBytes(target);
            LiquidBounce.instance.log("Transforming " + target.getSimpleName() + "...");
            try {
                if (data.length == 1) {
                    LiquidBounce.instance.log("Failed to transform class: " + transformer.getTransformTarget() + "(Length: " + data.length + ") !too short!");
                    return;
                }
                ClassNode node = ClassNodeUtils.toClassNode(data);
                transformer.transform(node);
                NativeWrapper.redefineClass(target,ClassNodeUtils.toBytes(node, transformer.computeFrame));
            } catch (Exception e) {
                LiquidBounce.instance.log(e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }

    private void addTransformer(Transformer transformer) {
        transformerMap.put(transformer.getTransformTarget().getName(), transformer);
    }


}
