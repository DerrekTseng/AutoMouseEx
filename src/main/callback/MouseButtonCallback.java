package main.callback;

import main.enums.MouseButton;
import main.enums.MouseEvent;

@FunctionalInterface
public interface MouseButtonCallback {
	void apply(MouseButton button, MouseEvent event, int x, int y);
}
