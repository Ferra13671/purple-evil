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

    public void playRawSound(SoundBuffer buffer, float volume, float pitch) {
        SoundSource soundSource = Overtone.create2DSource();
        soundSource.setSoundBuffer(buffer);
        soundSource.setVolume(volume);
        soundSource.setPitch(pitch);

        this.rawSounds.add(soundSource);
        soundSource.play();
    }
}
