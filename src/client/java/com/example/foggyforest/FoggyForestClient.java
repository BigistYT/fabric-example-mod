package com.example.foggyforest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;

public class FoggyForestClient implements ClientModInitializer {
    /** Текущая сила тумана 0..1 (плавно меняется). Читается миксином. */
    public static volatile float fog = 0f;

    private static int soundCooldown = 20 * 20;

    @Override
    public void onInitializeClient() {
        ForestConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(FoggyForestClient::tick);
    }

    private static void tick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            fog = 0f;
            return;
        }
        if (mc.isPaused()) return;

        ForestConfig cfg = ForestConfig.get();
        BlockPos pos = mc.player.blockPosition();

        boolean inForest = level.getBiome(pos).is(BiomeTags.IS_FOREST)
                || (cfg.includeTaiga && level.getBiome(pos).is(BiomeTags.IS_TAIGA));
        if (pos.getY() < level.getSeaLevel() - cfg.belowSeaLevelCutoff) inForest = false;

        // плавный вход/выход
        float step = 1f / (float) Math.max(1.0, cfg.transitionSeconds * 20.0);
        fog = Mth.clamp(fog + (inForest ? step : -step), 0f, 1f);

        // жуткие звуки
        if (cfg.soundsEnabled && inForest && fog > 0.5f) {
            if (--soundCooldown <= 0) {
                playCreepySound(level, mc, cfg);
                int min = Math.max(1, cfg.soundMinIntervalSeconds) * 20;
                int max = Math.max(min, cfg.soundMaxIntervalSeconds * 20);
                soundCooldown = min + level.getRandom().nextInt(max - min + 1);
            }
        }
    }

    private static void playCreepySound(ClientLevel level, Minecraft mc, ForestConfig cfg) {
        var rnd = level.getRandom();
        double angle = rnd.nextDouble() * Math.PI * 2;
        double dist = cfg.soundMinDistance + rnd.nextDouble() * Math.max(0, cfg.soundMaxDistance - cfg.soundMinDistance);
        double x = mc.player.getX() + Math.cos(angle) * dist;
        double z = mc.player.getZ() + Math.sin(angle) * dist;
        float pitch = 0.6f + rnd.nextFloat() * 0.4f;
        // Звуки пещер, как в оригинале. Идут "из тумана" со случайной стороны.
        level.playLocalSound(x, mc.player.getY(), z,
                SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT,
                (float) cfg.soundVolume, pitch, false);
    }
}
