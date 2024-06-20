package dev.isxander.controlify.utils.render;

//? if >=1.20.3 {
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
//?}

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public record ControlifySprite(ResourceLocation atlas, SpriteScaling scaling, float u0, float u1, float v0, float v1, RenderType renderType) {

    public ControlifySprite(ResourceLocation atlas, SpriteScaling scaling, float u0, float u1, float v0, float v1) {
        this(atlas, scaling, u0, u1, v0, v1, RenderType.text(atlas));
    }

    public float getU(float delta) {
        return Mth.lerp(delta, u0, u1);
    }

    public float getV(float delta) {
        return Mth.lerp(delta, v0, v1);
    }

    //? if >=1.20.3 {
    public ControlifySprite(
            TextureAtlasSprite sprite,
            GuiSpriteScaling scaling
    ) {
        this(sprite.atlasLocation(), fromVanillaScale(scaling), sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
    }

    public static ControlifySprite fromSpriteId(ResourceLocation spriteId) {
        GuiSpriteManager sprites = Minecraft.getInstance().getGuiSprites();
        TextureAtlasSprite sprite = sprites.getSprite(spriteId);
        GuiSpriteScaling scaling = sprites.getSpriteScaling(sprite);

        return new ControlifySprite(sprite, scaling);
    }

    private static SpriteScaling fromVanillaScale(net.minecraft.client.resources.metadata.gui.GuiSpriteScaling scaling) {
        if (scaling instanceof GuiSpriteScaling.Stretch)
            return new SpriteScaling.Stretch();
        else if (scaling instanceof GuiSpriteScaling.Tile tile)
            return new SpriteScaling.Tiled(tile.width(), tile.height());
        else if (scaling instanceof GuiSpriteScaling.NineSlice nineSlice)
            return new SpriteScaling.NineSlice(
                    nineSlice.width(),
                    nineSlice.height(),
                    new SpriteScaling.NineSlice.Border(
                            nineSlice.border().left(),
                            nineSlice.border().right(),
                            nineSlice.border().top(),
                            nineSlice.border().bottom()
                    )
            );
        throw new IllegalArgumentException("Unknown scaling");
    }
    //?} else {
    /*public ControlifySprite(ResourceLocation atlas, SpriteScaling scaling) {
        this(atlas, scaling, 0, 1, 0, 1);
    }
    *///?}
}
