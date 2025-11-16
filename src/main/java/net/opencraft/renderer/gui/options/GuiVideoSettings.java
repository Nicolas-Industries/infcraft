
package net.opencraft.renderer.gui.options;

import net.opencraft.client.config.GameSettings;
import net.opencraft.renderer.gui.*;

public class GuiVideoSettings extends GuiScreen {

    private GuiScreen parentScreen;
    protected String screenTitle;
    private GameSettings options;
    private int buttonId;

    public GuiVideoSettings(final GuiScreen dc, final GameSettings ja) {
        this.screenTitle = "Video Settings";
        this.buttonId = -1;
        this.parentScreen = dc;
        this.options = ja;
        // Initialize tempGuiScale to current guiScale when opening the video settings
        this.options.tempGuiScale = this.options.guiScale;
    }

    @Override
    public void initGui() {
        this.controlList.clear();
        for (int i = 2; i < 8; ++i) {
            this.controlList.add(new GuiSmallButton(i, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), this.options.getKeyBinding(i)));
        }
        this.controlList.add(new GuiSmallButton(9, this.width / 2 - 155 + 9 % 2 * 160, this.height / 6 + 24 * (9 >> 1), this.options.getKeyBinding(9)));
        // Update the 'Done' button to 'Apply' and add a separate 'Done' button
        this.controlList.add(new GuiButton(201, this.width / 2 - 155, this.height / 6 + 168, "Apply", 150, 20));
        this.controlList.add(new GuiButton(200, this.width / 2 + 5, this.height / 6 + 168, "Done", 150, 20));
        this.controlList.add(new GuiSlider(300, this.width / 2 - 155 + 8 % 2 * 160, this.height / 6 + 24 * (8 >> 1), 11, "Brightness", this.options.minimumBrightness, 100.0F, 0.0F, 0.3F));
        // Add GUI scale slider (button ID 301, option ID 11 for GUI scale)
        // Calculate max scale based on current resolution - at least 1, max 8, or based on resolution
        int maxGuiScale = Math.min(8, Math.max(this.id.width / 320, this.id.height / 240));
        this.controlList.add(new GuiSlider(301, this.width / 2 - 155 + 10 % 2 * 160, this.height / 6 + 24 * (10 >> 1), -11, "GUI Scale", this.options.tempGuiScale, 0.0F, 0.0F, (float)maxGuiScale));

    }

    @Override
    protected void actionPerformed(final GuiButton iq) {
        if (!iq.enabled) {
            return;
        }
        if (iq.buttonId < 100) {
            this.options.setOptionFloatValue(iq.buttonId, 1);
            iq.displayString = this.options.getKeyBinding(iq.buttonId);
        }
        if (iq.buttonId == 200) { // Done button
            this.id.displayGuiScreen(this.parentScreen);
        }
        if (iq.buttonId == 201) { // Apply button
            // Apply GUI scale
            this.options.applyGuiScale();
            // Apply other options if needed (for brightness, etc.)
            // The brightness is applied immediately when changed, so we just need to apply GUI scale
        }
    }

    @Override
    public void drawScreen(final int integer1, final int integer2, final float float3) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 16777215);
        super.drawScreen(integer1, integer2, float3);
    }
}
