package jp.hisano.wasm.interpreter;

import java.util.ArrayList;
import java.util.List;

final class Module {
	private List<FunctionType> functionTypes = new ArrayList<>();

	void addFunctionType(ValueType[] parameterTypes, ValueType[] returnTypes) {
		functionTypes.add(new FunctionType(parameterTypes, returnTypes));
	}

	public enum ValueType {
		I32, I64, F32, F64
	}

	private class FunctionType {
		private final ValueType[] parameterTypes;
		private final ValueType[] returnTypes;

		FunctionType(ValueType[] parameterTypes, ValueType[] returnTypes) {
			this.parameterTypes = parameterTypes;
			this.returnTypes = returnTypes;
		}
	}
}
