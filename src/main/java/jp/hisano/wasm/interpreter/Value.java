package jp.hisano.wasm.interpreter;

import jp.hisano.wasm.interpreter.Module.ValueType;
import static jp.hisano.wasm.interpreter.Module.ValueType.*;

final class Value {
	private final ValueType type;

	private int i32Value;
	private long i64Value;
	private float f32Value;
	private double f64Value;

	Value(ValueType type) {
		this.type = type;
	}

	Value(int i32Value) {
		this(I32);
		this.i32Value = i32Value; 
	}

	Value(long i64Value) {
		this(I64);
		this.i64Value = i64Value;
	}

	Value(float f32Value) {
		this(F32);
		this.f32Value = f32Value;
	}

	Value(double f64Value) {
		this(F64);
		this.f64Value = f64Value;
	}

	ValueType getType() {
		return type;
	}

	void setI32(int newValue) {
		i32Value = newValue;
	}

	int getI32() {
		return i32Value;
	}

	void setI64(long newValue) {
		i64Value = newValue;
	}

	long getI64() {
		return i64Value;
	}

	void setF32(float newValue) {
		f32Value = newValue;
	}

	float getF32() {
		return f32Value;
	}

	void setF64(double newValue) {
		f64Value = newValue;
	}

	double getF64() {
		return f64Value;
	}
}
