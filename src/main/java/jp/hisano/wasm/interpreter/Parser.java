package jp.hisano.wasm.interpreter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.*;
import static jp.hisano.wasm.interpreter.InterpreterException.Type.*;
import jp.hisano.wasm.interpreter.Module.AbstractBlock;
import jp.hisano.wasm.interpreter.Module.BlockEndMarker;
import jp.hisano.wasm.interpreter.Module.Br;
import jp.hisano.wasm.interpreter.Module.BrIf;
import jp.hisano.wasm.interpreter.Module.BrTable;
import jp.hisano.wasm.interpreter.Module.Call;
import jp.hisano.wasm.interpreter.Module.Drop;
import jp.hisano.wasm.interpreter.Module.Else;
import jp.hisano.wasm.interpreter.Module.End;
import jp.hisano.wasm.interpreter.Module.F32Add;
import jp.hisano.wasm.interpreter.Module.F32Const;
import jp.hisano.wasm.interpreter.Module.F64Add;
import jp.hisano.wasm.interpreter.Module.F64Const;
import jp.hisano.wasm.interpreter.Module.Function;
import jp.hisano.wasm.interpreter.Module.FunctionBlock;
import jp.hisano.wasm.interpreter.Module.GlobalGet;
import jp.hisano.wasm.interpreter.Module.I32Add;
import jp.hisano.wasm.interpreter.Module.I32And;
import jp.hisano.wasm.interpreter.Module.I32Clz;
import jp.hisano.wasm.interpreter.Module.I32Const;
import jp.hisano.wasm.interpreter.Module.I32Ctz;
import jp.hisano.wasm.interpreter.Module.I32DivS;
import jp.hisano.wasm.interpreter.Module.I32DivU;
import jp.hisano.wasm.interpreter.Module.I32Eqz;
import jp.hisano.wasm.interpreter.Module.I32Extend16S;
import jp.hisano.wasm.interpreter.Module.I32Extend8S;
import jp.hisano.wasm.interpreter.Module.I32Mul;
import jp.hisano.wasm.interpreter.Module.I32Or;
import jp.hisano.wasm.interpreter.Module.I32PopCnt;
import jp.hisano.wasm.interpreter.Module.I32RemS;
import jp.hisano.wasm.interpreter.Module.I32RemU;
import jp.hisano.wasm.interpreter.Module.I32RotL;
import jp.hisano.wasm.interpreter.Module.I32RotR;
import jp.hisano.wasm.interpreter.Module.I32Shl;
import jp.hisano.wasm.interpreter.Module.I32ShrS;
import jp.hisano.wasm.interpreter.Module.I32ShrU;
import jp.hisano.wasm.interpreter.Module.I32Sub;
import jp.hisano.wasm.interpreter.Module.I32Xor;
import jp.hisano.wasm.interpreter.Module.I64Add;
import jp.hisano.wasm.interpreter.Module.I64Const;
import jp.hisano.wasm.interpreter.Module.If;
import jp.hisano.wasm.interpreter.Module.Instruction;
import jp.hisano.wasm.interpreter.Module.Kind;
import jp.hisano.wasm.interpreter.Module.LocalGet;
import jp.hisano.wasm.interpreter.Module.Loop;
import jp.hisano.wasm.interpreter.Module.RefNull;
import jp.hisano.wasm.interpreter.Module.Return;
import jp.hisano.wasm.interpreter.Module.Block;
import jp.hisano.wasm.interpreter.Module.Unreachable;
import jp.hisano.wasm.interpreter.Module.ValueType;
import static jp.hisano.wasm.interpreter.Module.ValueType.*;

final class Parser {
	private static final int MAGIC = 0x6d736100; // = "\0asm"
	private static final int VERSION = 1;

	private final ByteBuffer byteBuffer;

	Parser(byte[] byteBuffer) {
		this.byteBuffer = new ByteBuffer(byteBuffer);
	}

