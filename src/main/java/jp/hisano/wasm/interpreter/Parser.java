package jp.hisano.wasm.interpreter;

import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.*;
import static jp.hisano.wasm.interpreter.InterpreterException.Type.*;
import jp.hisano.wasm.interpreter.Module.AbstractBlock;
import jp.hisano.wasm.interpreter.Module.Br;
import jp.hisano.wasm.interpreter.Module.BrIf;
import jp.hisano.wasm.interpreter.Module.BrTable;
import jp.hisano.wasm.interpreter.Module.Call;
import jp.hisano.wasm.interpreter.Module.Drop;
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
import jp.hisano.wasm.interpreter.Module.Instruction;
import jp.hisano.wasm.interpreter.Module.LocalGet;
import jp.hisano.wasm.interpreter.Module.LoopBlock;
import jp.hisano.wasm.interpreter.Module.Return;
import jp.hisano.wasm.interpreter.Module.Block;
import jp.hisano.wasm.interpreter.Module.ValueType;

final class Parser {
	private static final int MAGIC = 0x6d736100; // = "\0asm"
	private static final int VERSION = 1;

	private final ByteBuffer wasmFileContent;

	private final Module module = new Module();

	Parser(byte[] wasmFileContent) {
		this.wasmFileContent = new ByteBuffer(wasmFileContent);
	}

	Module parse() {
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
			int instruction = byteBuffer.readUnsignedByte();
			switch (instruction) {
				case 0x02: {
					Block block = new Block(parent, parseValueType(byteBuffer));
					block.setInstructions(parseInstructions(module, block, byteBuffer));
					result.add(block);
					break;
				}
				case 0x03: {
					LoopBlock block = new LoopBlock(parent, parseValueType(byteBuffer));
					block.setInstructions(parseInstructions(module, block, byteBuffer));
					result.add(block);
					break;
				}

				case 0x0b:
					result.add(new End());
					return result;

				case 0x0c:
					result.add(new Br(byteBuffer.readUnsignedLeb128()));
					break;
				case 0x0d:
					result.add(new BrIf(byteBuffer.readUnsignedLeb128()));
					break;
				case 0x0e:
					result.add(new BrTable(byteBuffer.readVarUInt32Array(), byteBuffer.readUnsignedLeb128()));
					break;

				case 0x0f:
					result.add(new Return());
					break;

				case 0x10:
					result.add(new Call(module.getFunction(byteBuffer.readUnsignedLeb128())));
					break;

				case 0x1a:
					result.add(new Drop());
					break;

				case 0x20:
					result.add(new LocalGet(byteBuffer.readUnsignedLeb128()));
					break;

				case 0x41:
					result.add(new I32Const(byteBuffer.readUnsignedLeb128()));
					break;

				case 0x68:
					result.add(new I32Ctz());
					break;
				case 0x6a:
					result.add(new I32Add());
					break;
				case 0x6b:
					result.add(new I32Sub());
					break;
				case 0x6c:
					result.add(new I32Mul());
					break;
				case 0x6d:
					result.add(new I32DivS());
					break;
				case 0x73:
					result.add(new I32Xor());
					break;

				case 0xC0:
					result.add(new I32Extend8S());
					break;
				case 0xC1:
					result.add(new I32Extend16S());
					break;

				default:
					throw new UnsupportedOperationException("not implemented instruction: instruction = 0x" + toHexString(instruction) + ", readIndex = " + byteBuffer.getPosition());
			}
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
			result[i] = parseValueType(wasmFileContent);
		}

		return result;
	}

	private static ValueType parseValueType(ByteBuffer byteBuffer) {
		switch (byteBuffer.readByte()) {
			case 0x40:
				return ValueType.VOID;
			case 0x7f:
				return ValueType.I32;

			default:
				return null;
		}
	}
}
