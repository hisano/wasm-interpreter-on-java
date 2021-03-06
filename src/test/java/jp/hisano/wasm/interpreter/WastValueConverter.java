package jp.hisano.wasm.interpreter;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

import static java.lang.Double.*;
import static java.lang.Float.*;
import static java.lang.Integer.*;
import static java.lang.Long.*;

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
					return intBitsToFloat(0xffc0_0000);
				default:
					return parseFloat(value);
			}
		} else if (targetType == double.class) {
			switch (value) {
				case "inf":
					return Double.POSITIVE_INFINITY;
				case "-inf":
					return Double.NEGATIVE_INFINITY;
				case "nan":
					return Double.NaN;
				case "-nan":
					// Suppress canonical NaN
					return (double) intBitsToFloat(0xffc0_0000);
				default:
					return parseDouble(value);
			}
		} else if (targetType == long.class) {
			if (value.startsWith("0x")) {
				return parseUnsignedLong(value.substring(2).replace("_", ""), 16);
			} else if (value.startsWith("-0x")) {
					return -parseUnsignedLong(value.substring(3).replace("_", ""), 16);
			} else {
				return parseLong(value);
			}
		} else if (targetType == int.class) {
			if (value.startsWith("0x")) {
				return parseUnsignedInt(value.substring(2).replace("_", ""), 16);
			} else if (value.startsWith("-0x")) {
				return -parseUnsignedInt(value.substring(3).replace("_", ""), 16);
			} else {
				return parseInt(value);
			}
		}
		throw new ArgumentConversionException("not supported type: " + targetType.getSimpleName());
	}
}
