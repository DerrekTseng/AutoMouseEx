package main.callback;

@FunctionalInterface
public interface MouseMoveCallback {
	void apply(int x, int y);
}
