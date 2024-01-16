package net.ccbluex.liquidbounce.injection.mapping;

import net.ccbluex.liquidbounce.LiquidBounce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MappingParser {
    private final String name;
    private final Map<String, String> methods;
    private final Map<String, String> fields;
    private final Map<String, String> classes;

    public MappingParser(String name) {
        this.name = name;
        methods = new HashMap<>();
        fields = new HashMap<>();
        classes = new HashMap<>();
        parse(this.name);
    }

    public String getName() {
        return name;
    }

    public String getMethod(String name) {
        return methods.getOrDefault(name, name);
    }

    public String getField(String name) {
        return fields.getOrDefault(name, name);
    }

    public String getMethodName(String name) {
        String fullName = methods.getOrDefault(name, name);
        String[] parts = fullName.split("/");
        return parts[parts.length - 1];
    }

    public String getFieldName(String name) {
        String fullName = fields.getOrDefault(name, name);
        String[] parts = fullName.split("/");
        return parts[parts.length - 1];
    }

    public String getClass(String name) {
        return classes.getOrDefault(name, name);
    }

    private void parse(String name) {
        if (name.isEmpty()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(LiquidBounce.class.getResourceAsStream("/assets/minecraft/liquidbounce/mappings/" + name))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MD: ")) {
                    String[] parts = line.substring(4).split(" ");
                    if (parts.length == 4) {
                        methods.put(parts[0], parts[2]);
                    }
                } else if (line.startsWith("FD: ")) {
                    String[] parts = line.substring(4).split(" ");
                    if (parts.length == 2) {
                        fields.put(parts[0], parts[1]);
                    }
                } else if (line.startsWith("CL: ")) {
                    String[] parts = line.substring(4).split(" ");
                    if (parts.length == 2) {
                        classes.put(parts[0], parts[1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
