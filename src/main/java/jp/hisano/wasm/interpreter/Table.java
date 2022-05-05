package jp.hisano.wasm.interpreter;

import jp.hisano.wasm.interpreter.Module.Function;
import jp.hisano.wasm.interpreter.Module.TableType;

final class Table {
	private final Module module;
	private final Function[] functions;

	Table(Module module, TableType tableType) {
		this.module = module;
		if (tableType.getElementType() == Module.ValueType.FUNCREF) {
			functions = new Function[tableType.getMinimumElementLength()];
		} else {
			functions = new Function[0];
		}
	}

	void setElements(int offset, int[] elements) {
		for (int i = 0, length = elements.length; i < length; i++) {
			functions[offset + i] = module.getFunction(elements[i]);
		}
	}

	Function getFunction(int index) {
		return functions[index];
	}
}
