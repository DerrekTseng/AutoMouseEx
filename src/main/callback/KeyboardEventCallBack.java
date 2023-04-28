package main.callback;

import com.sun.jna.platform.win32.Win32VK;

import main.enums.KeyBoradEvent;

@FunctionalInterface
public interface KeyboardEventCallBack {
	void apply(Win32VK vk, KeyBoradEvent event);
}
