package jp.hisano.wasm.interpreter;

import java.util.List;
import java.util.stream.Collectors;

import jp.hisano.wasm.interpreter.Module.GlobalVariableType;
import jp.hisano.wasm.interpreter.Module.Instruction;

public final class Instance {
	private final Module module;
	private final List<GlobalVariable> globalVariables;

	public Instance(Module module) {
		this.module = module;
		globalVariables = module.getGlobalVariableTypes().stream().map(GlobalVariable::new).collect(Collectors.toList());

		prepareGlobalVariables();
	}

	private void prepareGlobalVariables() {
		globalVariables.stream().forEach(globalVariable -> {
			GlobalVariableType type = globalVariable.getType();
			if (type.getType() == Module.ValueType.I32) {
				List<Instruction> instructions = type.getInstructions();
				// モジュールでインポートされたGlobalはスキップ
				if (!instructions.isEmpty()) {
					Frame frame = new Frame(Instance.this, null);
					instructions.forEach(instruction -> {
						instruction.execute(frame);
					});
					globalVariable.getValue().setI32(frame.pop().getI32());
				}
			}
		});
	}

	public <T> T invoke(String name, Object... parameters) {
		return module.getExportedFunction(name).invoke(this, parameters);
	}

	Module getModule() {
		return module;
	}

	GlobalVariable getGlobalVariable(int index) {
		return globalVariables.get(index);
	}

	static class GlobalVariable {
		private final GlobalVariableType type;
		private final Value value;

		GlobalVariable(GlobalVariableType type) {
			this.type = type;
			value = new Value(type.getType());
		}

		GlobalVariableType getType() {
			return type;
		}

		Value getValue() {
			return value;
		}
	}
}
