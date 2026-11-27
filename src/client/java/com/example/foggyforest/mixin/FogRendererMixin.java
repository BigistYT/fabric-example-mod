package com.example.foggyforest.mixin;

import com.example.foggyforest.FoggyForestClient;
import com.example.foggyforest.ForestConfig;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void foggyforest$thickenFog(CallbackInfoReturnable<FogData> cir) {
        float f = FoggyForestClient.fog;
        if (f <= 0.001f) return;

        ForestConfig cfg = ForestConfig.get();
        FogData d = cir.getReturnValue();

        float baseEnd = d.renderDistanceEnd;
        float reduced = (float) Math.max(cfg.minFogEndBlocks, baseEnd * (1.0 - cfg.visibilityReductionPercent / 100.0));
        float targetEnd = Math.min(reduced, baseEnd);
        float targetStart = targetEnd * (float) Math.max(0.0, Math.min(0.9, cfg.fogStartFraction));

        float end = baseEnd + (targetEnd - baseEnd) * f;
        float start = d.renderDistanceStart + (targetStart - d.renderDistanceStart) * f;

        // min() = туман только сгущается; вода/лава/слепота остаются как были
        d.renderDistanceStart = Math.min(d.renderDistanceStart, start);
        d.renderDistanceEnd = Math.min(d.renderDistanceEnd, end);
        d.environmentalStart = Math.min(d.environmentalStart, start);
        d.environmentalEnd = Math.min(d.environmentalEnd, end);
        d.skyEnd = Math.min(d.skyEnd, end);
        d.cloudEnd = Math.min(d.cloudEnd, end);
    }
}
