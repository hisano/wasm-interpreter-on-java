package jp.hisano.wasm.interpreter;

import jp.hisano.wasm.interpreter.Module.Function;

public final class ExportedFunction {
	private final Function function;

	ExportedFunction(Function function) {
		this.function = function;
	}

	public Object invoke(Object... parameters) {
		return 0;
	}
}
