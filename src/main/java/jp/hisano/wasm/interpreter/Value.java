package jp.hisano.wasm.interpreter;

final class Value {
	private int i32Value;
	private long i64Value;
	private float f32Value;
	private double f64Value;

	void setI32(int newValue) {
		i32Value = newValue;
	}

	int getI32() {
		return i32Value;
	}
}
