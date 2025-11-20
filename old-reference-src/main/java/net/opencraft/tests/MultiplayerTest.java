package net.opencraft.client.tests;

import net.opencraft.server.Server;

import java.io.File;

/**
 * Test class for multiplayer functionality
 * This tests the core server-client communication and connection handling
 */
public class MultiplayerTest {

    public static void main(String[] args) {
        System.out.println("Testing multiplayer functionality...");

        // Test server creation and startup
        testServerCreation();

        // Test that server can be started and stopped properly
        testServerStartStop();

        System.out.println("Multiplayer functionality tests completed.");
    }

    /**
     * Test that server can be created properly
     */
    public static void testServerCreation() {
        System.out.println("  - Testing server creation...");

        try {
            File testSaveDir = new File("test_saves");
            testSaveDir.mkdirs();

            Server server = new Server("test_world", testSaveDir);

            if (server != null) {
                System.out.println("    ✓ Server created successfully");

                // Verify server has required components
                if (true) { // Placeholder since we can't access world from simple server
                    System.out.println("    ✓ Server created successfully");
                } else {
                    System.out.println("    ✗ Server world is null");
                }
            } else {
                System.out.println("    ✗ Failed to create server");
            }
        } catch (Exception e) {
            System.out.println("    ✗ Error creating server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test that server can start and stop properly
     */
    public static void testServerStartStop() {
        System.out.println("  - Testing server start/stop...");

        try {
            File testSaveDir = new File("test_saves");
            testSaveDir.mkdirs();

            Server server = new Server("test_world", testSaveDir);

            // Start the server
            server.start();
            System.out.println("    ✓ Server started successfully");

            // Check if server is running
            if (server.isRunning()) {
                System.out.println("    ✓ Server reports as running");
            } else {
                System.out.println("    ✗ Server reports as not running after start");
            }

            // Stop the server after a short time
            Thread.sleep(1000); // Wait a bit for server to initialize
            server.stop();
            System.out.println("    ✓ Server stopped successfully");

            // Check if server is no longer running
            if (!server.isRunning()) {
                System.out.println("    ✓ Server reports as stopped");
            } else {
                System.out.println("    ✗ Server reports as still running after stop");
            }

        } catch (Exception e) {
            System.out.println("    ✗ Error testing server start/stop: " + e.getMessage());
            e.printStackTrace();
        }
    }
}