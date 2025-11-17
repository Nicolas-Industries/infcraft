package net.opencraft.client.renderer.gui;

public class GuiMultiplayer extends GuiScreen {

    protected GuiScreen parentGuiScreen;
    protected String screenHeader;
    private GuiTextField serverAddressField;
    private GuiTextField serverPortField;
    private GuiButton connectButton;

    public GuiMultiplayer(final GuiScreen dc) {
        this.screenHeader = "Multiplayer";
        this.parentGuiScreen = dc;
    }

    @Override
    public void initGui() {
        this.controlList.clear();

        // Create text fields for server address and port
        this.serverAddressField = new GuiTextField(this.fontRenderer, (this.width / 2) - 100, this.height / 2 - 20, 200, 20);
        this.serverAddressField.setText("localhost");

        this.serverPortField = new GuiTextField(this.fontRenderer, (this.width / 2) - 100, this.height / 2 + 10, 200, 20);
        this.serverPortField.setText("25565");

        this.initButtons();
    }

    public void initButtons() {
        // Connect button
        this.connectButton = new GuiButton(0, (this.width / 2) - 100, this.height / 2 + 40, "Connect", 200, 20);
        this.controlList.add(this.connectButton);

        // Cancel button
        this.controlList.add(new GuiButton(-6, (this.width / 2) - 100, this.height / 2 + 70, "Cancel", 200, 20));
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (!button.enabled) {
            return;
        }

        if (button.buttonId == 0) { // Connect button
            String address = serverAddressField.getText();
            String portText = serverPortField.getText();

            try {
                int port = Integer.parseInt(portText);
                // Attempt to connect to the server
                if (id.connectToMultiplayer(address, port)) {
                    // Connection successful - in a real implementation, we'd start connecting
                    // For now, just return to main menu since full connection logic isn't implemented
                    this.id.displayGuiScreen(this.parentGuiScreen);
                } else {
                    // Handle connection failure
                    System.out.println("Connection failed to " + address + ":" + port);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number: " + portText);
            }
        } else if (button.buttonId == -6) { // Cancel button
            this.id.displayGuiScreen(this.parentGuiScreen);
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenHeader, this.width / 2, 20, 16777215);
        this.drawString(this.fontRenderer, "Server Address:", (this.width / 2) - 100, this.height / 2 - 40, 10526880);
        this.drawString(this.fontRenderer, "Server Port:", (this.width / 2) - 100, this.height / 2 - 5, 10526880);

        this.serverAddressField.drawTextBox();
        this.serverPortField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.serverAddressField.isFocused()) {
            this.serverAddressField.textboxKeyTyped(typedChar, keyCode);
        } else if (this.serverPortField.isFocused()) {
            this.serverPortField.textboxKeyTyped(typedChar, keyCode);
        } else if (keyCode == 256) { // GLFW_KEY_ESCAPE
            this.id.displayGuiScreen(this.parentGuiScreen);
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onMouseButtonPressed(int mouseX, int mouseY, int mouseButton) {
        super.onMouseButtonPressed(mouseX, mouseY, mouseButton);
        this.serverAddressField.mouseClicked(mouseX, mouseY, mouseButton);
        this.serverPortField.mouseClicked(mouseX, mouseY, mouseButton);
    }
}