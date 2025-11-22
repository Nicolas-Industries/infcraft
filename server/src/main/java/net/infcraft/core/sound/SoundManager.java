package net.infcraft.core.sound;

import java.net.URL;

public class SoundManager {
    private SoundPool soundPoolSounds = new SoundPool();
    private SoundPool soundPoolStreaming = new SoundPool();
    private int playedSoundsCount = 0;

    public enum SoundType {
        SOUND("sound"),
        MUSIC("music"),
        STREAMING;

        public final String[] alternateNames;

        SoundType(String...alternateNames) {
            if(alternateNames == null) {
                this.alternateNames = new String[0];
            } else {
                this.alternateNames = alternateNames;
            }
        }

        public static SoundType fromString(String name) {
            for(SoundType type : values()) {
                if(name.contains(type.name().toLowerCase())) {
                    return type;
                }
                for(String alternateName : type.alternateNames) {
                    if(name.contains(alternateName)) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public boolean containsSound(String soundName) {
        return soundPoolSounds.contains(soundName) || soundPoolStreaming.contains(soundName);
    }

    public boolean addSound(String name, URL resourceURL) {
        return soundPoolSounds.addSound(name, resourceURL) != null;
    }

    public void stopSound(String var1) {
        //TODO: fire event
    }

    public void playStreaming(String var1, float var2, float var3, float var4, float var5, float var6) {
                    String var7 = "streaming";
//        // fire event
            if(var1 != null) {
                SoundPoolEntry var8 = this.soundPoolStreaming.getRandomSoundFromSoundPool(var1);
                if(var8 != null && var5 > 0.0F) {
//TODO: fire event
                }

            }
    }

    public void playSound(String var1, float var2, float var3, float var4, float var5, float var6) {

            if(!soundPoolSounds.contains(var1)) {
                System.err.println("Sound not found: " + var1);
            }
            SoundPoolEntry var7 = this.soundPoolSounds.getRandomSoundFromSoundPool(var1);
            if(var7 != null && var5 > 0.0F) {
                this.playedSoundsCount = (this.playedSoundsCount + 1) % 256;
                String var8 = "sound_" + this.playedSoundsCount;
                float var9 = 16.0F;
                if(var5 > 1.0F) {
                    var9 *= var5;
                }
// handle pitch
                if(var5 > 1.0F) {
                    var5 = 1.0F;
                }
                 // handle volume
                // TODO: send event into an event system
            }

    }

    public void playSoundFX(String var1, float var2, float var3) {
            SoundPoolEntry var4 = this.soundPoolSounds.getRandomSoundFromSoundPool(var1);
            if(var4 != null) {
                this.playedSoundsCount = (this.playedSoundsCount + 1) % 256;
                String var5 = "sound_" + this.playedSoundsCount;
//                this.sndSystem.newSource(false, var5, var4.soundUrl, var4.soundName, false, 0.0F, 0.0F, 0.0F, 0, 0.0F);
                if(var2 > 1.0F) {
                    var2 = 1.0F;
                }

//                var2 *= 0.25F;
//                this.sndSystem.setPitch(var5, var3);
//                this.sndSystem.setVolume(var5, var2);
//                this.sndSystem.play(var5);
                // TODO: send audio fx event into an event system with volume and pitch

        }
    }
}
