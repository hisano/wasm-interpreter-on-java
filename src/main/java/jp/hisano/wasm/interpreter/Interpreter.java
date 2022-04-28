package jp.hisano.wasm.interpreter;

public class Interpreter {
	private final byte[] _wasm;

	public Interpreter(byte[] wasm) {
		_wasm = wasm;
	}

	public ExportedFunction getExportedFunction(String name) {
		return null;
	}
}
