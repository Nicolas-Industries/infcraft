package net.opencraft.client;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.io.File;

import net.opencraft.client.resources.DownloadResourcesJob;
import net.opencraft.client.world.ClientWorld;
import net.opencraft.server.Server;
import net.opencraft.core.input.MovingObjectPosition;
import net.opencraft.core.item.ItemStack;
import net.opencraft.server.Server;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.opengl.*;

import net.opencraft.core.blocks.Block;
import net.opencraft.core.blocks.SandBlock;
import net.opencraft.client.config.GameSettings;
import net.opencraft.client.entity.*;
import net.opencraft.client.renderer.entity.models.ModelBiped;
import net.opencraft.client.input.*;
import net.opencraft.client.sound.SoundManager;
import net.opencraft.client.entity.EntityPlayerSP;
import net.opencraft.client.renderer.entity.EntityRenderer;
import net.opencraft.core.physics.AABB;
import net.opencraft.client.renderer.*;
import net.opencraft.client.renderer.entity.RenderGlobal;
import net.opencraft.client.renderer.entity.Renderer;
import net.opencraft.client.renderer.font.FontRenderer;
import net.opencraft.client.renderer.gui.*;
import net.opencraft.client.renderer.texture.*;
import net.opencraft.core.util.*;
import net.opencraft.client.world.WorldRenderer;
import org.lwjgl.system.Platform;

public class OpenCraft implements Runnable {
	public static final String PROJECT_NAME_LOWERCASE = "opencraft";

	public static OpenCraft oc;

	public static long[] tickTimes;
	public static int numRecordedFrameTimes;
	private static File gameDir;
	public long window;
	public PlayerController playerController;
	private boolean fullscreen;
	public int width;
	public int height;
	private OpenGlCapsChecker glCapabilities;
	private Timer timer;
	public ClientWorld clientWorld;
	public RenderGlobal renderGlobal;
	public EntityPlayerSP player;
	public EffectRenderer effectRenderer;
	public Session sessionData;
	public String minecraftUri;
	public boolean hideQuitButton;
	public volatile boolean isGamePaused;
	public Renderer renderer;
	public FontRenderer font;
	public GuiScreen currentScreen;
	public LoadingScreenRenderer loadingScreen;
	public EntityRenderer entityRenderer;
	private int ticksRan;
	private int leftClickCounter;
	private int tempDisplayWidth;
	private int tempDisplayHeight;
	public String objectMouseOverString;
	public int rightClickDelayTimer;
	public GuiIngame ingameGUI;
	public boolean skipRenderWorld;
	public ModelBiped field_9242_w;
	public MovingObjectPosition objectMouseOver;
	public GameSettings options;
	public SoundManager sndManager;
	public File mcDataDir;
	private TextureWaterFX textureWaterFX;
	private TextureLavaFX textureLavaFX;
	public volatile boolean running;
	public String debug;
	long prevFrameTime;
	public boolean inGameHasFocus;
	private int mouseTicksRan;
	public boolean isRaining;
	long systemTime;

	// Track keyboard state to prevent continuous triggering of key-based actions
	private java.util.Set<Integer> prevPressedKeys = new java.util.HashSet<>();

	/**
	 * Holds the function called when the window is resized, otherwise the function
	 * would be garbage collected
	 */
	private GLFWFramebufferSizeCallback frameBufferResizeCallback;
	private GLFWWindowFocusCallback windowFocusCallback;
	public MouseInput mouse;
	public KeyboardInput keyboard;

	static {
		OpenCraft.tickTimes = new long[512];
		OpenCraft.numRecordedFrameTimes = 0;
		OpenCraft.gameDir = new File("opencraft");
		if (!OpenCraft.gameDir.exists()) {
			OpenCraft.gameDir.mkdir();
		}
	}

