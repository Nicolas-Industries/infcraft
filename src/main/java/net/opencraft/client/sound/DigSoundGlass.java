// filepath: /run/media/nicolas/98c765ca-052b-46ba-b040-9e26061a1235/projects/OpenCraft/src/main/java/net/opencraft/client/sound/DigSoundGlass.java

package net.opencraft.client.sound;

public class DigSoundGlass extends DigSound {

    public DigSoundGlass(final String soundName, final float volume, final float pitch) {
        super(soundName, volume, pitch);
    }

    @Override
    public String digSoundDir() {
        // Use a special random glass break sound for glass-like dig
        return "random.glass";
    }
}

