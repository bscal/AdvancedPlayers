package me.bscal.advancedplayer.client;

import io.github.cottonmc.cotton.gui.widget.data.Texture;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.client.debug.DebugWindow;
import me.bscal.advancedplayer.client.renderers.GhoulRenderer;
import me.bscal.advancedplayer.client.ui.PlayerStatusHud;
import me.bscal.advancedplayer.common.APPlayer;
import me.bscal.advancedplayer.common.entities.EntityRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.TextureHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.*;
import net.minecraft.client.texture.atlas.AtlasLoader;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class AdvancedPlayerClient implements ClientModInitializer
{
    public static KeyBinding DebugTemperatureKeyBind;

    public static final DebugWindow DEBUG_WINDOW = new DebugWindow();
    public static final Identifier TEXTURE_THERMOMETER = GetUiTexture("thermometer");
    public static final Identifier TEXTURE_BURNING = GetUiTexture("temperature_burning");
    public static final Identifier TEXTURE_HOT = GetUiTexture("temperature_hot");
    public static final Identifier TEXTURE_WARM = GetUiTexture("temperature_warm");
    public static final Identifier TEXTURE_NORMAL = GetUiTexture("temperature_normal");
    public static final Identifier TEXTURE_COOL = GetUiTexture("temperature_cool");
    public static final Identifier TEXTURE_COLD = GetUiTexture("temperature_cold");
    public static final Identifier TEXTURE_FREEZING = GetUiTexture("temperature_freezing");
    public static final Identifier TEXTURE_NEUTRAL = GetUiTexture("neutral_line");
    public static final Identifier TEXTURE_UP_CARROT = GetUiTexture("up_carrot");
    public static final Identifier TEXTURE_DOWN_CARROT = GetUiTexture("down_carrot");
    public static final Identifier TEXTURE_WATER = GetUiTexture("water_skin_sprites");

    //public static SpriteAtlasTexture AtlasTexture;

    public static APPlayer ClientAPPlayer;

    @Override
    public void onInitializeClient()
    {
        DebugTemperatureKeyBind = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.advancedplayer.debug_temperature", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, "category.advancedplayer.debug"));

        EntityRendererRegistry.register(EntityRegistry.GHOUL_ENTITY, GhoulRenderer::new);

        ClientAPPlayer = new APPlayer(MinecraftClient.getInstance());

        ClientPlayNetworking.registerGlobalReceiver(APPlayer.SYNC_PACKET,
                ((client, handler, buf, responseSender) ->
                {
                    ClientAPPlayer.Deserialize(buf);
/*                    final byte[] data = buf.readByteArray();

                    final APPlayer newPlayer = APPlayer.Deserialize(null, data);
                    client.execute(() ->
                    {
                        ClientAPPlayer = newPlayer;
                    });*/
                }));

        HudRenderCallback.EVENT.register(DEBUG_WINDOW);

        // Create sprite sheet
        ClientLifecycleEvents.CLIENT_STARTED.register((client) ->
        {
            //var rm = client.getResourceManager();
            //var id = new Identifier(AdvancedPlayer.MOD_ID, "textures");
            //AtlasTexture = new SpriteAtlasTexture(id);
            List<Identifier> textures = List.of(
                    TEXTURE_BURNING,
                    TEXTURE_HOT,
                    TEXTURE_WARM,
                    TEXTURE_NORMAL,
                    TEXTURE_COOL,
                    TEXTURE_COLD,
                    TEXTURE_FREEZING,
                    TEXTURE_THERMOMETER,
                    TEXTURE_NEUTRAL,
                    TEXTURE_DOWN_CARROT,
                    TEXTURE_UP_CARROT,
                    TEXTURE_WATER);

/*            List<Vector2i> sizes = List.of(
                    new Vector2i(16, 32),
                    new Vector2i(16, 32),
                    new Vector2i(16, 32),
                    new Vector2i(16, 32),
                    new Vector2i(16, 32),
                    new Vector2i(16, 32),
                    new Vector2i(16, 32),
                    new Vector2i(16, 32),
                    new Vector2i(16, 32),
                    new Vector2i(16, 16),
                    new Vector2i(16, 16),
                    new Vector2i(208, 16));

            List<SpriteContents> sprites = new ArrayList<>();
            for (int i = 0 ; i < textures.size(); ++i)
            {
                sprites.add(MakeSprite(textures.get(i), sizes.get(i)));
            }
            var loaderId = new Identifier(AdvancedPlayer.MOD_ID, "sprite_loader");
            SpriteLoader loader = new SpriteLoader(loaderId, 2048, 2048, 2048);
            SpriteLoader.StitchResult res = loader.stitch(sprites,0, (r) -> {});
            res.readyForUpload().whenComplete((result, throwable) -> {
                if (throwable != null) throwable.printStackTrace();
                AtlasTexture.upload(res);
                AtlasTexture.registerTexture(client.getTextureManager(), rm, id, (r) ->
                {
                });
                AdvancedPlayer.LOGGER.info("AtlasTexture loaded!");
            });*/

            for (var textureId : textures)
            {
                client.getTextureManager().getTexture(textureId);
            }
            HudRenderCallback.EVENT.register(new PlayerStatusHud());
        });
    }

    public static SpriteContents MakeSprite(Identifier id, Vector2i size)
    {
        SpriteDimensions dims = new SpriteDimensions(size.x, size.y);
        NativeImage img = new NativeImage(dims.width(), dims.height(), true);
        AnimationResourceMetadata animData = new AnimationResourceMetadata(List.of(), dims.width(), dims.height(), 0, false);
        SpriteContents contents = new SpriteContents(id, dims, img, animData);
        return contents;
    }

    public static Identifier GetUiTexture(String filename)
    {
        return new Identifier(AdvancedPlayer.MOD_ID, "textures/ui/" + filename + ".png");
    }
}
