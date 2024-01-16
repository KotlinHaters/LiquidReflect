package net.ccbluex.liquidbounce.injection;

import java.security.ProtectionDomain;

import java.util.HashMap;

public class BytecodeGrabber implements ClassProcessor {

	private final HashMap<String, byte[]> map = new HashMap<>();

	public BytecodeGrabber() {
		Environment.addClassProcessor(this);
	}

	public byte[] getBytecode(Class<?> cls) {
		String name = cls.getName().replace('.', '/');
		map.put(name, null);
		NativeWrapper.retransformClass(cls);
		byte[] bytecode = map.get(name);
		map.remove(name);
		Environment.removeClassProcessor(this);
		return bytecode;
	}
	
	@Override
	public byte[] process(String name, byte[] data) {
		if(map.containsKey(name)) 
			map.put(name, data);
		return data;
	}
}
