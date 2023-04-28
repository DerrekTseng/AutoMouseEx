package ame.callback;

import com.sun.jna.platform.win32.Win32VK;

import ame.enums.KeyBoradEvent;

@FunctionalInterface
public interface KeyboardEventCallBack {
	void apply(Win32VK vk, KeyBoradEvent event);
}
