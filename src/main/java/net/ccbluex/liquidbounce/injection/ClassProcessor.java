package net.ccbluex.liquidbounce.injection;

public interface ClassProcessor {
    byte[] process(String name, byte[] bytes);
}