	void parseModule(Module module) {
		checkInt(MAGIC);
		checkInt(VERSION);

		while (byteBuffer.canRead()) {
			int section = byteBuffer.readVaruint7();
			int size = byteBuffer.readVaruint32();
			switch (section) {
				case 0x00:
					// ignore custom section
					byteBuffer.skipBytes(size);
					break;
				case 0x01:
					parseTypeSection(module);
					break;
				case 0x02:
					parseImportSection(module);
					break;
				case 0x03:
					parseFunctionSection(module);
					break;
				case 0x04:
					// TODO Tableセクションの読み込み
					byteBuffer.skipBytes(size);
					break;
				case 0x05:
					// TODO Memoryセクションの読み込み
					byteBuffer.skipBytes(size);
					break;
				case 0x06:
					parseGlobalSection(module);
					break;
				case 0x07:
					parseExportSection(module);
					break;
				case 0x09:
					// TODO Elementセクションの読み込み
					byteBuffer.skipBytes(size);
					break;
				case 0x0A:
					parseCodeSection(module);
					break;
				default:
					throw new UnsupportedOperationException("not implemented section (0x" + toHexString(section) + "): readIndex = 0x" + toHexString(byteBuffer.getReadIndex()));
			}
		}
	}

	private void checkInt(int expectedValue) {
		if (byteBuffer.readUint32() != expectedValue) {
			throw new InterpreterException(ILLEGAL_BINARY);
		}
	}

	private void parseCodeSection(Module module) {
		for (int i = 0, length = byteBuffer.readVaruint32(); i < length; i++) {
			int size = byteBuffer.readVaruint32();
			int baseIndex = byteBuffer.getReadIndex();
			ValueType[] localTypes = parseValueTypes();
			int instructionLength = size - (byteBuffer.getReadIndex() - baseIndex);
			byte[] instructions = byteBuffer.readInt8Array(instructionLength);
			Function function = module.getFunction(i);
			function.setBody(localTypes, instructions);
		}
	}

	FunctionBlock parseFunctionBlock(Module module, Function function) {
		FunctionBlock functionBlock = new FunctionBlock(function);
		functionBlock.setInstructions(parseInstructions(module, functionBlock));
		return functionBlock;
	}

	private List<Instruction> parseInstructions(Module module, AbstractBlock parent) {
		List<Instruction> result = new LinkedList<>();
		while (true) {
			Instruction instruction = parseInstruction(module, parent);
			result.add(instruction);
			if (instruction instanceof BlockEndMarker) {
				return result;
			}
		}
	}

	private Instruction parseInstruction(Module module, AbstractBlock parent) {
		int instruction = byteBuffer.readUint8AsInt();
		switch (instruction) {
			case 0x00:
				return new Unreachable();
			case 0x02: {
				Block block = new Block(parent, toValueType(byteBuffer.readVarsint7()));
				block.setInstructions(parseInstructions(module, block));
				return block;
			}
			case 0x03: {
				Loop block = new Loop(parent, toValueType(byteBuffer.readVarsint7()));
				block.setInstructions(parseInstructions(module, block));
				return block;
			}
			case 0x04: {
				If block = new If(parent, toValueType(byteBuffer.readVarsint7()));
				List<Instruction> thenInstructions = parseInstructions(module, block);
				List<Instruction> elseInstructions = null;
				if (thenInstructions.get(thenInstructions.size() - 1) instanceof Else) {
					elseInstructions = parseInstructions(module, block);
				}
				block.setInstructions(thenInstructions, elseInstructions);
				return block;
			}
			case 0x05:
				return new Else();

			case 0x0b:
				return new End();

			case 0x0c:
				return new Br(byteBuffer.readVaruint32());
			case 0x0d:
				return new BrIf(byteBuffer.readVaruint32());
			case 0x0e:
				return new BrTable(byteBuffer.readVaruint32Array(), byteBuffer.readVaruint32());

			case 0x0f:
				return new Return();

			case 0x10:
				return new Call(module.getFunction(byteBuffer.readVaruint32()));

			case 0x1a:
				return new Drop();

			case 0x20:
				return new LocalGet(byteBuffer.readVaruint32());
			case 0x23:
				return new GlobalGet(byteBuffer.readVaruint32());

			case 0x41:
				return new I32Const(byteBuffer.readVarsint32());
			case 0x42:
				return new I64Const(byteBuffer.readVarsint64());
			case 0x43:
				return new F32Const(byteBuffer.readFloat32());
			case 0x44:
				return new F64Const(byteBuffer.readFloat64());

			case 0x45:
				return new I32Eqz();
			case 0x67:
				return new I32Clz();
			case 0x68:
				return new I32Ctz();
			case 0x69:
				return new I32PopCnt();
			case 0x6a:
				return new I32Add();
			case 0x6b:
				return new I32Sub();
			case 0x6c:
				return new I32Mul();
			case 0x6d:
				return new I32DivS();
			case 0x6e:
				return new I32DivU();
			case 0x6f:
				return new I32RemS();
			case 0x70:
				return new I32RemU();
			case 0x71:
				return new I32And();
			case 0x72:
				return new I32Or();
			case 0x73:
				return new I32Xor();
			case 0x74:
				return new I32Shl();
			case 0x75:
				return new I32ShrS();
			case 0x76:
				return new I32ShrU();
			case 0x77:
				return new I32RotL();
			case 0x78:
				return new I32RotR();

			case 0x7c:
				return new I64Add();

			case 0x92:
				return new F32Add();

			case 0xa0:
				return new F64Add();

			case 0xC0:
				return new I32Extend8S();
			case 0xC1:
				return new I32Extend16S();

			case 0xD0:
				return new RefNull(toValueType(byteBuffer.readVarsint7()));
			default:
				throw new UnsupportedOperationException("not implemented instruction: instruction = 0x" + toHexString(instruction) + ", readIndex = " + byteBuffer.getReadIndex());
		}
	}

