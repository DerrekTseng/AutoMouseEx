package main.enums;

import com.sun.jna.platform.win32.WinUser;

public enum KeyBoradEvent {

	KEYUP(WinUser.WM_KEYUP), KEYDOWN(WinUser.WM_KEYDOWN), SYSKEYUP(WinUser.WM_SYSKEYUP), SYSKEYDOWN(WinUser.WM_SYSKEYDOWN);

	final int code;

	KeyBoradEvent(int code) {
		this.code = code;
	}

	public static KeyBoradEvent fromValue(final int code) {
		for (KeyBoradEvent ke : KeyBoradEvent.values()) {
			if (ke.code == code) {
				return ke;
			}
		}
		throw new IllegalArgumentException(String.format("No mapping for %02x", code));
	}

}
