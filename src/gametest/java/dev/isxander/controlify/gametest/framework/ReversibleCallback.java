package dev.isxander.controlify.gametest.framework;

public interface ReversibleCallback extends AutoCloseable {
	@Override
	void close();
}
