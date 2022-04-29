package jp.hisano.wasm.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Module {
	private final List<FunctionType> functionTypes = new ArrayList<>();
	private final List<Function> functions = new ArrayList<>();
	private final Map<String, ExportedFunction> exportedFunctions = new HashMap<>();

	void addFunctionType(ValueType[] parameterTypes, ValueType[] returnTypes) {
		functionTypes.add(new FunctionType(parameterTypes, returnTypes));
	}

	void addFunction(int typeIndex) {
		FunctionType functionType = functionTypes.get(typeIndex);
		functions.add(new Function(functionType.parameterTypes, functionType.returnTypes));
	}

	void addExportedFunction(String name, int functionIndex) {
		exportedFunctions.put(name, new ExportedFunction(functions.get(functionIndex)));
	}

	Function getFunction(int functionIndex) {
		return functions.get(functionIndex);
	}

	ExportedFunction getExportedFunction(String name) {
		return exportedFunctions.get(name);
	}

	enum ValueType {
		I32, I64, F32, F64
	}

	class Function {
		final ValueType[] parameterTypes;
		final ValueType[] returnTypes;

		ValueType[] localTypes;
		byte[] instructions;

		Function(ValueType[] parameterTypes, ValueType[] returnTypes) {
			this.parameterTypes = parameterTypes;
			this.returnTypes = returnTypes;
		}

		void setBody(ValueType[] localTypes, byte[] instructions) {
			this.localTypes = localTypes;
			this.instructions = instructions;
		}
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
