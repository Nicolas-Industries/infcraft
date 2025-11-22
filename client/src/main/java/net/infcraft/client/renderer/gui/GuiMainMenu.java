
package net.infcraft.client.renderer.gui;

import net.infcraft.client.Main;
import net.infcraft.client.renderer.RenderSkybox;
import net.infcraft.client.renderer.RenderSkyboxCube;
import net.infcraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

public class GuiMainMenu extends GuiScreen {

    private float updateCounter;
    private String[] splashes;
    private String currentSplash;
    private final RenderSkybox panorama = new RenderSkybox(new RenderSkyboxCube("textures/gui/title/background/panorama"));

    public GuiMainMenu() {
        this.updateCounter = 0.0f;
        // TODO: Arrays are not the best always...
        this.splashes = new String[]{"Pre-beta!", "As seen on TV!", "Awesome!", "100% pure!", "May contain nuts!", "Better than Prey!", "More polygons!", "Sexy!", "Limited edition!", "Flashing letters!", "Made by Notch!", "Coming soon!", "Best in class!", "When it's finished!", "Absolutely dragon free!", "Excitement!", "More than 5000 sold!", "One of a kind!", "700+ hits on YouTube!", "Indev!", "Spiders everywhere!", "Check it out!", "Holy cow, man!", "It's a game!", "Made in Sweden!", "Uses LWJGL!", "Reticulating splines!", "InfCraft!", "Yaaay!", "Alpha version!", "Singleplayer!", "Keyboard compatible!", "Undocumented!", "Ingots!", "Exploding creepers!", "That's not a moon!", "l33t!", "Create!", "Survive!", "Dungeon!", "Exclusive!", "The bee's knees!", "Down with O.P.P.!", "Closed source!", "Classy!", "Wow!", "Not on steam!", "9.95 euro!", "Half price!", "Oh man!", "Check it out!", "Awesome community!", "Pixels!", "Teetsuuuuoooo!", "Kaaneeeedaaaa!", "Now with difficulty!", "Enhanced!", "90% bug free!", "Pretty!", "12 herbs and spices!", "Fat free!", "Absolutely no memes!", "Free dental!", "Ask your doctor!", "Minors welcome!", "Cloud computing!", "Legal in Finland!", "Hard to label!", "Technically good!", "Bringing home the bacon!", "Indie!", "GOTY!", "Ceci n'est pas une title screen!", "Euclidian!", "Now in 3D!", "Inspirational!", "Herregud!", "Complex cellular automata!", "Yes, sir!", "Played by cowboys!", "OpenGL 1.1!", "Thousands of colors!", "Try it!", "Age of Wonders is better!", "Try the mushroom stew!", "Sensational!", "Hot tamale, hot hot tamale!", "Play him off, keyboard cat!", "Guaranteed!", "Macroscopic!", "Bring it on!", "Random splash!", "Call your mother!", "Monster infighting!", "Loved by millions!", "Ultimate edition!", "Freaky!", "You've got a brand new key!", "Water proof!", "Uninflammable!", "Whoa, dude!", "All inclusive!", "Tell your friends!", "NP is not in P!", "Notch <3 Ez!", "Music by C418!"};
        this.currentSplash = this.splashes[(int) (Math.random() * this.splashes.length)];
    }

    @Override
    public void updateScreen() {
        this.updateCounter += 0.01f;
    }

    @Override
    protected void keyTyped(final char character, final int integer) {
    }

