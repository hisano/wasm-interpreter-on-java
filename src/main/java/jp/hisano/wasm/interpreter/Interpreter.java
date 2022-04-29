package jp.hisano.wasm.interpreter;

public final class Interpreter {
	private final Module module;

	public Interpreter(byte[] wasmBinary) {
		module = new Parser(wasmBinary).parse();
	}

	public ExportedFunction getExportedFunction(String name) {
		return module.getExportedFunction(name);
	}
}
