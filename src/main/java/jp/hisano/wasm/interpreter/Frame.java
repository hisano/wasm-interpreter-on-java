package jp.hisano.wasm.interpreter;

import jp.hisano.wasm.interpreter.Module.Function;
import jp.hisano.wasm.interpreter.Module.Value;

final class Frame {
	private final Module module;
	private final Value[] localVariables;

	private final int[] stack = new int[256];
	private int stackIndex = 0;

	Frame(Module module, Function function) {
		this.module = module;

		localVariables = new Value[function.parameterTypes.length];
		for (int i = 0; i < localVariables.length; i++) {
			localVariables[i] = new Value(function.parameterTypes[i]);
		}
	}

	Module getModule() {
		return module;
	}

	int pop() {
		return stack[--stackIndex];
	}

	void push(int value) {
		stack[stackIndex++] = value;
	}

	Value getLocalVariable(int index) {
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
}
