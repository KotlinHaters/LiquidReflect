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
        ClassProcessor loadHook;
        Environment.addClassProcessor(loadHook = (cls, data) -> {
            for (Transformer transformer : transformerMap.values()) {
                if (cls.equals( transformer.getTransformTarget().getName())) {
                    try {
                        if (data.length == 1 || data.length == 0) {
                            LiquidBounce.instance.log("Failed to transform class: " + transformer.getTransformTarget() + "(Length: " + data.length + ") !too short!");
                            return data;
                        } else {
                            LiquidBounce.instance.log("Buffer: " + data.length);
                        }
                        ClassNode node = ClassNodeUtils.toClassNode(data);
                        transformer.transform(node);
                        return ClassNodeUtils.toBytes(node, transformer.computeFrame);
                    } catch (Exception e) {
                        LiquidBounce.instance.log(e.getMessage());
                        e.printStackTrace(System.err);
                    }
                }
            }
            return data;
        });
        transformerMap.forEach((s, transformer) -> NativeWrapper.retransformClass(transformer.getTransformTarget()));
        Environment.removeClassProcessor(loadHook);
    }

    private void addTransformer(Transformer transformer) {
        transformerMap.put(transformer.getTransformTarget().getName(), transformer);
    }


}
