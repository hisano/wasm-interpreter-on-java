package jp.hisano.wasm.interpreter;

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
import jp.hisano.wasm.interpreter.Module.Function;
import jp.hisano.wasm.interpreter.Module.FunctionBlock;
import jp.hisano.wasm.interpreter.Module.I32Add;
import jp.hisano.wasm.interpreter.Module.I32Const;
import jp.hisano.wasm.interpreter.Module.I32Ctz;
import jp.hisano.wasm.interpreter.Module.I32DivS;
import jp.hisano.wasm.interpreter.Module.I32Extend16S;
import jp.hisano.wasm.interpreter.Module.I32Extend8S;
import jp.hisano.wasm.interpreter.Module.I32Mul;
import jp.hisano.wasm.interpreter.Module.I32Sub;
import jp.hisano.wasm.interpreter.Module.I32Xor;
import jp.hisano.wasm.interpreter.Module.If;
import jp.hisano.wasm.interpreter.Module.Instruction;
import jp.hisano.wasm.interpreter.Module.LocalGet;
import jp.hisano.wasm.interpreter.Module.Loop;
import jp.hisano.wasm.interpreter.Module.Return;
import jp.hisano.wasm.interpreter.Module.Block;
import jp.hisano.wasm.interpreter.Module.ValueType;
import static jp.hisano.wasm.interpreter.Module.ValueType.*;

final class Parser {
	private static final int MAGIC = 0x6d736100; // = "\0asm"
	private static final int VERSION = 1;

	private final ByteBuffer wasmFileContent;

	private final Module module = new Module();

	Parser(byte[] wasmFileContent) {
		this.wasmFileContent = new ByteBuffer(wasmFileContent);
	}

	Module parseModule() {
		checkInt(MAGIC);
		checkInt(VERSION);

		while (wasmFileContent.canRead()) {
			int section = wasmFileContent.readByte();
			int size = wasmFileContent.readUnsignedLeb128();
			switch (section) {
				case 0x00:
					// ignore custom section
					wasmFileContent.skipBytes(size);
					break;
				case 0x01:
					parseTypeSection();
					break;
				case 0x03:
					parseFunctionSection();
					break;
				case 0x04:
					// TODO Tableセクションの読み込み
					wasmFileContent.skipBytes(size);
					break;
				case 0x05:
					// TODO Memoryセクションの読み込み
					wasmFileContent.skipBytes(size);
					break;
				case 0x06:
					// TODO Globalセクションの読み込み
					wasmFileContent.skipBytes(size);
					break;
				case 0x07:
					parseExportSection();
					break;
				case 0x09:
					// TODO Elementセクションの読み込み
					wasmFileContent.skipBytes(size);
					break;
				case 0x0A:
					parseCodeSection();
					break;
				default:
					throw new UnsupportedOperationException("not implemented section (0x" + toHexString(section) + "): readIndex = 0x" + toHexString(wasmFileContent.getPosition()));
			}
		}

		return module;
	}

	private void checkInt(int expectedValue) {
		if (wasmFileContent.readInt() != expectedValue) {
			throw new InterpreterException(ILLEGAL_BINARY);
		}
	}

	private void parseCodeSection() {
		int length = wasmFileContent.readUnsignedLeb128();
		for (int i = 0; i < length; i++) {
			int size = wasmFileContent.readUnsignedLeb128();
			int baseIndex = wasmFileContent.getPosition();
			ValueType[] localTypes = parseValueTypes();
			int instructionLength = size - (wasmFileContent.getPosition() - baseIndex);
			byte[] instructions = wasmFileContent.readBytes(instructionLength);
			Function function = module.getFunction(i);
			function.setBody(localTypes, instructions);
		}
	}

	static FunctionBlock parseFunctionBlock(Module module, Function function, byte[] instructions) {
		FunctionBlock result = new FunctionBlock(function);
		result.setInstructions(parseInstructions(module, result, new ByteBuffer(instructions)));
		return result;
	}

	private static List<Instruction> parseInstructions(Module module, AbstractBlock parent, ByteBuffer byteBuffer) {
		List<Instruction> result = new LinkedList<>();
		while (true) {
			Instruction instruction = parseInstruction(module, parent, byteBuffer);
			result.add(instruction);
			if (instruction instanceof BlockEndMarker) {
				return result;
			}
		}
	}

