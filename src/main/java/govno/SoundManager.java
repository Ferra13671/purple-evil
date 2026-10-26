package govno;

import com.ferra13671.overtone.Overtone;
import com.ferra13671.overtone.SoundBuffer;
import com.ferra13671.overtone.SoundSource;
import com.ferra13671.overtone.SoundState;

import java.util.ArrayList;
import java.util.List;

public class SoundManager {
    private final List<SoundSource> rawSounds = new ArrayList<>();

    public void tick() {
        for (SoundSource soundSource : this.rawSounds)
            if (soundSource.getState() == SoundState.Stopped)
                soundSource.close();

        this.rawSounds.removeIf(soundSource -> soundSource.getState() == SoundState.Stopped);
    }

    public SoundSource playSound(SoundBuffer buffer, float volume, float pitch) {
        return playSound(buffer, volume, pitch, false);
    }

    public SoundSource playSound(SoundBuffer buffer, float volume, float pitch, boolean loop) {
        SoundSource soundSource = Overtone.create2DSource();
        soundSource.setSoundBuffer(buffer);
        soundSource.setVolume(volume * Settings.SOUNDS_VOLUME);
        soundSource.setPitch(pitch);
        soundSource.setLooping(loop);

        this.rawSounds.add(soundSource);
        soundSource.play();

        return soundSource;
    }
}
