
package net.infcraft.client.renderer.entity;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import javax.imageio.ImageIO;

import net.infcraft.client.SkinHolder;
import net.infcraft.client.renderer.texture.ImageProvider;
import org.lwjgl.BufferUtils;

import net.infcraft.client.config.GameSettings;
import net.infcraft.client.renderer.GLAllocation;
import net.infcraft.client.renderer.texture.TextureFX;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class Renderer {

	private HashMap<String, Integer> textureFileNameToID;
	private HashMap<Integer, BufferedImage> textureIDToImage;
	private IntBuffer intBuffer;
	private ByteBuffer byteBuffer;
	private List<TextureFX> effects;
	private Map<String, SkinHolder> skinURLToTextureHolder;
	private GameSettings settings;
	private boolean h;
	private Set<String> missingTextures = new HashSet<>();

	public Renderer(final GameSettings settings) {
		this.textureFileNameToID = new HashMap<>();
		this.textureIDToImage = new HashMap<>();
		this.intBuffer = BufferUtils.createIntBuffer(1);
		this.byteBuffer = BufferUtils.createByteBuffer(99999999);
		this.effects = new ArrayList<>();
		this.skinURLToTextureHolder = new HashMap<>();
		this.h = false;
		this.settings = settings;
	}

	/**
	 * Loads a texture from the specified file name. If the texture is not found,
	 * the "missing texture" texture is loaded instead.
	 * @return textureID
	 */
	public int loadTexture(final String textureFileName) {
		final Integer textureID = this.textureFileNameToID.get(textureFileName);
		if (textureID != null) {
			return textureID;
		}
		try {
			this.intBuffer.clear();
			GLAllocation.generateDisplayLists(this.intBuffer);
			final int firstUnusedTextureID = this.intBuffer.get(0);

			// Try to load the texture based on different prefixes
			InputStream textureStream = null;
			if (textureFileName.startsWith("##")) {
				textureStream = Renderer.class.getResourceAsStream(textureFileName.substring(2));
			} else if (textureFileName.startsWith("%%")) {
				textureStream = Renderer.class.getResourceAsStream(textureFileName.substring(2));
			} else {
				textureStream = Renderer.class.getResourceAsStream(textureFileName);
			}

			// If the texture was found, load it
			if (textureStream != null) {
				if (textureFileName.startsWith("##")) {
					this.bindTexture(this.scaledImage(ImageIO.read(textureStream)), firstUnusedTextureID);
				} else {
					if (textureFileName.startsWith("%%")) {
						this.h = true;
					}
					this.bindTexture(ImageIO.read(textureStream), firstUnusedTextureID);
					if (textureFileName.startsWith("%%")) {
						this.h = false;
					}
				}
				this.textureFileNameToID.put(textureFileName, firstUnusedTextureID);
				return firstUnusedTextureID;
			} else {
				// If the texture is not found, load the missing texture
				logMissingTexture(textureFileName);
				return loadMissingTexture();
			}

		} catch (IOException ex) {
			// Fallback to missing texture in case of any I/O errors
			return loadMissingTexture();
		}
	}

    /**
     * Loads a texture from the specified file name with custom filtering.
     * @param textureFileName Path to the texture
     * @param useLinearFilter true for GL_LINEAR, false for GL_NEAREST
     * @return textureID
     */
    public int loadTextureWithFilter(final String textureFileName, final boolean useLinearFilter) {
        final Integer textureID = this.textureFileNameToID.get(textureFileName);
        if (textureID != null) {
            return textureID;
        }
        try {
            this.intBuffer.clear();
            GLAllocation.generateDisplayLists(this.intBuffer);
            final int firstUnusedTextureID = this.intBuffer.get(0);

            InputStream textureStream = null;
            if (textureFileName.startsWith("##")) {
                textureStream = Renderer.class.getResourceAsStream(textureFileName.substring(2));
            } else if (textureFileName.startsWith("%%")) {
                textureStream = Renderer.class.getResourceAsStream(textureFileName.substring(2));
            } else {
                textureStream = Renderer.class.getResourceAsStream(textureFileName);
            }

            if (textureStream != null) {
                if (textureFileName.startsWith("##")) {
                    this.bindTexture(this.scaledImage(ImageIO.read(textureStream)), firstUnusedTextureID, useLinearFilter);
                } else {
                    if (textureFileName.startsWith("%%")) {
                        this.h = true;
                    }
                    this.bindTexture(ImageIO.read(textureStream), firstUnusedTextureID, useLinearFilter);
                    if (textureFileName.startsWith("%%")) {
                        this.h = false;
                    }
                }
                this.textureFileNameToID.put(textureFileName, firstUnusedTextureID);
                return firstUnusedTextureID;
            } else {
                logMissingTexture(textureFileName);
                return loadMissingTexture();
            }

        } catch (IOException ex) {
            return loadMissingTexture();
        }
    }

	private void logMissingTexture(String textureName) {
		// Log the missing texture only if it hasn't been logged before
		if (missingTextures.add(textureName)) {
			System.err.println("Texture not found: " + textureName + ", using missing texture instead.");
		}
	}

	private int loadMissingTexture() {
		// Path to missing texture or generate it programmatically
		final String missingTexturePath = "/assets/missing.png";

		try {
			InputStream missingTextureStream = Renderer.class.getResourceAsStream(missingTexturePath);

			if (missingTextureStream != null) {
				this.intBuffer.clear();
				GLAllocation.generateDisplayLists(this.intBuffer);
				final int missingTextureValue = this.intBuffer.get(0);
				this.bindTexture(ImageIO.read(missingTextureStream), missingTextureValue);
				this.textureFileNameToID.put(missingTexturePath, missingTextureValue);
				return missingTextureValue;
			} else {
				// Handle the case where the missing texture itself is missing
				throw new RuntimeException("Missing texture could not be loaded.");
			}

		} catch (IOException e) {
			throw new RuntimeException("Missing texture could not be loaded.", e);
		}
	}

	/**
	 * This provides a blank image at 1/16 scale of the input image.
	 */
	private BufferedImage scaledImage(final BufferedImage input) {
		final int widthDiv16 = input.getWidth() / 16;
		final BufferedImage bufferedImage2 = new BufferedImage(16, input.getHeight() * widthDiv16, BufferedImage.TYPE_INT_ARGB);
		final Graphics graphics = bufferedImage2.getGraphics();
		for (int i = 0; i < widthDiv16; ++i) {
			graphics.drawImage(input, -i * 16, i * input.getHeight(), null);
		}
		graphics.dispose();
		return bufferedImage2;
	}

	/**
	 * Registers a texture and returns the texture ID.
	 * @return textureID
	 */
	public int registerTexture(final BufferedImage bufferedImage) {
		this.intBuffer.clear();
		GLAllocation.generateDisplayLists(this.intBuffer);
		final int textureID = this.intBuffer.get(0);
		this.bindTexture(bufferedImage, textureID);
		this.textureIDToImage.put(textureID, bufferedImage);
		return textureID;
	}

	/**
	 * Wrapper for bindTexture, with GL_NEAREST
	 */
    public void bindTexture(final BufferedImage bufferedImage, final int textureID) {
        bindTexture(bufferedImage, textureID, false);
    }

    /**
     * Binds the specified image to the specified texture ID.
     */
    public void bindTexture(final BufferedImage bufferedImage, final int textureID, boolean useLinearFilter) {
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Set filter based on usage
        if (useLinearFilter) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            // ✅ FORCE CLAMP_TO_EDGE for smooth textures
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        }

        // Wrapping: keep as-is (CLAMP_TO_EDGE is ideal for logos)
        if (this.h) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }

        final int width = bufferedImage.getWidth();
        final int height = bufferedImage.getHeight();

        // Extract ARGB → RGBA (byte-swapped)
        final int[] pixels = new int[width * height];
        final byte[] rgba = new byte[width * height * 4];
        bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);

        for (int i = 0; i < pixels.length; ++i) {
            int argb = pixels[i];
            int a = (argb >> 24) & 0xFF;
            int r = (argb >> 16) & 0xFF;
            int g = (argb >>  8) & 0xFF;
            int b = (argb      ) & 0xFF;

            // Anaglyph mode (keep your cool effect ✅)
            if (this.settings != null && this.settings.anaglyph) {
                int gray = (r * 30 + g * 59 + b * 11) / 100;
                int redOnly = (r * 30 + g * 70) / 100;
                int blueOnly = (r * 30 + b * 70) / 100;
                r = gray;
                g = redOnly;
                b = blueOnly;
            }

            rgba[i * 4 + 0] = (byte) r;
            rgba[i * 4 + 1] = (byte) g;
            rgba[i * 4 + 2] = (byte) b;
            rgba[i * 4 + 3] = (byte) a;
        }

        // Upload with proper enums (no magic numbers!)
        this.byteBuffer.clear();
        this.byteBuffer.put(rgba);
        this.byteBuffer.flip(); // ← more idiomatic than .position(0).limit(...)

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA,            // internal format
                width, height,
                0,
                GL11.GL_RGBA,            // format
                GL11.GL_UNSIGNED_BYTE,   // type
                this.byteBuffer
        );
    }

	/**
	 * Deletes the texture with the specified ID.
	 */
	public void deleteTexture(final int textureID) {
		this.textureIDToImage.remove(textureID);
		this.intBuffer.clear();
		this.intBuffer.put(textureID);
		this.intBuffer.flip();
		glDeleteTextures(this.intBuffer);
	}

	/**
	 * Loads and binds a texture from the specified URL or file name.
	 * @return textureID
	 */
	public int loadAndBindTexture(final String textureURL, final String textureFileName) {
		final SkinHolder holder = this.skinURLToTextureHolder.get(textureURL);
		if (holder != null && holder.image != null && !holder.isBound) {
			if (holder.textureID < 0) {
				holder.textureID = this.registerTexture(holder.image);
			} else {
				this.bindTexture(holder.image, holder.textureID);
			}
			holder.isBound = true;
		}
		if (holder == null || holder.textureID < 0) {
			return this.loadTexture(textureFileName);
		}
		return holder.textureID;
	}

	public SkinHolder registerNewTextureHolder(final String skinURL, final ImageProvider p) {
		final SkinHolder holder = this.skinURLToTextureHolder.get(skinURL);
		if (holder == null) {
			this.skinURLToTextureHolder.put(skinURL, new SkinHolder(skinURL, p));
		} else {
			++holder.useCount;
		}
		return holder;
	}

	public void deleteTextureIfUnused(final String string) {
		final SkinHolder skinHolder = this.skinURLToTextureHolder.get(string);
		if (skinHolder != null) {
			--skinHolder.useCount;
			if (skinHolder.useCount == 0) {
				if (skinHolder.textureID >= 0) {
					this.deleteTexture(skinHolder.textureID);
				}
				this.skinURLToTextureHolder.remove(string);
			} else {
				System.out.println("SkinHolder not removed, use count: " + skinHolder.useCount);
			}
		}
	}

	public void registerTextureFX(final TextureFX effect) {
		this.effects.add(effect);
		effect.onTick();
	}

	public void updateDynamicTextures() {
		for(final TextureFX textureFX : this.effects) {
			textureFX.anaglyphEnabled = this.settings.anaglyph;
			textureFX.onTick();
			this.byteBuffer.clear();
			this.byteBuffer.put(textureFX.imageData);
			this.byteBuffer.position(0).limit(textureFX.imageData.length);
			for(int j = 0; j < textureFX.tileSize; ++j) {
				for(int k = 0; k < textureFX.tileSize; ++k) {
					glTexSubImage2D(GL_TEXTURE_2D, 0, textureFX.iconIndex % 16 * 16 + j * 16, textureFX.iconIndex / 16 * 16 + k * 16, 16, 16, 6408, 5121, this.byteBuffer);
				}
			}
		}
		for(final TextureFX textureFX2 : this.effects) {
			if(textureFX2.textureId > 0) {
				this.byteBuffer.clear();
				this.byteBuffer.put(textureFX2.imageData);
				this.byteBuffer.position(0).limit(textureFX2.imageData.length);
				glBindTexture(GL_TEXTURE_2D, textureFX2.textureId);
				glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 16, 16, 6408, 5121, this.byteBuffer);
			}
		}
	}

	public void refreshTextures() {
		for (final int intValue : this.textureIDToImage.keySet()) {
			this.bindTexture(this.textureIDToImage.get(intValue), intValue);
		}
		for(SkinHolder holder : this.skinURLToTextureHolder.values()) {
			holder.isBound = false;
		}
		for (final String s : this.textureFileNameToID.keySet()) {
			try {
				BufferedImage bufferedImage;
				if (s.startsWith("##")) {
					bufferedImage = this.scaledImage(ImageIO.read(Renderer.class.getResourceAsStream(s.substring(2))));
				} else if (s.startsWith("%%")) {
					this.h = true;
					bufferedImage = ImageIO.read(Renderer.class.getResourceAsStream(s.substring(2)));
					this.h = false;
				} else {
					bufferedImage = ImageIO.read(Renderer.class.getResourceAsStream(s));
				}
				this.bindTexture(bufferedImage, this.textureFileNameToID.get(s));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Binds texture to the specified ID. ID must be > 0
	 */
	public void bindTexture(final int id) {
		if (id < 0) {
			System.err.println("Refusing to bind texture. Invalid texture ID: " + id);
			return;
		}
		
		glBindTexture(GL_TEXTURE_2D, id);
	}
}
