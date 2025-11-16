package net.opencraft.core;

import net.opencraft.core.blocks.Block;
import net.opencraft.core.blocks.SandBlock;
import net.opencraft.core.sound.SoundManager;
import net.opencraft.core.input.MovingObjectPosition;
import net.opencraft.core.entity.EntityPlayer;
import net.opencraft.core.item.ItemStack;
import net.opencraft.core.physics.AABB;
import net.opencraft.core.util.Mth;
import net.opencraft.core.util.UnexpectedThrowable;
import net.opencraft.core.world.World;

import java.io.File;

public class OpenCraft implements Runnable {
	public static final String PROJECT_NAME_LOWERCASE = "opencraft";

	public static OpenCraft oc;

	public static long[] tickTimes;
	private static File gameDir;

	// Server integration
	private boolean isSingleplayer = false; // if we are internal TODO: properly implement
	private Timer timer;
	public World world;
	//public EntityPlayer player; TODO: handle multiple players
	public String minecraftUri;
	public volatile boolean isGamePaused;
	private int ticksRan;
	public MovingObjectPosition objectMouseOver;
	//public GameSettings options; // TODO: initialize server properties
	public SoundManager sndManager;
	public File mcDataDir;
	public volatile boolean running;
	public String debug;
	long prevFrameTime;
	public boolean inGameHasFocus;
	private int mouseTicksRan;
	public boolean isRaining;
	long systemTime;

	static {
		OpenCraft.tickTimes = new long[512];
		OpenCraft.gameDir = new File("opencraft");
		if (!OpenCraft.gameDir.exists()) {
			OpenCraft.gameDir.mkdir();
		}
	}

	public OpenCraft() {
		oc = this;
		this.timer = null;
		this.isGamePaused = false;
		this.ticksRan = 0;
		this.objectMouseOver = null;
		this.sndManager = new SoundManager();
		this.running = true;
		this.debug = "";
		this.prevFrameTime = -1L;
		this.inGameHasFocus = false;
		this.isRaining = false;
		this.systemTime = System.currentTimeMillis();
		(new SleepingForeverThread("Timer hack thread")).start();
	}

	public void displayUnexpectedThrowable(final UnexpectedThrowable t) {
		t.exception.printStackTrace();
	}

	public void init() {
		timer = new Timer(20.0f);
		mcDataDir = getGameDir();
		//options = new GameSettings(oc, mcDataDir); TODO: initialize server properties
	}

	public void stop() {
		try {
			System.out.println("Stopping!");
			changeWorld1(null);
		} catch (Exception ex) {
            ex.printStackTrace();
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
				if (isGamePaused) {
					final float renderPartialTicks = timer.renderPartialTicks;
					timer.updateTimer();
					timer.renderPartialTicks = renderPartialTicks;
				} else {
					timer.updateTimer();
				}
				for (int j = 0; j < Math.min(10, this.timer.elapsedTicks); ++j) {
					++ticksRan;
					this.runTick();
				}
				if (isGamePaused)
					timer.renderPartialTicks = 1.0f;

				if (world != null)
					while (world.updatingLighting());


				prevFrameTime = System.nanoTime();

				++n;
                // control game paused here, possibly optimize when everyone leaves in multiplayer. TODO: implement

				while (System.currentTimeMillis() >= currentTimeMillis + 1000L) {
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

	public void shutdown() {
		running = false;
	}

	public void runTick() {
		// TODO: Process network data


		if (world != null) {
			//world.difficultySetting = options.difficulty; TODO: adjust for server properties
			if (!isGamePaused) {
				world.updateEntities();
			}
		}
		systemTime = System.currentTimeMillis();
	}

	public boolean isMultiplayerWorld() {
		return !isSingleplayer;
	}

	/**
	 * Start an integrated server for singleplayer mode
	 */
	public void startIntegratedServer(String worldName) {
		File saveDir = getGameDir();

		// Create and start the integrated server TODO: implement
//		integratedServer = new Server();
//		integratedServer.start();

		// Mark as singleplayer so we know to connect to our integrated server
		isSingleplayer = true;

		System.out.println("Integrated server started for singleplayer world: " + worldName);
	}

    /**
     * Start up a new server world
     * @param worldName The name of the world to start
     */
	public void startWorld(final String worldName) {
		changeWorld1(null);
		System.gc();

		// Start integrated server for singleplayer
		startIntegratedServer(worldName);

        // Create world instance, root of the server game dir
		final World world = new World(getSavesDir(), worldName);

		// The world initialization will come from the server via PacketWorldInit
		if (world.isNewWorld) {
			changeWorld2(world, "Generating level");
		} else {
			changeWorld2(world, "Loading level");
		}
	}

	public void changeWorld1(final World fe) {
		changeWorld2(fe, "");
	}

	public void changeWorld2(final World fe, final String string) {
		if ((world = fe) != null) {
			func_6255_d(string);
            //TODO: no clue, but this needs to be changed for server worlds
		}
		System.gc();
		systemTime = 0L;
	}

	private void func_6255_d(final String string) { // TODO: also likely needs modification for server worlds
		final int n = 128;
		int n2 = 0;
		int n3 = n * 2 / 16 + 1;
		n3 *= n3;
		for (int i = -n; i <= n; i += 16) {
			int x = world.x;
			int z = world.z;
			if (world.player != null) {
				x = (int) world.player.posX;
				z = (int) world.player.posZ;
			}
			for (int j = -n; j <= n; j += 16) {
				world.getBlockId(x + i, 64, z + j);
				while (world.updatingLighting()) {
				}
			}
		}
		n3 = 2000;
		SandBlock.fallInstantly = true;
		for (int i = 0; i < n3; ++i) {
			world.TickUpdates(true);
		}
		world.func_656_j();
		SandBlock.fallInstantly = false;
	}

    public File getSavesDir() {
        // Now this is variable, because for server worlds, we store the world in the game dir directly
        if (isSingleplayer) {
            return new File(getGameDir(), "saves");
        } else {
            return getGameDir();
        }
    }

	public static File getGameDir() {
		if(gameDir == null)
			gameDir = new File(PROJECT_NAME_LOWERCASE);
		return gameDir;
	}

	public float getTickDelta() {
		return this.timer.tickDelta;
	}

    public static long getSystemTime() {
        return System.currentTimeMillis();
    }
}
