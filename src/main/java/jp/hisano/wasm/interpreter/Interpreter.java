package jp.hisano.wasm.interpreter;

public final class Interpreter {
	private final Module module;

	public Interpreter(byte[] wasmFileContent) {
		module = new Parser(wasmFileContent).parseModule();
	}

	public <T> T invoke(String name, Object... parameters) {
		return (T) module.getExportedFunction(name).invoke(parameters);
	}
}