	public OpenCraft(final int width, final int height, final boolean boolean6) {
		oc = this;
		this.playerController = new PlayerController(oc);
		this.fullscreen = false;
		this.timer = null;
		this.sessionData = new Session("Notch", "1488228");
		this.hideQuitButton = true;
		this.isGamePaused = false;
		this.currentScreen = null;
		this.loadingScreen = new LoadingScreenRenderer(oc);
		this.entityRenderer = new EntityRenderer(oc);
		this.ticksRan = 0;
		this.leftClickCounter = 0;
		this.objectMouseOverString = null;
		this.rightClickDelayTimer = 0;
		this.skipRenderWorld = false;
		this.field_9242_w = new ModelBiped(0.0f);
		this.objectMouseOver = null;
		this.sndManager = new SoundManager();
		this.textureWaterFX = new TextureWaterFX();
		this.textureLavaFX = new TextureLavaFX();
		this.running = true;
		this.debug = "";
		this.prevFrameTime = -1L;
		this.inGameHasFocus = false;
		this.mouseTicksRan = 0;
		this.isRaining = false;
		this.systemTime = System.currentTimeMillis();
		this.tempDisplayWidth = width;
		this.tempDisplayHeight = height;
		this.fullscreen = boolean6;
		(new SleepingForeverThread("Timer hack thread")).start();
		this.width = width;
		this.height = height;
		this.fullscreen = boolean6;
	}

	public static long getSystemTime() {
		return glfwGetTimerValue() * 1000L / glfwGetTimerFrequency();
	}

	public void displayUnexpectedThrowable(final UnexpectedThrowable t) {
		t.exception.printStackTrace();
	}

	public void setServer(final String string, final int integer) {
	}

	public void init() {
		if (Platform.get() == Platform.LINUX)
			glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11);
		glfwInit();
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
		window = glfwCreateWindow(width, height, Main.TITLE, 0, 0);
		if (window == 0) {
			throw new RuntimeException("Failed to create the GLFW window");
		}
		glfwMakeContextCurrent(window);
		if (GL.createCapabilities() == null)
			throw new RuntimeException("Failed to create OpenGL capabilities");
		glfwShowWindow(window);

