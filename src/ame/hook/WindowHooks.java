package ame.hook;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.Win32VK;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;

import ame.callback.KeyboardEventCallBack;
import ame.callback.MouseButtonCallback;
import ame.callback.MouseMoveCallback;
import ame.enums.KeyBoradEvent;
import ame.enums.MouseButton;
import ame.enums.MouseEvent;

public class WindowHooks {

	private static final Executor executor = Executors.newSingleThreadExecutor();

	private static final User32 lib = User32.INSTANCE;

	private static final HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);

	private static AtomicBoolean running = new AtomicBoolean();

	private static WinUser.LowLevelMouseProc mouseProc;
	private static WinUser.HHOOK mouseHook;

	private static WinUser.LowLevelKeyboardProc keyboardProc;
	private static WinUser.HHOOK keyboardHook;

	private static final List<KeyboardEventCallBack> keyboardEventCallBacks = new ArrayList<>();
	private static final List<MouseButtonCallback> mouseButtonCallbacks = new ArrayList<>();
	private static final List<MouseMoveCallback> mouseMoveCallbacks = new ArrayList<>();

	public static void addKeyboardCallback(KeyboardEventCallBack keyboardEventCallBack) {
		keyboardEventCallBacks.add(keyboardEventCallBack);
	}

	public static void addMouseButtonCallback(MouseButtonCallback mouseButtonCallback) {
		mouseButtonCallbacks.add(mouseButtonCallback);
	}

	public static void addMouseMoveCallback(MouseMoveCallback mouseMoveCallback) {
		mouseMoveCallbacks.add(mouseMoveCallback);
	}

	public static void removeKeyboardCallback(KeyboardEventCallBack keyboardEventCallBack) {
		keyboardEventCallBacks.remove(keyboardEventCallBack);
	}

	public static void removeMouseButtonCallback(MouseButtonCallback mouseButtonCallback) {
		mouseButtonCallbacks.remove(mouseButtonCallback);
	}

	public static void removeMouseMoveCallback(MouseMoveCallback mouseMoveCallback) {
		mouseMoveCallbacks.remove(mouseMoveCallback);
	}

	public static void clearCallbacks() {
		keyboardEventCallBacks.clear();
		mouseButtonCallbacks.clear();
		mouseMoveCallbacks.clear();
	}

	public static void start() {
		if (!running.get()) {
			running.set(true);
		} else {
			throw new IllegalAccessError("WindowHooks is already running");
		}

		executor.execute(() -> {

			keyboardProc = new LowLevelKeyboardProc() {
				@Override
				public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
					if (nCode >= 0) {
						switch (wParam.intValue()) {
						case WinUser.WM_KEYUP:
						case WinUser.WM_KEYDOWN:
						case WinUser.WM_SYSKEYUP:
						case WinUser.WM_SYSKEYDOWN:
							Win32VK vk = Win32VK.fromValue(info.vkCode);
							KeyBoradEvent keyBoradEvent = KeyBoradEvent.fromValue(wParam.intValue());
							keyboardEventCallBacks.forEach(callback -> {
								callback.apply(vk, keyBoradEvent);
							});

						}
					}
					Pointer ptr = info.getPointer();
					long peer = Pointer.nativeValue(ptr);
					return lib.CallNextHookEx(keyboardHook, nCode, wParam, new LPARAM(peer));
				}
			};

			keyboardHook = lib.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardProc, hMod, 0);

			mouseProc = new WinUser.LowLevelMouseProc() {
				@Override
				public LRESULT callback(int nCode, WPARAM wParam, WinUser.MSLLHOOKSTRUCT lParam) {
					if (nCode >= 0) {
						POINT point = lParam.pt;
						int x = point.x;
						int y = point.y;
						switch (wParam.intValue()) {
						case 512: {
							mouseMoveCallbacks.forEach(callback -> {
								callback.apply(x, y);
							});
							break;
						}
						case 513: {
							mouseButtonCallbacks.forEach(callback -> {
								callback.apply(MouseButton.LEFT, MouseEvent.BUTTON_DOWN, x, y);
							});
							break;
						}
						case 514: {
							mouseButtonCallbacks.forEach(callback -> {
								callback.apply(MouseButton.LEFT, MouseEvent.BUTTON_UP, x, y);
							});
							break;
						}
						case 516: {
							mouseButtonCallbacks.forEach(callback -> {
								callback.apply(MouseButton.RIGHT, MouseEvent.BUTTON_DOWN, x, y);
							});
							break;
						}
						case 517: {
							mouseButtonCallbacks.forEach(callback -> {
								callback.apply(MouseButton.RIGHT, MouseEvent.BUTTON_UP, x, y);
							});
							break;
						}
						case 519: {
							mouseButtonCallbacks.forEach(callback -> {
								callback.apply(MouseButton.MIDDLE, MouseEvent.BUTTON_DOWN, x, y);
							});
							break;
						}
						case 520: {
							mouseButtonCallbacks.forEach(callback -> {
								callback.apply(MouseButton.MIDDLE, MouseEvent.BUTTON_UP, x, y);
							});
							break;
						}
						}
					}
					return lib.CallNextHookEx(mouseHook, nCode, wParam, new WinDef.LPARAM(Pointer.nativeValue(lParam.getPointer())));
				}
			};

			mouseHook = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_MOUSE_LL, mouseProc, hMod, 0);

			int result;
			MSG msg = new MSG();
			while ((result = lib.GetMessage(msg, null, 0, 0)) != 0 && running.get()) {
				if (result == -1) {
					System.err.println("error in get message");
					break;
				} else {
					System.err.println("got message");
					lib.TranslateMessage(msg);
					lib.DispatchMessage(msg);
				}
			}
			lib.UnhookWindowsHookEx(keyboardHook);
			lib.UnhookWindowsHookEx(mouseHook);
		});

	}

	public static void stop() {
		if (running.get()) {
			running.set(false);
			lib.UnhookWindowsHookEx(keyboardHook);
			lib.UnhookWindowsHookEx(mouseHook);
		} else {
			throw new IllegalAccessError("WindowHooks is not running");
		}
	}

}
