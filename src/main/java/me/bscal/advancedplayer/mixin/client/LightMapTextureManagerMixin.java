package me.bscal.advancedplayer.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import org.joml.Vector3f;
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

    @Shadow
    private static void clamp(Vector3f vec)
    {
    }

    /**
     * Mixin to make minecrafts lightmap dark darker. It was easier to Inject and cancel, maybe update into smaller mixins
     */
    @Inject(method = "update", at = @At(value = "HEAD"), cancellable = true)
    public void update(float delta, CallbackInfo ci)
    {
        ci.cancel();
        if (this.dirty)
        {
            this.dirty = false;
            this.client.getProfiler().push("lightTex");

            ClientWorld clientWorld = this.client.world;
            if (clientWorld != null)
            {
                // bscal
                long time = clientWorld.getTimeOfDay() % 24000L;
                boolean isDay = IsDayClient(time);
                float nightMidpoint = GetNightMidpoint(time);
                float scaleModifier = MathHelper.lerp(nightMidpoint, 0f, MoonPhaseModifier(isDay, clientWorld));

                if (!clientWorld.getDimension().hasSkyLight())
                    scaleModifier = 0f;
                // end

                float f = clientWorld.getSkyBrightness(1.0F);
                float g;
                if (clientWorld.getLightningTicksLeft() > 0) {
                    g = 1.0F;
                } else {
                    g = f * 0.95F + 0.05F;
                }

                float h = ((Double)this.client.options.getDarknessEffectScale().getValue()).floatValue();
                float i = this.GetDarknessFactor(delta) * h;
                float j = this.GetDarkness(this.client.player, i, delta) * h;
                float k = this.client.player.getUnderwaterVisibility();
                float l;
                if (this.client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                    l = GameRenderer.getNightVisionStrength(this.client.player, delta);
                } else if (k > 0.0F && this.client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER)) {
                    l = k;
                } else {
                    l = 0.0F;
                }

                Vector3f vector3f = (new Vector3f(f, f, 1.0F)).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
                float m = this.flickerIntensity + 1.5F;
                Vector3f vector3f2 = new Vector3f();

                for(int n = 0; n < 16; ++n) {
                    for(int o = 0; o < 16; ++o) {
                        float p = GetBrightness(clientWorld.getDimension(), n) * g;
                        float q = GetBrightness(clientWorld.getDimension(), o) * m;
                        float s = q * ((q * 0.6F + 0.4F) * 0.6F + 0.4F);
                        float t = q * (q * q * 0.6F + 0.4F);
                        vector3f2.set(q, s, t);
                        boolean bl = clientWorld.getDimensionEffects().shouldBrightenLighting();
                        float u;
                        Vector3f vector3f4;
                        if (bl) {
                            vector3f2.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
                            clamp(vector3f2);
                        } else {
/*                            Vector3f vector3f3 = (new Vector3f(vector3f)).mul(p);
                            vector3f2.add(vector3f3);
                            vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                            if (this.renderer.getSkyDarkness(delta) > 0.0F) {
                                u = this.renderer.getSkyDarkness(delta);
                                vector3f4 = (new Vector3f(vector3f2)).mul(0.7F, 0.6F, 0.6F);
                                vector3f2.lerp(vector3f4, u);
                            }*/

                            // TODO sunset/sunset
                            // TODO moon stages lights
                            // TODO maybe noon increase light
                            // TODO flash of green, bloodmoons
                            // TODO adjust blue at nighttime

                            // These seem to be decent values:
                            // Caves are pitch black but midnight is dark but enough to see outlines
                            Vector3f vec3f3 = new Vector3f(vector3f);
                            vec3f3.mul(p * 2.0f);
                            vec3f3.add(new Vector3f(scaleModifier, scaleModifier, scaleModifier));
                            vec3f3.sub(new Vector3f(.21f, .21f, .21f));

                            vector3f2.add(vec3f3);
                            //vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                            if (this.renderer.getSkyDarkness(delta) > 0.0f)
                            {
                                u = this.renderer.getSkyDarkness(delta);
                                vector3f4 = (new Vector3f(vector3f2)).mul(0.7F, 0.6F, 0.6F);
                                vector3f2.lerp(vector3f4, u);
                            }
                        }

                        float v;
                        if (l > 0.0F) {
                            v = Math.max(vector3f2.x(), Math.max(vector3f2.y(), vector3f2.z()));
                            if (v < 1.0F) {
                                u = 1.0F / v;
                                vector3f4 = (new Vector3f(vector3f2)).mul(u);
                                vector3f2.lerp(vector3f4, l);
                            }
                        }

                        if (!bl) {
                            if (j > 0.0F) {
                                vector3f2.add(-j, -j, -j);
                            }

                            clamp(vector3f2);
                        }

                        v = ((Double)this.client.options.getGamma().getValue()).floatValue();
                        Vector3f vector3f5 = new Vector3f(this.easeOutQuart(vector3f2.x), this.easeOutQuart(vector3f2.y), this.easeOutQuart(vector3f2.z));
                        vector3f2.lerp(vector3f5, Math.max(0.0F, v - i));
                        vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                        clamp(vector3f2);
                        vector3f2.mul(255.0F);
                        int x = (int)vector3f2.x();
                        int y = (int)vector3f2.y();
                        int z = (int)vector3f2.z();
                        this.image.setColor(o, n, -16777216 | z << 16 | y << 8 | x);
                    }
                }

                this.texture.upload();
                this.client.getProfiler().pop();
            }
        }
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

    private static float GetBrightness(DimensionType type, int lightLevel) {
        float f = (float)lightLevel / 15.0F;
        float g = f / (4.0F - 3.0F * f);
        return MathHelper.lerp(type.ambientLight(), g, 1.0F);
    }

    private float GetDarknessFactor(float delta) {
        if (this.client.player.hasStatusEffect(StatusEffects.DARKNESS)) {
            StatusEffectInstance statusEffectInstance = this.client.player.getStatusEffect(StatusEffects.DARKNESS);
            if (statusEffectInstance != null && statusEffectInstance.getFactorCalculationData().isPresent()) {
                return ((StatusEffectInstance.FactorCalculationData)statusEffectInstance.getFactorCalculationData().get()).lerp(this.client.player, delta);
            }
        }

        return 0.0F;
    }

    private float GetDarkness(LivingEntity entity, float factor, float delta) {
        float f = 0.45F * factor;
        return Math.max(0.0F, MathHelper.cos(((float)entity.age - delta) * 3.1415927F * 0.025F) * f);
    }

}
