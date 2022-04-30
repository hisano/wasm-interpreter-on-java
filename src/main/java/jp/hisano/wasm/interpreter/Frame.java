package jp.hisano.wasm.interpreter;

import jp.hisano.wasm.interpreter.Module.Function;

final class Frame {
	private final int[] localVariables;

	private final int[] stack = new int[256];
	private int stackIndex = 0;
	private int exitDepth;

	Frame(Function function) {
		localVariables = new int[function.parameterTypes.length];
	}

	int pop() {
		return stack[--stackIndex];
	}

	void push(int value) {
		stack[stackIndex++] = value;
	}

	void setLocalVariable(int index, int value) {
		localVariables[index] = value;
	}

	int getLocalVariable(int index) {
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
