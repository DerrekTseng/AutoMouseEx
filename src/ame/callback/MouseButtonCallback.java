package ame.callback;

import ame.enums.MouseButton;
import ame.enums.MouseEvent;

@FunctionalInterface
public interface MouseButtonCallback {
	void apply(MouseButton button, MouseEvent event, int x, int y);
}
