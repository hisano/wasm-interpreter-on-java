package jp.hisano.wasm.interpreter;

public final class Interpreter {
	private final Instance instance;

	public Interpreter(byte[] wasmFileContent) {
		instance = new Instance(new Module(wasmFileContent));
	}

	public <T> T invoke(String name, Object... parameters) {
		return (T) instance.invoke(name, parameters);
	}
}
