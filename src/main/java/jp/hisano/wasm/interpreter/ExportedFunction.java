package jp.hisano.wasm.interpreter;

import jp.hisano.wasm.interpreter.Module.Function;

final class ExportedFunction {
	private final Function function;

	ExportedFunction(Function function) {
		this.function = function;
	}

	<T> T invoke(Instance instance, Object... parameters) {
		Frame frame = new Frame(instance, function);
		for (int i = 0; i < parameters.length; i++) {
			Object parameter = parameters[i];
			if (parameter instanceof Integer) {
				frame.getLocalVariable(i).getValue().setI32((Integer)parameter);
			}
		}

		function.invoke(frame);

		if (function.returnTypes.length == 0) {
			return null;
		}

		switch (function.returnTypes[0]) {
			case I32:
				return (T) (Integer)frame.pop();

			default:
				return null;
		}
	}
}
