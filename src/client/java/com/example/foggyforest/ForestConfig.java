package com.example.foggyforest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Настройки мода. Файл: config/foggyforest.json (создаётся при первом запуске). */
public class ForestConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("foggyforest.json");

    // ---------- ТУМАН / ДАЛЬНОСТЬ ПРОРИСОВКИ ----------
    /** На сколько % уменьшается дальность (0 = ничего, 90 = почти вплотную). */
    public double visibilityReductionPercent = 70;
    /** Туман никогда не подойдёт ближе этого расстояния (в блоках). */
    public double minFogEndBlocks = 20;
    /** Где начинается туман: доля от конца тумана (0.0 - 0.9). Меньше = гуще. */
    public double fogStartFraction = 0.1;
    /** За сколько секунд туман плавно появляется/исчезает. */
    public double transitionSeconds = 4;
    /** Считать тайгу лесом тоже. */
    public boolean includeTaiga = false;
    /** Не включать туман, если игрок ниже уровня моря на столько блоков (пещеры). */
    public int belowSeaLevelCutoff = 8;

    // ---------- ЗВУКИ ----------
    public boolean soundsEnabled = true;
    public int soundMinIntervalSeconds = 20;
    public int soundMaxIntervalSeconds = 70;
    public double soundVolume = 0.8;
    public double soundMinDistance = 8;
    public double soundMaxDistance = 20;

    private static ForestConfig instance;

    public static ForestConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                instance = GSON.fromJson(Files.readString(FILE), ForestConfig.class);
            }
        } catch (Exception e) {
            System.err.println("[FoggyForest] Не удалось прочитать конфиг: " + e);
        }
        if (instance == null) instance = new ForestConfig();
        try {
            Files.writeString(FILE, GSON.toJson(instance));
        } catch (Exception ignored) {}
    }
}
