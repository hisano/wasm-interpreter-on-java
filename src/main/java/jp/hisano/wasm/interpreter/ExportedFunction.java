package jp.hisano.wasm.interpreter;

import jp.hisano.wasm.interpreter.Module.Function;

public final class ExportedFunction {
	private final Function function;

	ExportedFunction(Function function) {
		this.function = function;
	}

	public Object invoke(Object... parameters) {
		Frame frame = new Frame(function);
		for (int i = 0; i < parameters.length; i++) {
			Object parameter = parameters[i];
			if (parameter instanceof Integer) {
				frame.setLocalVariable(i, (Integer)parameter);
			}
		}

		function.execute(frame);

		if (function.returnTypes.length == 0) {
			return null;
		}

		switch (function.returnTypes[0]) {
			case I32:
				return frame.pop();

			default:
				return null;
		}
	}
}