    @Override
    public void initGui() {
        this.controlList.clear();
        this.controlList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 48, "Singleplayer", 200, 20));
        this.controlList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 72, "Multiplayer", 200, 20));
        this.controlList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 114, "Options...", 95, 20));
        this.controlList.add(new GuiButton(3, this.width / 2 + 5, this.height / 4 + 114, "Quit Game", 95, 20));
    }

    @Override
    protected void actionPerformed(final GuiButton iq) {
        if (iq.buttonId == 0) {
            this.id.displayGuiScreen(new GuiOptions(this, this.id.options));
        }
        if (iq.buttonId == 1) {
            this.id.displayGuiScreen(new GuiCreateWorld(this));
        }
        if (iq.buttonId == 2) {
            this.id.displayGuiScreen(new GuiMultiplayer(this));
        }
        if (iq.buttonId == 3) {
            this.id.shutdown();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // —— PANORAMA —— //
        this.panorama.render(partialTicks);

        // —— SCALING: Match original Minecraft logo size (256x49) —— //
        final int ORIGINAL_LOGO_WIDTH = 256;
        final int ORIGINAL_LOGO_HEIGHT = 49; // Actual original height

        // Calculate proportional size for 401x78 logo
        final float ASPECT_RATIO = 401.0f / 78.0f;
        final int LOGO_DRAW_WIDTH = ORIGINAL_LOGO_WIDTH;
        final int LOGO_DRAW_HEIGHT = Math.round(LOGO_DRAW_WIDTH / ASPECT_RATIO); // ≈50

        // Center logo (same as original)
        int logoX = (this.width - LOGO_DRAW_WIDTH) / 2;
        int logoY = 30; // Same Y position as original
        logoX = Math.round(logoX);
        logoY = Math.round(logoY);

        // —— DRAW SCALED LOGO (256x50) —— //
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id.renderer.loadTexture("/assets/gui/logo.png"));
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        Tessellator t = Tessellator.instance;
        t.beginQuads();

        // Normalized UVs (0.0-1.0) for entire texture
        t.uv(0.0f, 1.0f); t.vertex(logoX,               logoY + LOGO_DRAW_HEIGHT, 0.0);
        t.uv(1.0f, 1.0f); t.vertex(logoX + LOGO_DRAW_WIDTH, logoY + LOGO_DRAW_HEIGHT, 0.0);
        t.uv(1.0f, 0.0f); t.vertex(logoX + LOGO_DRAW_WIDTH, logoY,               0.0);
        t.uv(0.0f, 0.0f); t.vertex(logoX,               logoY,               0.0);

        t.draw();

        // —— REST OF MENU (UNCHANGED) —— //
        GL11.glPushMatrix();
        GL11.glTranslatef(this.width / 2.0f + 90.0f, 70.0f, 0.0f);
        GL11.glRotatef(-20.0f, 0.0f, 0.0f, 1.0f);
        double cycle = (System.currentTimeMillis() % 1000L) / 1000.0;
        float pulse = (float) Math.abs(Math.sin(cycle * 2 * Math.PI)) * 0.1f;
        float baseScale = 1.8f - pulse;
        int textWidth = this.fontRenderer.getStringWidth(this.currentSplash) + 32;
        float scale = (baseScale * 100.0f) / textWidth;
        GL11.glScalef(scale, scale, 1.0f);
        this.drawCenteredString(this.fontRenderer, this.currentSplash, 0, -8, 0xFFCC00);
        GL11.glPopMatrix();

        this.drawString(this.fontRenderer, Main.TITLE, 4, this.height - 12, 0xFFFFFF);

        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();

        String freeStr = String.format("Free memory: %d%% of %dMB",
                (maxMem - freeMem) * 100L / maxMem,
                maxMem / (1024L * 1024L));
        String allocStr = String.format("Allocated memory: %d%% (%dMB)",
                totalMem * 100L / maxMem,
                totalMem / (1024L * 1024L));

        int rightX = this.width - this.fontRenderer.getStringWidth(freeStr) - 2;
        this.drawString(this.fontRenderer, freeStr,  rightX, 2,  0xFFFFFF);
        this.drawString(this.fontRenderer, allocStr, rightX, 12, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
        id.sndManager.currentMusicTheme = "menu";
        id.sndManager.playRandomMusicIfReady();
    }
}
