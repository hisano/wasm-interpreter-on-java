package jp.hisano.wasm.interpreter;

public final class TrapException extends RuntimeException {
	TrapException(String message) {
		super(message);
	}

	TrapException(String message, Throwable cause) {
		super(message, cause);
	}
}