	private void parseImportSection(Module module) {
		for (int i = 0, length = byteBuffer.readVaruint32(); i < length; i++) {
			String moduleName = byteBuffer.readUtf8();
			String exportName = byteBuffer.readUtf8();
			Kind kind = toKind(byteBuffer.readVaruint7());
			switch (kind) {
				case FUNCTION:
					// TODO Function対応を実装
					int index = byteBuffer.readVaruint32();
					break;
				case TABLE:
					// TODO Table対応を実装
					throw new UnsupportedOperationException();
				case MEMORY:
					// TODO Memory対応を実装
					throw new UnsupportedOperationException();
				case GLOBAL:
					ValueType type = toValueType(byteBuffer.readVarsint7());
					boolean mutability = byteBuffer.readUint1() != 0;
					module.addGlobalVariableType(type, mutability, Collections.emptyList());
					break;
			}
		}
	}

	private void parseGlobalSection(Module module) {
		for (int i = 0, length = byteBuffer.readVaruint32(); i < length; i++) {
			ValueType type = toValueType(byteBuffer.readVarsint7());
			boolean mutability = byteBuffer.readUint1() != 0;
			List<Instruction> instructions = parseInstructions(null, null);
			module.addGlobalVariableType(type, mutability, instructions);
		}
	}

	private void parseExportSection(Module module) {
		for (int i = 0, length = byteBuffer.readVaruint32(); i < length; i++) {
			String name = byteBuffer.readUtf8();
			int kind = byteBuffer.readVaruint7();
			switch (kind) {
				case 0x00:
					module.addExportedFunction(name, byteBuffer.readVaruint32());
					break;

				default:
					throw new UnsupportedOperationException("not implemented kind (0x" + toHexString(kind) + ")");
			}
		}
	}

	private void parseFunctionSection(Module module) {
		for (int i = 0, length = byteBuffer.readVaruint32(); i < length; i++) {
			module.addFunction(byteBuffer.readVaruint32());
		}
	}

	private void parseTypeSection(Module module) {
		for (int i = 0, length = byteBuffer.readVaruint32(); i < length; i++) {
			switch (byteBuffer.readVaruint7()) {
				case 0x60:
					parseFunctionType(module);
					break;
			}
		}
	}

	private void parseFunctionType(Module module) {
		module.addFunctionType(parseValueTypes(), parseValueTypes());
	}

	private ValueType[] parseValueTypes() {
		ValueType[] valueTypes = new ValueType[byteBuffer.readVaruint32()];
		for (int i = 0, length = valueTypes.length; i < length; i++) {
			valueTypes[i] = toValueType(byteBuffer.readVarsint7());
		}
		return valueTypes;
	}

	private static Kind toKind(byte valueOfVaruint7) {
		switch (valueOfVaruint7) {
			case 0x00:
				return Kind.FUNCTION;
			case 0x01:
				return Kind.TABLE;
			case 0x02:
				return Kind.MEMORY;
			case 0x03:
				return Kind.GLOBAL;

			default:
				throw new UnsupportedOperationException("not implemented kind: kind = 0x" + toHexString(valueOfVaruint7));
		}
	} 

	private static ValueType toValueType(byte valueOfVarsin7) {
		switch (valueOfVarsin7) {
			case 0x40:
				return VOID;

			case 0x7f:
				return I32;
			case 0x7e:
				return I64;
			case 0x7d:
				return F32;
			case 0x7c:
				return F64;

			case 0x7b:
				return V128;

			case 0x70:
				return FUNCREF;
			case 0x6f:
				return EXTERNREF;

			default:
				return null;
		}
	}
}
