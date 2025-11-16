// filepath: /run/media/nicolas/98c765ca-052b-46ba-b040-9e26061a1235/projects/OpenCraft/src/main/java/net/opencraft/client/sound/DigSound.java

package net.opencraft.client.sound;

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

