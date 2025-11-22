package net.infcraft.client.renderer.gui;

import org.lwjgl.glfw.GLFW;

import net.infcraft.client.input.MouseInput;
import net.infcraft.client.renderer.font.FontRenderer;

public class GuiTextField extends GuiElement {
    
    private FontRenderer fontRenderer;
    private String text = "";
    private int maxStringLength = 32;
    private int cursorCounter;
    private boolean enableBackgroundDrawing = true;
    private boolean canLoseFocus = true;
    public boolean isFocused = false;
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;
    private int enabledColor = 14737632;
    private int disabledColor = 7368816;
    private boolean visible = true;
    private int x, y, width, height;

    public GuiTextField(FontRenderer fontRenderer, int x, int y, int width, int height) {
        this.fontRenderer = fontRenderer;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void updateCursorCounter() {
        ++this.cursorCounter;
    }

    public void setText(String text) {
        if (text.length() > this.maxStringLength) {
            this.text = text.substring(0, this.maxStringLength);
        } else {
            this.text = text;
        }

        this.setCursorPositionEnd();
    }

    public String getText() {
        return this.text;
    }

    public String getSelectedText() {
        int start = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int end = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.text.substring(start, end);
    }

    public void deleteWords(int num) {
        if (this.text.length() != 0) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
            }
        }
    }

    public void deleteFromCursor(int num) {
        if (this.text.length() != 0) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean isPositive = num > 0;
                int start = isPositive ? this.cursorPosition : this.cursorPosition + num;
                int end = isPositive ? this.cursorPosition + num : this.cursorPosition;

                if (start < 0) {
                    end += -start;
                    start = 0;
                }

                if (end > this.text.length()) {
                    start -= end - this.text.length();
                    end = this.text.length();
                }

                if (start != end) {
                    String before = this.text.substring(0, start);
                    String after = this.text.substring(end);
                    this.text = before + after;
                    this.cursorPosition = isPositive ? start : end;
                }
            }
        }
    }

    public int getNthWordFromCursor(int numWords) {
        return this.getNthWordFromPos(numWords, this.getCursorPosition());
    }

    public int getNthWordFromPos(int n, int pos) {
        int i = pos;
        boolean isForward = n < 0;
        int absN = Math.abs(n);

        for (int j = 0; j < absN; ++j) {
            if (!isForward) {
                int k = this.text.length();
                i = this.text.indexOf(' ', i);

                if (i == -1) {
                    i = k;
                } else {
                    while (i < k && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                i = this.text.lastIndexOf(' ', i - 1);

                if (i < 0) {
                    i = 0;
                }

                while (i < pos && this.text.charAt(i) == ' ') {
                    ++i;
                }
            }
        }

        return i;
    }

    public void moveCursorBy(int num) {
        this.cursorPosition += num;
        int textLength = this.text.length();

        if (this.cursorPosition < 0) {
            this.cursorPosition = 0;
        }

        if (this.cursorPosition > textLength) {
            this.cursorPosition = textLength;
        }

        this.selectionEnd = this.cursorPosition;
    }

    public void setCursorPosition(int pos) {
        this.cursorPosition = pos;
        int textLength = this.text.length();

        if (this.cursorPosition < 0) {
            this.cursorPosition = 0;
        }

        if (this.cursorPosition > textLength) {
            this.cursorPosition = textLength;
        }

        this.selectionEnd = this.cursorPosition;
    }

    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
    }

    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        if (!this.isFocused) {
            return false;
        } else {
            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE:
                    if (this.canWriteText()) {
                        this.deleteFromCursor(-1);
                    }
                    return true;
                case GLFW.GLFW_KEY_DELETE:
                    if (this.canWriteText()) {
                        this.deleteFromCursor(1);
                    }
                    return true;
                case GLFW.GLFW_KEY_LEFT:
                    if (this.isFocused) {
                        if (GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
                            this.setSelectionPos(this.getNthWordFromCursor(-1));
                        } else {
                            this.moveCursorBy(-1);
                        }
                    }
                    return true;
                case GLFW.GLFW_KEY_RIGHT:
                    if (this.isFocused) {
                        if (GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
                            this.setSelectionPos(this.getNthWordFromCursor(1));
                        } else {
                            this.moveCursorBy(1);
                        }
                    }
                    return true;
                case GLFW.GLFW_KEY_HOME:
                    this.setCursorPositionZero();
                    return true;
                case GLFW.GLFW_KEY_END:
                    this.setCursorPositionEnd();
                    return true;
                case GLFW.GLFW_KEY_ENTER:
                case GLFW.GLFW_KEY_KP_ENTER:
                    this.isFocused = false;
                    return true;
                default:
                    if (Character.isISOControl(typedChar)) {
                        return false;
                    } else if (this.writeText(Character.toString(typedChar))) {
                        return true;
                    } else {
                        return false;
                    }
            }
        }
    }

    private boolean writeText(String textToWrite) {
        if (!this.isFocused) {
            return false;
        } else if (this.canWriteText()) {
            int start = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
            int end = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
            String newText = (new StringBuilder()).append(this.text.substring(0, start)).append(textToWrite).append(this.text.substring(end)).toString();

            if (newText.length() <= this.maxStringLength) {
                this.text = newText;
                this.moveCursorBy(start - this.selectionEnd + textToWrite.length());
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean canWriteText() {
        return this.text.length() < this.maxStringLength && this.isFocused;
    }

    public void mouseClicked(int x, int y, int button) {
        boolean isHovering = x >= this.x && x < this.x + this.width && y >= this.y && y < this.y + this.height;

        if (this.canLoseFocus && this.isFocused && !isHovering && button == MouseInput.ButtonEvent.BUTTON_1_PRESS.buttonNumber()) {
            this.isFocused = false;
        }

        if (this.isFocused && isHovering && button == MouseInput.ButtonEvent.BUTTON_1_PRESS.buttonNumber()) {
            int relativeX = x - this.x;
            this.setSelectionPos(this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), relativeX).length() + this.lineScrollOffset);
        }
    }

    private void setSelectionPos(int pos) {
        this.selectionEnd = pos;
        int textLength = this.text.length();

        if (this.selectionEnd < 0) {
            this.selectionEnd = 0;
        }

        if (this.selectionEnd > textLength) {
            this.selectionEnd = textLength;
        }

        if (this.selectionEnd < this.cursorPosition) {
            int temp = this.cursorPosition;
            this.cursorPosition = this.selectionEnd;
            this.selectionEnd = temp;
        }
    }

    public void drawTextBox() {
        if (this.visible) {
            if (this.enableBackgroundDrawing) {
                // Draw background rectangle
                // This would use the rendering methods from the original game
                // For now, just draw a simple rectangle with OpenGL
                org.lwjgl.opengl.GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_BLEND);
                org.lwjgl.opengl.GL11.glBlendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);
                org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
                
                org.lwjgl.opengl.GL11.glLineWidth(1.0F);
                org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_LINES);
                org.lwjgl.opengl.GL11.glVertex2i(this.x, this.y);
                org.lwjgl.opengl.GL11.glVertex2i(this.x + this.width, this.y);
                
                org.lwjgl.opengl.GL11.glVertex2i(this.x + this.width, this.y);
                org.lwjgl.opengl.GL11.glVertex2i(this.x + this.width, this.y + this.height);
                
                org.lwjgl.opengl.GL11.glVertex2i(this.x + this.width, this.y + this.height);
                org.lwjgl.opengl.GL11.glVertex2i(this.x, this.y + this.height);
                
                org.lwjgl.opengl.GL11.glVertex2i(this.x, this.y + this.height);
                org.lwjgl.opengl.GL11.glVertex2i(this.x, this.y);
                org.lwjgl.opengl.GL11.glEnd();
                
                org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
            }
            
            int textColor = this.isFocused ? this.enabledColor : this.disabledColor;
            int cursorOffset = this.fontRenderer.getStringWidth(this.text.substring(0, this.cursorPosition));
            String displayText = this.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.width);
            boolean cursorVisible = this.cursorCounter / 6 % 2 == 0 && this.isFocused;
            
            this.fontRenderer.drawStringWithShadow2(displayText, this.x, this.y + (this.height - 8) / 2, textColor);
            
            if (cursorPosition >= this.text.length() && this.text.length() >= maxStringLength) {
                cursorOffset = this.width;
            }
            
            if (this.cursorPosition < this.text.length()) {
                displayText = this.text.substring(0, this.cursorPosition);
                cursorOffset = this.fontRenderer.getStringWidth(displayText) - this.lineScrollOffset;
            }
            
            if (cursorVisible) {
                if (cursorOffset > this.width) {
                    this.fontRenderer.drawStringWithShadow2("_", this.x + this.width - 8, this.y + (this.height - 8) / 2, textColor);
                } else {
                    this.fontRenderer.drawStringWithShadow2("_", this.x + cursorOffset, this.y + (this.height - 8) / 2, textColor);
                }
            }
            
            if (this.selectionEnd != this.cursorPosition) {
                String selectedText = this.fontRenderer.trimStringToWidth(this.text.substring(this.cursorPosition, this.selectionEnd), this.width);
                int selectionWidth = this.fontRenderer.getStringWidth(selectedText);
                // Draw selection background
            }
        }
    }

    public void setMaxStringLength(int length) {
        this.maxStringLength = length;

        if (this.text.length() > length) {
            this.text = this.text.substring(0, length);
        }
    }

    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    public int getCursorPosition() {
        return this.cursorPosition;
    }

    public boolean getVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setEnableBackgroundDrawing(boolean enableBackgroundDrawing) {
        this.enableBackgroundDrawing = enableBackgroundDrawing;
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public void setFocused(boolean focused) {
        if (this.canLoseFocus) {
            this.isFocused = focused;
        }
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }
}