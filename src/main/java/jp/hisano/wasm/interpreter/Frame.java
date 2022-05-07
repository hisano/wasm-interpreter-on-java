package jp.hisano.wasm.interpreter;

import java.util.LinkedList;
import java.util.List;
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
			List<LocalVariable> localVariables = new LinkedList<>();
			for (int i = 0, length = function.parameterTypes.length; i < length; i++) {
				localVariables.add(new LocalVariable(function.parameterTypes[i]));
			}
			for (int i = 0, length = function.locals.length; i < length; i++) {
				for (int j = 0; j < function.locals[i].getCount(); j++) {
					localVariables.add(new LocalVariable(function.locals[i].getType()));
				}
			}
			this.localVariables = localVariables.toArray(new LocalVariable[localVariables.size()]);
		}
	}

	Instance getInstance() {
		return instance;
	}

	Value pop() {
		return stack.pop();
	}

	void pushI32(int i32Value) {
		stack.push(new Value(i32Value));
	}

	void pushI64(long i64Value) {
		stack.push(new Value(i64Value));
	}

	void pushF32(float f32Value) {
		stack.push(new Value(f32Value));
	}

	void pushF64(double f64Value) {
		stack.push(new Value(f64Value));
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
