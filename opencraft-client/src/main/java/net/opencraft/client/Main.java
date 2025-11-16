package net.opencraft.client;

import net.opencraft.OpenCraft;

/**
 * Main client class that starts the OpenCraft client
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting OpenCraft Client...");
        
        OpenCraft game = new OpenCraft();
        game.startGame();
    }
}