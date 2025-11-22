package net.infcraft.client.input;

import net.infcraft.client.InfCraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.util.HashSet;
import java.util.Set;

public class MouseInput {

	private MouseButtonCallback callbackInstance;
	public MouseButtonCallback buttons; // Keep public for backward compatibility
	public CursorPosCallback position = new CursorPosCallback();
	public ScrollCallback scroll = new ScrollCallback();

	public MouseInput(long window) {
		callbackInstance = new MouseButtonCallback(this);
		buttons = callbackInstance; // Maintain the field for backward compatibility
		GLFW.glfwSetMouseButtonCallback(window, callbackInstance);
		GLFW.glfwSetCursorPosCallback(window, position);
		GLFW.glfwSetScrollCallback(window, scroll);
	}

	public void reset() {
		buttons.events.clear();
	}

	public boolean isButton1Pressed() {
		return buttons.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_1);
	}

	public boolean isButton2Pressed() {
		return buttons.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_2);
	}

	public enum ButtonEvent {
		BUTTON_1_PRESS,
		BUTTON_1_RELEASE,
		BUTTON_2_PRESS,
		BUTTON_2_RELEASE;

		public boolean isPress() {
			return this == BUTTON_1_PRESS || this == BUTTON_2_PRESS;
		}

		public boolean isRelease() {
			return this == BUTTON_1_RELEASE || this == BUTTON_2_RELEASE;
		}

		public int buttonNumber() {
			return switch(this) {
				case BUTTON_1_PRESS, BUTTON_1_RELEASE -> GLFW.GLFW_MOUSE_BUTTON_1;
				case BUTTON_2_PRESS, BUTTON_2_RELEASE -> GLFW.GLFW_MOUSE_BUTTON_2;
			};
		}
	}

	public InfCraft gameInstance; // Reference to the game instance for immediate GUI processing

	public static final class MouseButtonCallback extends GLFWMouseButtonCallback {
		public Set<Integer> pressedButtons = new HashSet<>();
		public Set<ButtonEvent> events = new HashSet<>();
		public int mods;


		@Override
		public void invoke(long window, int button, int action, int mods) {
			this.mods = mods;
			ButtonEvent event = null;

			if (action == GLFW.GLFW_PRESS) {
				pressedButtons.add(button);
				if(button == GLFW.GLFW_MOUSE_BUTTON_1) {
					event = ButtonEvent.BUTTON_1_PRESS;
				} else if(button == GLFW.GLFW_MOUSE_BUTTON_2) {
					event = ButtonEvent.BUTTON_2_PRESS;
				}
			} else if (action == GLFW.GLFW_RELEASE) {
				pressedButtons.remove(button);
				if(button == GLFW.GLFW_MOUSE_BUTTON_1) {
					event = ButtonEvent.BUTTON_1_RELEASE;
				} else if(button == GLFW.GLFW_MOUSE_BUTTON_2) {
					event = ButtonEvent.BUTTON_2_RELEASE;
				}
			}

			if (event != null) {
				if (this_.gameInstance != null && this_.gameInstance.currentScreen != null) {
					// Process GUI event immediately instead of batching
					this_.gameInstance.currentScreen.handleMouseEvent(event);
				} else {
					// Add to batch for world context processing
					events.add(event);
				}
			}
		}

		private MouseInput this_;
		public MouseButtonCallback(MouseInput mouseInput) {
			this_ = mouseInput;
		}
	}

	public static final class CursorPosCallback extends GLFWCursorPosCallback {
		public double x;
		public double y;

		public double prevX;
		public double prevY;

		@Override
		public void invoke(long window, double xpos, double ypos) {
			x = xpos;
			int[] heightBuffer = new int[1];
			GLFW.glfwGetWindowSize(window, null, heightBuffer);
			y = heightBuffer[0] - ypos;
		}

		public double deltaX() {
			final double out = x - prevX;
			prevX = x;
			return out;
		}

		public double deltaY() {
			final double out = y - prevY;
			prevY = y;
			return out;
		}

	}

	public static final class ScrollCallback extends GLFWScrollCallback {
		public double x;
		public double y;

		@Override
		public void invoke(long window, double xoffset, double yoffset) {
			x = xoffset;
			y = yoffset;
		}
	}

}
