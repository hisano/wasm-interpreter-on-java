package jp.hisano.wasm.interpreter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.*;
import static jp.hisano.wasm.interpreter.InterpreterException.Type.*;
import jp.hisano.wasm.interpreter.Module.AbstractBlock;
import jp.hisano.wasm.interpreter.Module.Block;
import jp.hisano.wasm.interpreter.Module.BlockEndMarker;
import jp.hisano.wasm.interpreter.Module.Br;
import jp.hisano.wasm.interpreter.Module.BrIf;
import jp.hisano.wasm.interpreter.Module.BrTable;
import jp.hisano.wasm.interpreter.Module.Call;
import jp.hisano.wasm.interpreter.Module.CallIndirect;
import jp.hisano.wasm.interpreter.Module.Drop;
import jp.hisano.wasm.interpreter.Module.Else;
import jp.hisano.wasm.interpreter.Module.End;
import jp.hisano.wasm.interpreter.Module.F32Abs;
import jp.hisano.wasm.interpreter.Module.F32Add;
import jp.hisano.wasm.interpreter.Module.F32Ceil;
import jp.hisano.wasm.interpreter.Module.F32Const;
import jp.hisano.wasm.interpreter.Module.F32Copysign;
import jp.hisano.wasm.interpreter.Module.F32Div;
import jp.hisano.wasm.interpreter.Module.F32Eq;
import jp.hisano.wasm.interpreter.Module.F32Floor;
import jp.hisano.wasm.interpreter.Module.F32Ge;
import jp.hisano.wasm.interpreter.Module.F32Gt;
import jp.hisano.wasm.interpreter.Module.F32Le;
import jp.hisano.wasm.interpreter.Module.F32Lt;
import jp.hisano.wasm.interpreter.Module.F32Max;
import jp.hisano.wasm.interpreter.Module.F32Min;
import jp.hisano.wasm.interpreter.Module.F32Mul;
import jp.hisano.wasm.interpreter.Module.F32Ne;
import jp.hisano.wasm.interpreter.Module.F32Nearest;
import jp.hisano.wasm.interpreter.Module.F32Neg;
import jp.hisano.wasm.interpreter.Module.F32Sqrt;
import jp.hisano.wasm.interpreter.Module.F32Sub;
import jp.hisano.wasm.interpreter.Module.F32Trunc;
import jp.hisano.wasm.interpreter.Module.F64Abs;
import jp.hisano.wasm.interpreter.Module.F64Add;
import jp.hisano.wasm.interpreter.Module.F64Ceil;
import jp.hisano.wasm.interpreter.Module.F64Const;
import jp.hisano.wasm.interpreter.Module.F64Copysign;
import jp.hisano.wasm.interpreter.Module.F64Div;
import jp.hisano.wasm.interpreter.Module.F64Eq;
import jp.hisano.wasm.interpreter.Module.F64Floor;
import jp.hisano.wasm.interpreter.Module.F64Ge;
import jp.hisano.wasm.interpreter.Module.F64Gt;
import jp.hisano.wasm.interpreter.Module.F64Le;
import jp.hisano.wasm.interpreter.Module.F64Lt;
import jp.hisano.wasm.interpreter.Module.F64Max;
import jp.hisano.wasm.interpreter.Module.F64Min;
import jp.hisano.wasm.interpreter.Module.F64Mul;
import jp.hisano.wasm.interpreter.Module.F64Ne;
import jp.hisano.wasm.interpreter.Module.F64Nearest;
import jp.hisano.wasm.interpreter.Module.F64Neg;
import jp.hisano.wasm.interpreter.Module.F64Sqrt;
import jp.hisano.wasm.interpreter.Module.F64Sub;
import jp.hisano.wasm.interpreter.Module.F64Trunc;
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
import jp.hisano.wasm.interpreter.Module.I32Eq;
import jp.hisano.wasm.interpreter.Module.I32Eqz;
import jp.hisano.wasm.interpreter.Module.I32Extend16S;
import jp.hisano.wasm.interpreter.Module.I32Extend8S;
import jp.hisano.wasm.interpreter.Module.I32GeS;
import jp.hisano.wasm.interpreter.Module.I32GeU;
import jp.hisano.wasm.interpreter.Module.I32GtS;
import jp.hisano.wasm.interpreter.Module.I32GtU;
import jp.hisano.wasm.interpreter.Module.I32LeS;
import jp.hisano.wasm.interpreter.Module.I32LeU;
import jp.hisano.wasm.interpreter.Module.I32Load8S;
import jp.hisano.wasm.interpreter.Module.I32Load8U;
import jp.hisano.wasm.interpreter.Module.I32LtS;
import jp.hisano.wasm.interpreter.Module.I32LtU;
import jp.hisano.wasm.interpreter.Module.I32Mul;
import jp.hisano.wasm.interpreter.Module.I32Ne;
import jp.hisano.wasm.interpreter.Module.I32Or;
import jp.hisano.wasm.interpreter.Module.I32Popcnt;
import jp.hisano.wasm.interpreter.Module.I32RemS;
import jp.hisano.wasm.interpreter.Module.I32RemU;
import jp.hisano.wasm.interpreter.Module.I32Rotl;
import jp.hisano.wasm.interpreter.Module.I32Rotr;
import jp.hisano.wasm.interpreter.Module.I32Shl;
import jp.hisano.wasm.interpreter.Module.I32ShrS;
import jp.hisano.wasm.interpreter.Module.I32ShrU;
import jp.hisano.wasm.interpreter.Module.I32Sub;
import jp.hisano.wasm.interpreter.Module.I32Xor;
import jp.hisano.wasm.interpreter.Module.I64Add;
import jp.hisano.wasm.interpreter.Module.I64And;
import jp.hisano.wasm.interpreter.Module.I64Clz;
import jp.hisano.wasm.interpreter.Module.I64Const;
import jp.hisano.wasm.interpreter.Module.I64Ctz;
import jp.hisano.wasm.interpreter.Module.I64DivS;
import jp.hisano.wasm.interpreter.Module.I64DivU;
import jp.hisano.wasm.interpreter.Module.I64Eq;
import jp.hisano.wasm.interpreter.Module.I64Eqz;
import jp.hisano.wasm.interpreter.Module.I64Extend16S;
import jp.hisano.wasm.interpreter.Module.I64Extend32S;
import jp.hisano.wasm.interpreter.Module.I64Extend8S;
import jp.hisano.wasm.interpreter.Module.I64GtS;
import jp.hisano.wasm.interpreter.Module.I64LeS;
import jp.hisano.wasm.interpreter.Module.I64LeU;
import jp.hisano.wasm.interpreter.Module.I64LtS;
import jp.hisano.wasm.interpreter.Module.I64LtU;
import jp.hisano.wasm.interpreter.Module.I64Mul;
import jp.hisano.wasm.interpreter.Module.I64Ne;
import jp.hisano.wasm.interpreter.Module.I64Or;
import jp.hisano.wasm.interpreter.Module.I64Popcnt;
import jp.hisano.wasm.interpreter.Module.I64RemS;
import jp.hisano.wasm.interpreter.Module.I64RemU;
import jp.hisano.wasm.interpreter.Module.I64Rotl;
import jp.hisano.wasm.interpreter.Module.I64Rotr;
import jp.hisano.wasm.interpreter.Module.I64Shl;
import jp.hisano.wasm.interpreter.Module.I64ShrS;
import jp.hisano.wasm.interpreter.Module.I64ShrU;
import jp.hisano.wasm.interpreter.Module.I64Sub;
import jp.hisano.wasm.interpreter.Module.I64Xor;
import jp.hisano.wasm.interpreter.Module.If;
import jp.hisano.wasm.interpreter.Module.Instruction;
import jp.hisano.wasm.interpreter.Module.Kind;
import jp.hisano.wasm.interpreter.Module.LocalGet;
import jp.hisano.wasm.interpreter.Module.Loop;
import jp.hisano.wasm.interpreter.Module.RefNull;
import jp.hisano.wasm.interpreter.Module.Return;
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
					parseTableSection(module);
					break;
				case 0x05:
					parseMemorySection(module);
					break;
				case 0x06:
					parseGlobalSection(module);
					break;
				case 0x07:
					parseExportSection(module);
					break;
				case 0x09:
					parseElementSection(module);
					break;
				case 0x0A:
					parseCodeSection(module);
					break;
				case 0x0B:
					parseDataSection(module);
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

	private void parseDataSection(Module module) {
		for (int i = 0, length = byteBuffer.readVaruint32(); i < length; i++) {
			int memoryIndex = byteBuffer.readVaruint32();
			List<Instruction> offsetInstructions = parseInstructions(null, null);
			byte[] data = byteBuffer.readInt8Array();
			module.addMemoryData(memoryIndex, offsetInstructions, data);
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
			case 0x11:
				return new CallIndirect(module, byteBuffer.readVaruint32(), byteBuffer.readVaruint32());

			case 0x2c:
				return new I32Load8S(byteBuffer.readVaruint32(), byteBuffer.readVaruint32());
			case 0x2d:
				return new I32Load8U(byteBuffer.readVaruint32(), byteBuffer.readVaruint32());

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
			case 0x46:
				return new I32Eq();
			case 0x47:
				return new I32Ne();
			case 0x48:
				return new I32LtS();
			case 0x49:
				return new I32LtU();
			case 0x4a:
				return new I32GtS();
			case 0x4b:
				return new I32GtU();
			case 0x4c:
				return new I32LeS();
			case 0x4d:
				return new I32LeU();
			case 0x4e:
				return new I32GeS();
			case 0x4f:
				return new I32GeU();

			case 0x50:
				return new I64Eqz();
			case 0x51:
				return new I64Eq();
			case 0x52:
				return new I64Ne();
			case 0x53:
				return new I64LtS();
			case 0x54:
				return new I64LtU();
			case 0x55:
				return new I64GtS();
			case 0x57:
				return new I64LeS();
			case 0x58:
				return new I64LeU();

			case 0x5b:
				return new F32Eq();
			case 0x5c:
				return new F32Ne();
			case 0x5d:
				return new F32Lt();
			case 0x5e:
				return new F32Gt();
			case 0x5f:
				return new F32Le();
			case 0x60:
				return new F32Ge();

			case 0x61:
				return new F64Eq();
			case 0x62:
				return new F64Ne();
			case 0x63:
				return new F64Lt();
			case 0x64:
				return new F64Gt();
			case 0x65:
				return new F64Le();
			case 0x66:
				return new F64Ge();

			case 0x67:
				return new I32Clz();
			case 0x68:
				return new I32Ctz();
			case 0x69:
				return new I32Popcnt();
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
				return new I32Rotl();
			case 0x78:
				return new I32Rotr();

			case 0x79:
				return new I64Clz();
			case 0x7a:
				return new I64Ctz();
			case 0x7b:
				return new I64Popcnt();

			case 0x7c:
				return new I64Add();
			case 0x7d:
				return new I64Sub();
			case 0x7e:
				return new I64Mul();
			case 0x7f:
				return new I64DivS();
			case 0x80:
				return new I64DivU();
			case 0x81:
				return new I64RemS();
			case 0x82:
				return new I64RemU();
			case 0x83:
				return new I64And();
			case 0x84:
				return new I64Or();
			case 0x85:
				return new I64Xor();
			case 0x86:
				return new I64Shl();
			case 0x87:
				return new I64ShrS();
			case 0x88:
				return new I64ShrU();
			case 0x89:
				return new I64Rotl();
			case 0x8a:
				return new I64Rotr();

			case 0x8b:
				return new F32Abs();
			case 0x8c:
				return new F32Neg();
			case 0x8d:
				return new F32Ceil();
			case 0x8e:
				return new F32Floor();
			case 0x8f:
				return new F32Trunc();
			case 0x90:
				return new F32Nearest();
			case 0x91:
				return new F32Sqrt();

			case 0x92:
				return new F32Add();
			case 0x93:
				return new F32Sub();
			case 0x94:
				return new F32Mul();
			case 0x95:
				return new F32Div();
			case 0x96:
				return new F32Min();
			case 0x97:
				return new F32Max();
			case 0x98:
				return new F32Copysign();

			case 0x99:
				return new F64Abs();
			case 0x9a:
				return new F64Neg();
			case 0x9b:
				return new F64Ceil();
			case 0x9c:
				return new F64Floor();
			case 0x9d:
				return new F64Trunc();
			case 0x9e:
				return new F64Nearest();
			case 0x9f:
				return new F64Sqrt();

			case 0xa0:
				return new F64Add();
			case 0xa1:
				return new F64Sub();
			case 0xa2:
				return new F64Mul();
			case 0xa3:
				return new F64Div();
			case 0xa4:
				return new F64Min();
			case 0xa5:
				return new F64Max();
			case 0xa6:
				return new F64Copysign();

			case 0xc0:
				return new I32Extend8S();
			case 0xc1:
				return new I32Extend16S();

			case 0xc2:
				return new I64Extend8S();
			case 0xc3:
				return new I64Extend16S();
			case 0xc4:
				return new I64Extend32S();

			case 0xd0:
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

	private void parseElementSection(Module module) {
		for (int i = 0, length = byteBuffer.readVaruint32(); i < length; i++) {
			int tableIndex = byteBuffer.readVaruint32();
			List<Instruction> offsetInstructions = parseInstructions(null, null);
			if (module.getTableType(tableIndex).getElementType() == FUNCREF) {
				int[] elements = byteBuffer.readVaruint32Array();
				module.addElementType(tableIndex, offsetInstructions, elements);
			}
		}
	}

	private void parseFunctionSection(Module module) {
		for (int i = 0, length = byteBuffer.readVaruint32(); i < length; i++) {
			module.addFunction(byteBuffer.readVaruint32());
		}
	}

	private void parseTableSection(Module module) {
		for (int i = 0, length = byteBuffer.readVaruint32(); i < length; i++) {
			ValueType elementType = toValueType(byteBuffer.readVaruint7());
			int flags = byteBuffer.readVaruint32();
			if ((flags & 0x01) != 0) {
				module.addTableType(elementType, byteBuffer.readVaruint32(), byteBuffer.readVaruint32());
			} else {
				module.addTableType(elementType, byteBuffer.readVaruint32(), MAX_VALUE);
			}
		}
	}

	private void parseMemorySection(Module module) {
		for (int i = 0, length = byteBuffer.readVaruint32(); i < length; i++) {
			int flags = byteBuffer.readVaruint32();
			if ((flags & 0x01) != 0) {
				module.addMemoryType(byteBuffer.readVaruint32(), byteBuffer.readVaruint32());
			} else {
				module.addMemoryType(byteBuffer.readVaruint32(), MAX_VALUE / 64 / 1024);
			}
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
