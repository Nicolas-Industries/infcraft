package net.infcraft.client.sound;

public class DigSound {

    public final String soundName;
    public final float volume;
    public final float pitch;

    public DigSound(final String soundName, final float volume, final float pitch) {
        this.soundName = soundName;
        this.volume = volume;
        this.pitch = pitch;
    }

    public float soundVolume() {
        return this.volume;
    }

    public float soundPitch() {
        return this.pitch;
    }

    public String digSoundDir() {
        return "dig." + this.soundName;
    }
}

