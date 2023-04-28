package ame;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import ame.hook.WindowHooks;

public class Main {

	static final long AFKTime = (long) (1000 * 60 * 2);

	static final long checkAFK = 5000L;

	static Map<Robot, Rectangle> RobotRectangle = new HashMap<>();
	static Random random = new Random();

	static AtomicLong lastEventTime = new AtomicLong(System.currentTimeMillis());

	public static void main(String[] args) throws Exception {

		WindowHooks.addKeyboardCallback((vk, event) -> {
			lastEventTime.set(System.currentTimeMillis());
		});

		WindowHooks.addMouseButtonCallback((button, event, x, y) -> {
			lastEventTime.set(System.currentTimeMillis());
		});

		WindowHooks.addMouseMoveCallback((x, y) -> {
			lastEventTime.set(System.currentTimeMillis());
		});

		WindowHooks.start();

		start();
	}

	static void start() throws AWTException {
		initRobot();
		Executors.newSingleThreadExecutor().execute(() -> {
			while (true) {
				if (shoudMove()) {
					moveMouse();
					System.out.println("moved");
					sleep(checkAFK);
				}
			}
		});
	}

	static void initRobot() throws AWTException {
		for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			for (GraphicsConfiguration config : device.getConfigurations()) {
				Rectangle bounds = config.getBounds();
				Robot robot = new Robot(device);
				RobotRectangle.put(robot, bounds);
			}
		}
	}

	static boolean shoudMove() {
		return (System.currentTimeMillis() - lastEventTime.get()) > AFKTime;
	}

	static void moveMouse() {
		RobotRectangle.forEach((key, value) -> {
			Point point = MouseInfo.getPointerInfo().getLocation();
			key.mouseMove(random.nextInt(value.width), random.nextInt(value.height));
			sleep(10);
			key.mouseMove(point.x, point.y);
			return;
		});
	}

	static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
