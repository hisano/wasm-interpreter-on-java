package jp.hisano.wasm.interpreter;

public final class Instance {
	private final Module module;

	public Instance(Module module) {
		this.module = module;
	}

	public <T> T invoke(String name, Object... parameters) {
		return module.getExportedFunction(name).invoke(parameters);
	}
}
