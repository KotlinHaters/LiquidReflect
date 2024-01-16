package net.ccbluex.liquidbounce.injection;

import java.util.ArrayList;
import java.util.List;

public class Environment {
    public static List<ClassProcessor> processors = new ArrayList<>();

    public static byte[] process(String name, byte[] bytes){
       for(ClassProcessor processor : processors)
       {
           return processor.process(name,bytes);
       } 
       return bytes;
    }

    public static void addClassProcessor(ClassProcessor classProcessor) {
        processors.add(classProcessor);
    }

    public static void removeClassProcessor(ClassProcessor processor) {
        processors.remove(processor);
    }
}