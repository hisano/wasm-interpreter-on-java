package jp.hisano.wasm.interpreter;

public final class Interpreter {
	private final Module module;

	public Interpreter(byte[] wasmBinary) {
		module = new Parser(wasmBinary).parseModule();
	}

	public ExportedFunction getExportedFunction(String name) {
		return null;
	}
}
