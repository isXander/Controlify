package dev.isxander.splitscreen.client.host.features.music;

import dev.isxander.splitscreen.client.SplitscreenPawn;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

// TODO: currently we have no music at all.
public class PawnMusicManager {
    // pawn index to music
    private final Map<Integer, Music> requestedMusics = new Int2ObjectArrayMap<>();

    public void onRequest(@Nullable Music music, SplitscreenPawn pawn) {
        this.requestedMusics.put(pawn.pawnIndex(), music);
    }
}
