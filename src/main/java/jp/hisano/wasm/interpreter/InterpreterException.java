package jp.hisano.wasm.interpreter;

public final class InterpreterException extends RuntimeException {
	private Type type;

	InterpreterException(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public enum Type {
		ILLEGAL_BINARY,
	}
}