		glfwSetFramebufferSizeCallback(window, frameBufferResizeCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				width = Math.max(1, width);
				height = Math.max(1, height);
				resize(width, height);
			}
		});
		GLUtil.setupDebugMessageCallback();

		mouse = new MouseInput(window);
		mouse.gameInstance = this; // Set reference for immediate GUI event processing
		keyboard = new KeyboardInput(window);

		timer = new Timer(20.0f);
		resize(width, height);
		mcDataDir = getGameDir();
		options = new GameSettings(oc, mcDataDir);
		renderer = new Renderer(options);
		font = new FontRenderer(options, "/assets/default.png", renderer);
		loadScreen();
		try {
			// Controllers.create();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		}
		checkGLError();
		glEnable(GL_TEXTURE_2D);
		glShadeModel(7425);
		glClearDepth(1.0);
		glEnable(2929);
		glDepthFunc(515);
		glEnable(3008);
		glAlphaFunc(516, 0.1f);
		glCullFace(1029);
		glMatrixMode(5889);
		glLoadIdentity();
		glMatrixMode(5888);
		checkGLError();
		glCapabilities = new OpenGlCapsChecker();
		sndManager.loadSoundSettings(options);
		renderer.registerTextureFX(textureLavaFX);
		renderer.registerTextureFX(textureWaterFX);
		renderer.registerTextureFX(new TextureWaterFlowFX());
		renderer.registerTextureFX(new TextureLavaFlowFX());
		renderer.registerTextureFX(new TextureFlamesFX(0));
		renderer.registerTextureFX(new TextureFlamesFX(1));
		renderer.registerTextureFX(new TextureGearsFX(0));
		renderer.registerTextureFX(new TextureGearsFX(1));
		renderGlobal = new RenderGlobal(oc, renderer);
		glViewport(0, 0, width, height);
		displayGuiScreen(new GuiMainMenu());
		effectRenderer = new EffectRenderer(clientWorld, renderer);
		try {
			new DownloadResourcesJob().run();
		} catch (Exception ex4) {
			ex4.printStackTrace();
		}
		checkGLError();
		ingameGUI = new GuiIngame(oc);
		playerController.a();
	}

	private void loadScreen() {
		final ScaledResolution scaledResolution = new ScaledResolution(width, height, options);
		final int scaledWidth = scaledResolution.getScaledWidth();
		final int scaledHeight = scaledResolution.getScaledHeight();
		glClear(16640);
		glMatrixMode(5889);
		glLoadIdentity();
		glOrtho(0.0, scaledWidth, scaledHeight, 0.0, 1000.0, 3000.0);
		glMatrixMode(5888);
		glLoadIdentity();
		glTranslatef(0.0f, 0.0f, -2000.0f);
		glViewport(0, 0, width, height);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glDisable(2896);
		glDisable(2912);
		glEnable(3553);
		final Tessellator t = Tessellator.instance;
		glBindTexture(3553, renderer.loadTexture("/assets/dirt.png"));
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		final float n = 32.0f;
		t.beginQuads();
		t.setColorOpaque_I(4210752);
		t.vertexUV(0.0, height, 0.0, 0.0, height / n + 0.0f);
		t.vertexUV(width, height, 0.0, width / n, height / n + 0.0f);
		t.vertexUV(width, 0.0, 0.0, width / n, 0.0);
		t.vertexUV(0.0, 0.0, 0.0, 0.0, 0.0);
		t.draw();
		glEnable(3008);
		glAlphaFunc(516, 0.1f);
		font.drawStringWithShadow2("Loading...", (scaledWidth - font.getStringWidth("Loading...")) / 2,
				(scaledHeight - 8) / 2, 0xFFFFFF);
		glfwSwapBuffers(window);
	}

	public void displayGuiScreen(GuiScreen screen) {
		// if (currentScreen instanceof GuiEmptyScreen)
		// return;

		if (currentScreen != null)
			currentScreen.onGuiClosed();

		if (screen == null) {
			if (clientWorld == null)
				screen = new GuiMainMenu();
			else if (player != null && player.health <= 0) {
				screen = new GuiGameOver();
			}
		}

		if ((currentScreen = screen) != null) {
			setIngameNotInFocus();
			final ScaledResolution scaledResolution = new ScaledResolution(width, height, options);
			screen.setWorldAndResolution(oc, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
			skipRenderWorld = false;
		} else {
			setIngameFocus();
		}
	}

	private void checkGLError() {
		final int err = glGetError();
		if (err == 0)
			return;

		throw new IllegalStateException("GL ERROR: " + glGetError());
	}

	public void stop() {
		try {
			System.out.println("Stopping!");
			// Stop integrated server if running
			stopIntegratedServer();
			changeWorld1(null);
			try {
				GLAllocation.deleteTexturesAndDisplayLists();
			} catch (Exception ex2) {
			}
			sndManager.shutdown();
			// Mouse.destroy();
			// Keyboard.destroy();
		} finally {
			glfwDestroyWindow(window);
		}
		Thread.currentThread().interrupt();
		System.gc();
		System.exit(0);
	}

	@Override
	public void run() {
		running = true;
		try {
			init();
		} catch (Exception exception) {
			exception.printStackTrace();
			displayUnexpectedThrowable(new UnexpectedThrowable("Failed to start game", exception));
			return;
		}
		try {
			long currentTimeMillis = System.currentTimeMillis();
			int n = 0;
			while (running) {
				AABB.clearBoundingBoxPool();
				if (glfwWindowShouldClose(window)) {
					shutdown();
				}
				if (isGamePaused) {
					final float renderPartialTicks = timer.renderPartialTicks;
					timer.updateTimer();
					timer.renderPartialTicks = renderPartialTicks;
				} else {
					timer.updateTimer();
				}
				// for (int i = 0; i < minecraft.timer.elapsedTicks; ++i) {
				// ++minecraft.ticksRan;
				// minecraft.runTick();
				// }
				for (int j = 0; j < Math.min(10, this.timer.elapsedTicks); ++j) {
					++ticksRan;
					this.runTick();
				}
				checkGLError();
				if (isGamePaused)
					timer.renderPartialTicks = 1.0f;

				sndManager.setListener(player, timer.renderPartialTicks);
				glEnable(GL_TEXTURE_2D);
				if (clientWorld != null)
					while (clientWorld.updatingLighting())
						;

				if (!skipRenderWorld) {
					playerController.setPartialTime(timer.renderPartialTicks);
					entityRenderer.updateCameraAndRender(timer.renderPartialTicks);
				}

				prevFrameTime = System.nanoTime();

				// Thread.yield();
				mouse.reset();

				glfwSwapBuffers(window);

				glfwPollEvents();

				checkGLError();
				++n;
				isGamePaused = (!isMultiplayerWorld() && currentScreen != null
						&& currentScreen.doesGuiPauseGame());
				while (System.currentTimeMillis() >= currentTimeMillis + 1000L) {
					debug = new StringBuilder().append(n).append(" fps, ").append(WorldRenderer.chunksUpdated)
							.append(" chunk updates").toString();
					WorldRenderer.chunksUpdated = 0;
					currentTimeMillis += 1000L;
					n = 0;
				}
			}
		} catch (OpenCraftError openCraftError) {
		} catch (Exception exception2) {
			exception2.printStackTrace();
			displayUnexpectedThrowable(new UnexpectedThrowable("Unexpected error", exception2));
		} finally {
			stop();
		}
	}

	private void displayDebugInfo() {
		if (prevFrameTime == -1L) {
			prevFrameTime = System.nanoTime();
		}
		final long nanoTime = System.nanoTime();
		OpenCraft.tickTimes[OpenCraft.numRecordedFrameTimes++ & OpenCraft.tickTimes.length - 1] = nanoTime
				- prevFrameTime;
		prevFrameTime = nanoTime;
		glClear(256);
		glMatrixMode(5889);
		glLoadIdentity();
		glOrtho(0.0, width, height, 0.0, 1000.0, 3000.0);
		glMatrixMode(5888);
		glLoadIdentity();
		glTranslatef(0.0f, 0.0f, -2000.0f);
		glLineWidth(1.0f);
		glDisable(3553);
		final Tessellator instance = Tessellator.instance;
		instance.begin(7);
		instance.setColorOpaque_I(538968064);
		instance.vertex(0.0, height - 100, 0.0);
		instance.vertex(0.0, height, 0.0);
		instance.vertex(OpenCraft.tickTimes.length, height, 0.0);
		instance.vertex(OpenCraft.tickTimes.length, height - 100, 0.0);
		instance.draw();
		long n = 0L;
		for (int i = 0; i < OpenCraft.tickTimes.length; ++i) {
			n += OpenCraft.tickTimes[i];
		}
		int i = (int) (n / 200000L / OpenCraft.tickTimes.length);
		instance.begin(7);
		instance.setColorOpaque_I(541065216);
		instance.vertex(0.0, height - i, 0.0);
		instance.vertex(0.0, height, 0.0);
		instance.vertex(OpenCraft.tickTimes.length, height, 0.0);
		instance.vertex(OpenCraft.tickTimes.length, height - i, 0.0);
		instance.draw();
		instance.begin(1);
		for (int j = 0; j < OpenCraft.tickTimes.length; ++j) {
			final int n2 = (j - OpenCraft.numRecordedFrameTimes & OpenCraft.tickTimes.length - 1) * 255
					/ OpenCraft.tickTimes.length;
			int n3 = n2 * n2 / 255;
			n3 = n3 * n3 / 255;
			int n4 = n3 * n3 / 255;
			n4 = n4 * n4 / 255;
			instance.setColorOpaque_I(-16777216 + n4 + n3 * 256 + n2 * 65536);
			instance.vertex(j + 0.5f, height - OpenCraft.tickTimes[j] / 200000L + 0.5f, 0.0);
			instance.vertex(j + 0.5f, height + 0.5f, 0.0);
		}
		instance.draw();
		glEnable(3553);
	}

	public void shutdown() {
		running = false;
	}

	public void setIngameFocus() {
		if (inGameHasFocus) {
			return;
		}
		inGameHasFocus = true;
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		// Center the mouse cursor when focus is regained for camera control
		glfwSetCursorPos(window, width / 2.0, height / 2.0);
		// Reset mouse position and delta tracking to avoid camera movement from cursor
		// movement during GUI interaction
		mouse.position.x = width / 2.0;
		mouse.position.y = height / 2.0;
		mouse.position.prevX = width / 2.0;
		mouse.position.prevY = height / 2.0;
		displayGuiScreen(null);
		mouseTicksRan = ticksRan + 10000;
	}

	public void setIngameNotInFocus() {
		if (!inGameHasFocus) {
			return;
		}
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		inGameHasFocus = false;
	}

	public void displayInGameMenu() {
		if (currentScreen != null) {
			return;
		}
		displayGuiScreen(new GuiIngameMenu());
	}

	private void func_6254_a(final int integer, final boolean boolean2) {
		if (playerController.field_1064_b) {
			return;
		}
		if (integer == 0 && leftClickCounter > 0) {
			return;
		}
		if (boolean2 && objectMouseOver != null && objectMouseOver.typeOfHit == 0 && integer == 0) {
			final int blockX = objectMouseOver.blockX;
			final int blockY = objectMouseOver.blockY;
			final int blockZ = objectMouseOver.blockZ;
			playerController.sendBlockRemoving(blockX, blockY, blockZ, objectMouseOver.sideHit);
			effectRenderer.addBlockHitEffects(blockX, blockY, blockZ, objectMouseOver.sideHit);
		} else {
			playerController.resetBlockRemoving();
		}
	}

	private void clickMouse(final int integer) {
		if (integer == 0 && leftClickCounter > 0) {
			return;
		}
		if (integer == 0) {
			entityRenderer.itemRenderer.resetEquippedProgress();
		}
		if (objectMouseOver == null) {
			if (integer == 0) {
				leftClickCounter = 10;
			}
		} else if (objectMouseOver.typeOfHit == 1) {
			if (integer == 0) {
				player.a(objectMouseOver.entityHit);
			}
			if (integer == 1) {
				player.c(objectMouseOver.entityHit);
			}
		} else if (objectMouseOver.typeOfHit == 0) {
			final int blockX = objectMouseOver.blockX;
			final int n = objectMouseOver.blockY;
			final int blockZ = objectMouseOver.blockZ;
			final int sideHit = objectMouseOver.sideHit;
			final Block block = Block.blocksList[clientWorld.getBlockId(blockX, n, blockZ)];
			if (integer == 0) {
				clientWorld.onBlockHit(blockX, n, blockZ, objectMouseOver.sideHit);
				if (block != Block.bedrock || player.unusedByte >= 100) {
					playerController.clickBlock(blockX, n, blockZ);
				}
			} else {
				final ItemStack currentItem = player.inventory.getCurrentItem();
				final int blockId = clientWorld.getBlockId(blockX, n, blockZ);

				// Send block placement packet
				net.opencraft.shared.network.packets.PacketBlockPlacement packet = new net.opencraft.shared.network.packets.PacketBlockPlacement(
						blockX, n, blockZ, sideHit, 0);
				try {
					this.getClientNetworkManager().sendPacket(packet);
				} catch (java.io.IOException e) {
					e.printStackTrace();
				}

				// if (blockId > 0 && Block.blocksList[blockId].blockActivated(clientWorld,
				// blockX, n, blockZ, player)) {
				// return;
				// }
				if (currentItem == null) {
					return;
				}
				final int stackSize = currentItem.stackSize;
				// Predictively place block client-side for responsiveness
				// FIXME: ItemStack.useItem requires ServerWorld, but we have ClientWorld.
				// Disabling prediction for now to rely on server authority.
				// if (currentItem.useItem(player, clientWorld, blockX, n, blockZ, sideHit)) {
				// entityRenderer.itemRenderer.resetEquippedProgress();
				// }
				if (currentItem.stackSize == 0) {
					player.inventory.mainInventory[player.inventory.currentItem] = null;
				} else if (currentItem.stackSize != stackSize) {
					entityRenderer.itemRenderer.b();
				}
			}
		}
		if (integer == 1) {
			final ItemStack currentItem2 = player.inventory.getCurrentItem();
			if (currentItem2 != null) {
				final int n = currentItem2.stackSize;
				// final ItemStack useItemRightClick =
				// currentItem2.useItemRightClick(clientWorld, player);
				// if (useItemRightClick != currentItem2
				// || (useItemRightClick != null && useItemRightClick.stackSize != n)) {
				// player.inventory.mainInventory[player.inventory.currentItem] =
				// useItemRightClick;
				// entityRenderer.itemRenderer.d();
				// if (useItemRightClick.stackSize == 0) {
				// player.inventory.mainInventory[player.inventory.currentItem] = null;
				// }
				// }
			}
		}
	}

	public void resize(int width, int height) {
		if (width <= 0) {
			width = 1;
		}
		if (height <= 0) {
			height = 1;
		}
		this.width = width;
		this.height = height;
		if (currentScreen != null) {
			final ScaledResolution scaledResolution = new ScaledResolution(width, height, options);
			currentScreen.setWorldAndResolution(oc, scaledResolution.getScaledWidth(),
					scaledResolution.getScaledHeight());
		}
	}

	private void clickMiddleMouseButton() {
		if (objectMouseOver != null) {
			int integer = clientWorld.getBlockId(objectMouseOver.blockX, objectMouseOver.blockY,
					objectMouseOver.blockZ);
			if (integer == Block.grass.blockID) {
				integer = Block.dirt.blockID;
			}
			if (integer == Block.slabDouble.blockID) {
				integer = Block.slabSingle.blockID;
			}
			if (integer == Block.bedrock.blockID) {
				integer = Block.stone.blockID;
			}
			player.inventory.setCurrentItem(integer);
		}
	}

	public void runTick() {
		ingameGUI.updateTick();
		if (!isGamePaused && clientWorld != null) {
			playerController.updateController();
		}
		glBindTexture(3553, renderer.loadTexture("/assets/terrain.png"));
		if (!isGamePaused) {
			renderer.updateDynamicTextures();
		}
		if (currentScreen == null && player != null && player.health <= 0) {
			displayGuiScreen(null);
		}

		if (leftClickCounter > 0) {
			--leftClickCounter;
		}

		if (currentScreen == null || currentScreen.allowUserInput) {
			// Handle scroll wheel separately from mouse buttons
			final int eventDWheel = (int) mouse.scroll.y;
			if (eventDWheel != 0) {
				player.inventory.changeCurrentItem(eventDWheel);
			}
			// Reset scroll values after processing to prevent repetition in the same tick
			mouse.scroll.x = 0;
			mouse.scroll.y = 0;

			// Process mouse events for appropriate context only - once per event
			if (currentScreen == null) {
				// Process mouse events for world context only
				for (MouseInput.ButtonEvent event : mouse.buttons.events) {
					if (System.currentTimeMillis() - systemTime > 200L) {
						continue;
					}
					if (!inGameHasFocus && event.isPress()) {
						setIngameFocus();
					} else {
						if (event.isPress()) {
							clickMouse(event.buttonNumber());
							mouseTicksRan = ticksRan;
						}
					}
				}
			}
			// For GUI screens, mouse events will be processed in handleInputEvents() method
			// later
			// and should not be processed here to prevent double-processing

			// Process keys that were just pressed (not held down from previous tick)
			for (Integer key : keyboard.pressedKeys) {
				// Only process keys that weren't pressed in the previous tick to prevent
				// continuous triggering
				if (!prevPressedKeys.contains(key)) {
					if (key == GLFW_KEY_ESCAPE) {
						displayInGameMenu();
					}

					if (key == GLFW_KEY_F5) {
						options.thirdPersonView = !options.thirdPersonView;
						isRaining = !isRaining;
					}

					if (key == options.keyBindings.get(GameSettings.PlayerInput.INVENTORY)) {
						if (currentScreen instanceof GuiInventory)
							displayGuiScreen(null);
						else if (currentScreen == null)
							displayGuiScreen(new GuiInventory(player.inventory));
					}

					if (key == options.keyBindings.get(GameSettings.PlayerInput.DROP))
						player.dropPlayerItemWithRandomChoice(
								player.inventory.decrStackSize(player.inventory.currentItem, 1), false);

					for (int i = 0; i < 9; ++i) {
						if (key == GLFW_KEY_0 + i) {
							player.inventory.currentItem = i;
						}
					}
				}
			}

			// Update the previous pressed keys set for next tick
			prevPressedKeys.clear();
			prevPressedKeys.addAll(keyboard.pressedKeys);
			if (currentScreen == null) {
				// Only process clicks if not already cooling down from previous click
				// This prevents the issue where startup mouse state or air-clicking causes
				// blocking behavior
				if (leftClickCounter <= 0 && mouse.isButton1Pressed() && ticksRan - mouseTicksRan >= timer.tps / 4.0f
						&& inGameHasFocus) {
					clickMouse(0);
					mouseTicksRan = ticksRan;
				}
				if (leftClickCounter <= 0 && mouse.isButton2Pressed() && ticksRan - mouseTicksRan >= timer.tps / 4.0f
						&& inGameHasFocus) {
					clickMouse(1);
					mouseTicksRan = ticksRan;
				}
			}
			// Only call func_6254_a if not currently cooling down from a previous click
			if (leftClickCounter <= 0) {
				func_6254_a(0, currentScreen == null && mouse.isButton1Pressed() && inGameHasFocus);
			}
		}
		if (currentScreen != null) {
			mouseTicksRan = ticksRan + 10000;
		}
		if (currentScreen != null) {
			currentScreen.handleInputEvents();
			if (currentScreen != null) {
				currentScreen.updateScreen();
			}
		}

		// Process network data
		if (clientNetworkManager != null) {
			clientNetworkManager.processNetworkData();
		}

		// Process integrated server network data if applicable
		if (integratedServer != null && integratedServer.getNetworkSystem() != null) {
			integratedServer.getNetworkSystem().processIntegratedClientPackets();
		}

		// Reset mouse events after all processing
		mouse.reset();
		if (clientWorld != null) {
			clientWorld.difficultySetting = options.difficulty;
			if (!isGamePaused) {
				entityRenderer.updateRenderer();
			}
			if (!isGamePaused) {
				renderGlobal.updateClouds();
			}
			if (!isGamePaused) {
				// Always update entities on the client side for interpolation
				clientWorld.updateEntities();
			}
			if (!isGamePaused) {
				// Always tick the client world to handle animations, entity interpolation, etc.
				clientWorld.tick();
			}

			// Tick the integrated server in singleplayer mode
			if (integratedServer != null && !isGamePaused) {
				integratedServer.tick();
			}
			if (!isGamePaused) {
				clientWorld.randomDisplayUpdates(Mth.floor_double(player.posX), Mth.floor_double(player.posY),
						Mth.floor_double(player.posZ));
			}
			if (!isGamePaused) {
				effectRenderer.updateEffects();
			}
		}
		systemTime = System.currentTimeMillis();
	}

	public Server integratedServer; // For singleplayer integrated server

	public boolean isMultiplayerWorld() {
		return integratedServer != null;
	}

	/**
	 * Start an integrated server for singleplayer mode
	 */
	public void startIntegratedServer(String worldName) {
		File saveDir = getGameDir();
		// Initialize the integrated server
		File serverDir = new File(saveDir, "saves");
		integratedServer = new Server(worldName, serverDir);
		integratedServer.start();
		System.out.println("Integrated server started for singleplayer world: " + worldName);
	}

	/**
	 * Stop the integrated server
	 */
	public void stopIntegratedServer() {
		if (integratedServer != null) {
			integratedServer.stop();
			integratedServer = null;
		}
	}

	/**
	 * Connect to a multiplayer server
	 */
	private net.opencraft.client.network.ClientNetworkManager clientNetworkManager;

	public boolean connectToMultiplayer(String serverAddress, int port) {
		// Initialize client network manager if not already done
		if (clientNetworkManager == null) {
			clientNetworkManager = new net.opencraft.client.network.ClientNetworkManager(this);
		}

		// Connect to the remote server
		boolean connected = clientNetworkManager.connectToRemoteServer(serverAddress, port);
		if (connected) {
			System.out.println("Successfully connected to multiplayer server: " + serverAddress + ":" + port);
		} else {
			System.out.println("Failed to connect to multiplayer server: " + serverAddress + ":" + port);
		}
		return connected;
	}

	public void startWorld(final String string) {
		changeWorld1(null);
		System.gc();

		// Start integrated server for singleplayer
		startIntegratedServer(string);

		// Connect to integrated server
		if (clientNetworkManager == null) {
			clientNetworkManager = new net.opencraft.client.network.ClientNetworkManager(this);
		}
		clientNetworkManager.connectToIntegratedServer();

		// For integrated server mode, we need to wait for the server to provide world
		// data
		// but we can create a client world that will be populated with server data
		ClientWorld world = new ClientWorld(new File(getGameDir(), "saves"), string);

		if (world.isNewWorld) {
			changeWorld2(world, "Generating level");
		} else {
			changeWorld2(world, "Loading level");
		}
	}

	public void changeWorld1(final ClientWorld fe) {
		changeWorld2(fe, "");
	}

	public void changeWorld2(final ClientWorld fe, final String string) {
		if (clientWorld != null) {
			// Client should not save world, server handles it
			// clientWorld.saveWorldIndirectly(loadingScreen);

			// Always stop the integrated server when leaving a world
			// This ensures proper cleanup whether going to title screen or loading another
			// world
			stopIntegratedServer();
		}
		if ((clientWorld = fe) != null) {
			playerController.func_717_a(fe);
			fe.h = font;

			// For integrated server (singleplayer), create the player immediately
			// For multiplayer, player creation is handled by PacketPlayerSpawn
			if (player == null) {
				// Create the player
				(player = new EntityPlayerSP(oc, fe, sessionData)).preparePlayerToSpawn();
				playerController.flipPlayer(player);
				fe.player = player;
			} else {
				// Player already exists, just update world reference
				player.setWorld(fe);
				fe.player = player;
			}

			func_6255_d(string);

			// Ensure player exists and is properly set up
			/*
			 * if (player == null) {
			 * (player = new EntityPlayerSP(oc, fe, sessionData)).preparePlayerToSpawn();
			 * playerController.flipPlayer(player);
			 * }
			 */

			if (player != null) {
				player.movementInput = new net.opencraft.client.input.MovementInput(options, keyboard);
			}

			if (renderGlobal != null) {
				renderGlobal.changeWorld(fe);
			}
			if (effectRenderer != null) {
				effectRenderer.clearEffects(fe);
			}

			if (player != null) {
				playerController.func_6473_b(player);
				fe.player = player;
			}

			// Spawn player immediately for integrated server
			if (player != null && isMultiplayerWorld()) {
				fe.spawnPlayerWithLoadedChunks();
			}

			if (fe.isNewWorld) {
				// Client should not save world
				// fe.saveWorldIndirectly(loadingScreen);
			}
			if (player != null) {
				displayGuiScreen(new GuiInventory(player.inventory));
			}
		}
		System.gc();
		systemTime = 0L;
		this.sndManager.currentMusicTheme = "ingame";
		this.sndManager.ticksBeforeMusic = 0;
		this.sndManager.stopSound("BgMusic");
	}

	private void func_6255_d(final String string) {
		loadingScreen.printText(string);
		loadingScreen.displayLoadingString("Building terrain");
		final int n = 128;
		int n2 = 0;
		int n3 = n * 2 / 16 + 1;
		n3 *= n3;
		for (int i = -n; i <= n; i += 16) {
			int x = clientWorld.x;
			int z = clientWorld.z;
			if (clientWorld.player != null) {
				x = (int) clientWorld.player.posX;
				z = (int) clientWorld.player.posZ;
			}
			for (int j = -n; j <= n; j += 16) {
				loadingScreen.setLoadingProgress(n2++ * 100 / n3);
				clientWorld.getBlockId(x + i, 64, z + j);
				while (clientWorld.updatingLighting()) {
				}
			}
		}
		loadingScreen.displayLoadingString("Simulating world for a bit");
		n3 = 2000;
		SandBlock.fallInstantly = true;
		for (int i = 0; i < n3; ++i) {
			clientWorld.TickUpdates(true);
		}
		clientWorld.func_656_j();
		SandBlock.fallInstantly = false;
	}

	public OpenGlCapsChecker getOpenGlCapsChecker() {
		return glCapabilities;
	}

	public String debugInfoRenders() {
		return renderGlobal.getDebugInfoRenders();
	}

	public String entityRenderingInfo() {
		return renderGlobal.getDebugInfoEntities();
	}

	public String debugInfoEntities() {
		return "P: " + effectRenderer.getStatistics() + ". T: " + clientWorld.func_687_d();
	}

	public void respawn() {
		if (player != null && clientWorld != null) {
			clientWorld.setEntityDead(player);
		}
		clientWorld.a();
		(player = new EntityPlayerSP(oc, clientWorld, sessionData)).preparePlayerToSpawn();
		playerController.flipPlayer(player);
		if (clientWorld != null) {
			clientWorld.player = player;
			clientWorld.spawnPlayerWithLoadedChunks();
		}
		player.movementInput = new MovementInput(options, keyboard);
		playerController.func_6473_b(player);
		func_6255_d("Respawning");
	}

	public static File getGameDir() {
		if (gameDir == null)
			gameDir = new File(PROJECT_NAME_LOWERCASE);
		return gameDir;
	}

	/**
	 * Get the client network manager for sending packets to the server
	 */
	public net.opencraft.client.network.ClientNetworkManager getClientNetworkManager() {
		return clientNetworkManager;
	}

	// Track last position for movement optimization
	private double lastSentX = Double.MAX_VALUE;
	private double lastSentY = Double.MAX_VALUE;
	private double lastSentZ = Double.MAX_VALUE;

	/**
	 * Check if player has moved significantly since last update
	 */
	private boolean hasPlayerMovedSignificantly() {
		if (player == null) {
			return false;
		}

		// Calculate distance moved since last update
		double deltaX = player.posX - lastSentX;
		double deltaY = player.posY - lastSentY;
		double deltaZ = player.posZ - lastSentZ;
		double distanceSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

		// Only send update if player moved more than threshold
		// Using squared distance to avoid sqrt calculation
		boolean moved = distanceSquared > 0.0625; // 0.25^2 (quarter block threshold)

		if (moved) {
			// Update last sent position
			lastSentX = player.posX;
			lastSentY = player.posY;
			lastSentZ = player.posZ;
		}

		return moved;
	}

	public float getTickDelta() {
		return this.timer.tickDelta;
	}
}
