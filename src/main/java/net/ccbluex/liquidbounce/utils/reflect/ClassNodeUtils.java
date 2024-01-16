package net.ccbluex.liquidbounce.utils.reflect;

import net.ccbluex.liquidbounce.injection.BytecodeGrabber;
import net.ccbluex.liquidbounce.injection.NativeWrapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class ClassNodeUtils {

    public static ClassNode getClassNode(Class<?> clazz) {
        BytecodeGrabber grabber = new BytecodeGrabber();
        final ClassReader classReader = new ClassReader(grabber.getBytecode(clazz));
        final ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        return classNode;
    }


    public static ClassNode toClassNode(byte[] bytes) {
        final ClassReader classReader = new ClassReader(bytes);
        final ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        return classNode;
    }

    public static byte[] toBytes(ClassNode classNode, boolean compute) {
        final ClassWriter classWriter = new ClassWriter(compute ? ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);

        return classWriter.toByteArray();
    }
}
