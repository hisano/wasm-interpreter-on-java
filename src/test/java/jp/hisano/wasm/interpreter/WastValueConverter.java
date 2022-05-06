package jp.hisano.wasm.interpreter;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

class WastValueConverter extends SimpleArgumentConverter {
	@Override
	protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
		String value = source.toString();
		if (targetType == float.class) {
			switch (value) {
				case "inf":
					return Float.POSITIVE_INFINITY;
				case "-inf":
					return Float.NEGATIVE_INFINITY;
				case "nan":
					return Float.NaN; 
				case "-nan":
					// Suppress canonical NaN
					return Float.intBitsToFloat(0xffc0_0000);
				default:
					return Float.valueOf(value);
			}
		}
		throw new ArgumentConversionException("not supported type: " + targetType.getSimpleName());
	}
}
