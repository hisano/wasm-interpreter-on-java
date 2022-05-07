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
		setI32(i32Value);
	}

	Value(long i64Value) {
		this(I64);
		setI64(i64Value);
	}

	Value(float f32Value) {
		this(F32);
		setF32(f32Value);
	}

	Value(double f64Value) {
		this(F64);
		setF64(f64Value);
	}

	ValueType getType() {
		return type;
	}

	void setI32(int newValue) {
		assureType(I32);
		i32Value = newValue;
	}

	int getI32() {
		assureType(I32);
		return i32Value;
	}

	void setI64(long newValue) {
		assureType(I64);
		i64Value = newValue;
	}

	long getI64() {
		assureType(I64);
		return i64Value;
	}

	void setF32(float newValue) {
		assureType(F32);
		f32Value = newValue;
	}

	float getF32() {
		assureType(F32);
		return f32Value;
	}

	void setF64(double newValue) {
		assureType(F64);
		f64Value = newValue;
	}

	double getF64() {
		assureType(F64);
		return f64Value;
	}

	private void assureType(ValueType type) {
		if (this.type != type) {
			throw new TrapException("type mismatch");
		}
	}
}
