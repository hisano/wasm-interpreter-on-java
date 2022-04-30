package jp.hisano.wasm.interpreter;

import jp.hisano.wasm.interpreter.Module.Function;

public final class ExportedFunction {
	private final Module module;
	private final Function function;

	ExportedFunction(Module module, Function function) {
		this.module = module;
		this.function = function;
	}

	public Object invoke(Object... parameters) {
		module.prepareGlobalVariables();

		Frame frame = new Frame(module, function);
		for (int i = 0; i < parameters.length; i++) {
			Object parameter = parameters[i];
			if (parameter instanceof Integer) {
				frame.getLocalVariable(i).setI32((Integer)parameter);
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