	private static Instruction parseInstruction(Module module, AbstractBlock parent, ByteBuffer byteBuffer) {
		int instruction = byteBuffer.readUnsignedByte();
		switch (instruction) {
			case 0x02: {
				Block block = new Block(parent, toValueType(byteBuffer.readVarsint7()));
				block.setInstructions(parseInstructions(module, block, byteBuffer));
				return block;
			}
			case 0x03: {
				Loop block = new Loop(parent, toValueType(byteBuffer.readVarsint7()));
				block.setInstructions(parseInstructions(module, block, byteBuffer));
				return block;
			}
			case 0x04: {
				If block = new If(parent, toValueType(byteBuffer.readVarsint7()));
				List<Instruction> thenInstructions = parseInstructions(module, block, byteBuffer);
				List<Instruction> elseInstructions = null;
				if (thenInstructions.get(thenInstructions.size() - 1) instanceof Else) {
					elseInstructions = parseInstructions(module, block, byteBuffer);
				}
				block.setInstructions(thenInstructions, elseInstructions);
				return block;
			}
			case 0x05:
				return new Else();

			case 0x0b:
				return new End();

			case 0x0c:
				return new Br(byteBuffer.readUnsignedLeb128());
			case 0x0d:
				return new BrIf(byteBuffer.readUnsignedLeb128());
			case 0x0e:
				return new BrTable(byteBuffer.readVarUInt32Array(), byteBuffer.readUnsignedLeb128());

			case 0x0f:
				return new Return();

			case 0x10:
				return new Call(module.getFunction(byteBuffer.readUnsignedLeb128()));

			case 0x1a:
				return new Drop();

			case 0x20:
				return new LocalGet(byteBuffer.readUnsignedLeb128());

			case 0x41:
				return new I32Const(byteBuffer.readUnsignedLeb128());

			case 0x68:
				return new I32Ctz();
			case 0x6a:
				return new I32Add();
			case 0x6b:
				return new I32Sub();
			case 0x6c:
				return new I32Mul();
			case 0x6d:
				return new I32DivS();
			case 0x73:
				return new I32Xor();

			case 0xC0:
				return new I32Extend8S();
			case 0xC1:
				return new I32Extend16S();

			default:
				throw new UnsupportedOperationException("not implemented instruction: instruction = 0x" + toHexString(instruction) + ", readIndex = " + byteBuffer.getPosition());
		}
	}

	private void parseGlobalSection() {
		int length = wasmFileContent.readVaruint32();
		for (int i = 0; i < length; i++) {
			ValueType type = toValueType(wasmFileContent.readVarsint7());
			boolean mutability = wasmFileContent.readUint1() != 0;
			Instruction instruction = parseInstruction(null, null, wasmFileContent);
		}
	}

	private void parseExportSection() {
		int length = wasmFileContent.readUnsignedLeb128();
		for (int i = 0; i < length; i++) {
			String name = wasmFileContent.readUtf8();
			int kind = wasmFileContent.readByte();
			switch (kind) {
				case 0x00:
					module.addExportedFunction(name, wasmFileContent.readUnsignedLeb128());
					break;

				default:
					throw new UnsupportedOperationException("not implemented kind (0x" + toHexString(kind) + ")");
			}
		}
	}

	private void parseFunctionSection() {
		int length = wasmFileContent.readUnsignedLeb128();
		for (int i = 0; i < length; i++) {
			module.addFunction(wasmFileContent.readUnsignedLeb128());
		}
	}

	private void parseTypeSection() {
		int length = wasmFileContent.readUnsignedLeb128();
		int index = 0;
		while (index < length) {
			switch (wasmFileContent.readByte()) {
				case 0x60:
					parseFunctionType();
					break;
			}
			index++;
		}
	}

	private void parseFunctionType() {
		module.addFunctionType(parseValueTypes(), parseValueTypes());
	}

	private ValueType[] parseValueTypes() {
		int length = wasmFileContent.readUnsignedLeb128();
		ValueType[] result = new ValueType[length];

		for (int i = 0; i < length; i++) {
			result[i] = toValueType(wasmFileContent.readVarsint7());
		}

		return result;
	}

	private static ValueType toValueType(byte valueOfVarsin7) {
		switch (valueOfVarsin7) {
			case 0x40:
				return ValueType.VOID;
			case 0x7f:
				return ValueType.I32;

			default:
				return null;
		}
	}
}
