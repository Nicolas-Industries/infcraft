package net.infcraft.client.sound;

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

