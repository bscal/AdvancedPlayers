package me.bscal.advancedplayer.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public abstract class LightMapTextureManagerMixin
{

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private boolean dirty;

    @Shadow
    private float flickerIntensity;

    @Shadow
    @Final
    private GameRenderer renderer;

    @Shadow
    protected abstract float easeOutQuart(float x);

    @Shadow
    @Final
    private NativeImage image;

    @Shadow
    @Final
    private NativeImageBackedTexture texture;

    /**
     * Mixin to make minecrafts lightmap dark darker. It was easier to Inject and cancel, maybe update into smaller mixins
     */
    @Inject(method = "update", at = @At(value = "HEAD"), cancellable = true)
    public void update(float delta, CallbackInfo ci)
    {
        ci.cancel();
        if (!this.dirty)
        {
            return;
        }
        this.dirty = false;
        this.client.getProfiler().push("lightTex");
        ClientWorld clientWorld = this.client.world;
        if (clientWorld == null) return;
        var dimensionType = clientWorld.getDimension();

        long time = clientWorld.getTimeOfDay() % 24000L;
        boolean isDay = IsDayClient(time);
        float nightMidpoint = GetNightMidpoint(time);
        float scaleModifier = MathHelper.lerp(nightMidpoint, 0f, MoonPhaseModifier(isDay, clientWorld));

        if (!clientWorld.getDimension().hasSkyLight())
            scaleModifier = 0f;

        float f = clientWorld.getStarBrightness(1.0f);
        float g = clientWorld.getLightningTicksLeft() > 0 ? 1.0f : f * 0.95f + 0.05f;
        float h = this.client.player.getUnderwaterVisibility();
        float i = this.client.player.hasStatusEffect(StatusEffects.NIGHT_VISION) ?
                GameRenderer.getNightVisionStrength(this.client.player, delta) :
                (h > 0.0f && this.client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER) ? h : 0.0f);
        Vec3f vec3f = new Vec3f(f, f, 1.0f);
        vec3f.lerp(new Vec3f(1.0f, 1.0f, 1.0f), 0.35f);
        float j = this.flickerIntensity + 1.5f;
        Vec3f vec3f2 = new Vec3f();
        for (int k = 0; k < 16; ++k)
        {
            for (int l = 0; l < 16; ++l)
            {
                float s;
                Vec3f vec3f4;
                float r;
                float n;
                float m = LightmapTextureManager.getBrightness(dimensionType, k) * g;
                float o = n = LightmapTextureManager.getBrightness(dimensionType, l) * j;
                float p = n * ((n * 0.6f + 0.4f) * 0.6f + 0.4f);
                float q = n * (n * n * 0.6f + 0.4f);
                vec3f2.set(o, p, q);
                if (clientWorld.getDimensionEffects().shouldBrightenLighting())
                {
                    vec3f2.lerp(new Vec3f(0.99f, 1.12f, 1.0f), 0.25f);
                } else
                {
                    // TODO sunset/sunset
                    // TODO moon stages lights
                    // TODO maybe noon increase light
                    // TODO flash of green, bloodmoons
                    // TODO adjust blue at nighttime

                    // These seem to be decent values:
                    // Caves are pitch black but midnight is dark but enough to see outlines
                    Vec3f vec3f3 = vec3f.copy();
                    vec3f3.scale(m * 2f);
                    vec3f3.add(new Vec3f(scaleModifier, scaleModifier, scaleModifier));
                    vec3f3.subtract(new Vec3f(.21f, .21f, .21f));
                    //vec3f3.clamp(0.0f, 1.0f);
                    vec3f2.add(vec3f3);
                    //vec3f2.lerp(new Vec3f(0.75f, 0.75f, 0.75f), 0.04f);
                    if (this.renderer.getSkyDarkness(delta) > 0.0f)
                    {
                        r = this.renderer.getSkyDarkness(delta);
                        vec3f4 = vec3f2.copy();
                        vec3f4.multiplyComponentwise(0.7f, 0.6f, 0.6f);
                        vec3f2.lerp(vec3f4, r);
                    }
                }
                vec3f2.clamp(0.0f, 1.0f);
                if (i > 0.0f && (s = Math.max(vec3f2.getX(), Math.max(vec3f2.getY(), vec3f2.getZ()))) < 1.0f)
                {
                    r = 1.0f / s;
                    vec3f4 = vec3f2.copy();
                    vec3f4.scale(r);
                    vec3f2.lerp(vec3f4, i);
                }
                float s2 = this.client.options.getGamma().getValue().floatValue();
                Vec3f vec3f5 = vec3f2.copy();
                vec3f5.modify(this::easeOutQuart);
                vec3f2.lerp(vec3f5, s2);
                //vec3f2.lerp(new Vec3f(0.75f, 0.75f, 0.75f), 0.04f);
                vec3f2.clamp(0.0f, 1.0f);
                vec3f2.scale(255.0f);
                int t = 255;
                int u = (int) vec3f2.getX();
                int v = (int) vec3f2.getY();
                int w = (int) vec3f2.getZ();
                this.image.setColor(l, k, 0xFF000000 | w << 16 | v << 8 | u);
            }
        }
        this.texture.upload();
        this.client.getProfiler().pop();
    }

    private static final float[] MOON_PHASE_VALUES = new float[]{.075f, .02f, -.01f, -.06f, -.15f, -.06f, -.01f, .02f};
    private static final long DAY_END = 13000;
    private static final long DAY_START = 1000;

    private static float MoonPhaseModifier(boolean isDay, ClientWorld world)
    {
        return (!isDay) ? MOON_PHASE_VALUES[world.getMoonPhase()] : 0f;
    }

    private static boolean IsDayClient(long time)
    {
        return time >= DAY_START && time < DAY_END;
    }

    private static float GetNightMidpoint(long time)
    {
        if (time < 13000) return 0f;
        long diff = Math.abs(18000L - time);
        return MathHelper.clamp(1f - (float) diff / 6000L, 0f, 1f);
    }

}
