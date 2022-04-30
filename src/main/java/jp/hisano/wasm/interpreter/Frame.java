package jp.hisano.wasm.interpreter;

import java.util.Stack;

import jp.hisano.wasm.interpreter.Module.Function;
import jp.hisano.wasm.interpreter.Module.ValueType;

final class Frame {
	private final Instance instance;
	private final LocalVariable[] localVariables;

	private final Stack<Value> stack = new Stack<>();

	Frame(Instance instance, Function function) {
		this.instance = instance;

		if (function == null) {
			localVariables = new LocalVariable[0];
		} else {
			localVariables = new LocalVariable[function.parameterTypes.length];
			for (int i = 0, length = localVariables.length; i < length; i++) {
				localVariables[i] = new LocalVariable(function.parameterTypes[i]);
			}
		}
	}

	Instance getInstance() {
		return instance;
	}

	Value pop() {
		return stack.pop();
	}

	void pushI32(int value) {
		stack.push(new Value(value));
	}

	void pushI64(long value) {
		stack.push(new Value(value));
	}

	LocalVariable getLocalVariable(int index) {
		return localVariables[index];
	}

	void throwExceptionToExitBlock(int depth) {
		throw new ExceptionToExitBlock(depth);
	}

	static class ExceptionToExitBlock extends RuntimeException {
		private int depth;

		private ExceptionToExitBlock(int depth) {
			this.depth = depth;
		}

		boolean isMoreExitRequired() {
			return depth != 0;
		}

		void decrementDepth() {
			depth--;
		}
	}

	void throwExceptionToReturn() {
		throw new ExceptionToReturn();
	}

	static class ExceptionToReturn extends RuntimeException {
		private ExceptionToReturn() {
		}
	}

	static class LocalVariable {
		private final Value value;

		LocalVariable(ValueType type) {
			value = new Value(type);
		}

		Value getValue() {
			return value;
		}
	}
}
