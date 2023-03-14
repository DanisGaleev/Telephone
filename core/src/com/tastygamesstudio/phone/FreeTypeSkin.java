package com.tastygamesstudio.phone;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;


/**
 * <h2>Overview</h2>
 *
 * <p>A Skin that generates fonts from {@link BitmapFont}s from TFF files.
 * This skin understands any property of {@link FreeTypeFontParameter}
 * except {@code incremental} and {@code packer} if it specified
 * in the <code>BitmapFont</code> section of skin file. Additionally,
 * the <code>file</code> property specifies source TTF file.
 * </p>
 *
 * <p><strong>Please note, that you cannot load BitmapFonts the old way
 * (e.g. via <code>fnt</code> files) with this skin.</strong></p>
 * <br>
 *
 * <p>
 * When <code>BitmapFont</code> section defines several fonts that use
 * the same underlying TTF file, it will be loaded only once and reused
 * to generate each of the BitmapFonts. The <code>size</code> and the
 * <code>font</code> properties are required, the rest are optional. If
 * optional property is omitted, its value default to the one specified
 * in {@link FreeTypeFontParameter} class.
 * </p>
 *
 * <h2>Example skin file that defines a FreeType font</h2>
 *
 * <pre>{@code
 * BitmapFont: {
 *   default: {
 *     // required properties
 *     file: myfont.ttf,
 *     size: 14,
 *
 *     // optional properties
 *     color: { r: 1, g: 1, b: 1, a: 1 },
 *     borderColor: { r: 0.8, g: 0.8, b: 0.8, a: 1 }
 *     shadowColor: { r: 0, g: 0, b: 0, a: 1 },
 *     shadowOffsetX: -3
 *     shadowOffsetY: 2,
 *     // and more...
 *   }
 * }
 * }</pre>
 *
 * <p>And then, use it:</p>
 *
 * <pre>{@code
 * Skin skin = new FreeTypeSkin(Gdx.files.internal("assets/uiskin.json"));
 * Window window = new Window("Some Title", skin);
 * }</pre>
 *
 * <p>For the complete list of optional properties please refer to the
 * documentation of {@link FreeTypeFontParameter}.</p>
 *
 * <h2>Using with {@link AssetManager}</h2>
 *
 * <p>To use it with AssetManager, the default asset loader for {@link Skin}
 * must be replaced with {@link FreeTypeSkinLoader}.</p>
 *
 * <pre>{@code
 * AssetManager manager = new AssetManager();
 *
 * // replace the builtin skin loader with FreeTypeSkinLoader
 * manager.setLoader(Skin.class, new FreeTypeSkinLoader());
 * }</pre>
 *
 * <p>After than, skins can be loaded and used as usual:</p>
 *
 * <pre>{@code
 * // load the skin via asset manager
 * manager.load(Skin.class, "assets/uiskin.json");
 * manager.finishLoading();
 *
 * // and use it
 * Skin skin = manager.get("assets/uiskin.json", Skin.class);
 * Window window = new Window("Some Title", skin);
 * }</pre>
 *
 * @author acanthite
 * @see FreeTypeSkinLoader
 * @see FreeTypeFontParameter
 */
public class FreeTypeSkin extends Skin {
    private FontGeneratorPool fontGeneratorPool;

    public FreeTypeSkin() {
    }

    public FreeTypeSkin(FileHandle skinFile) {
        super(skinFile);
    }

    public FreeTypeSkin(FileHandle skinFile, TextureAtlas atlas) {
        super(skinFile, atlas);
    }

    public FreeTypeSkin(TextureAtlas atlas) {
        super(atlas);
    }

    @Override
    public void load(FileHandle skinFile) {
        fontGeneratorPool = new FontGeneratorPool();
        super.load(skinFile);
        fontGeneratorPool.dispose();
    }

    /**
     * Overrides serializer for BitmapFont to make it possible to generate BitmapFonts from TTF
     */
    @Override
    protected Json getJsonLoader(FileHandle skinFile) {
        Json json = super.getJsonLoader(skinFile);
        json.setSerializer(FreeTypeFontParameter.class, new FreeTypeFontParametersDeserializer());
        json.setSerializer(BitmapFont.class, new BitmapFontDeserializer(skinFile, fontGeneratorPool));
        return json;
    }

    /**
     * Disposes all {@link FreeTypeFontGenerator} instances if any.
     * And calls {@link Skin#dispose()} to dispose the rest.
     *
     * @see Skin#dispose()
     */
    @Override
    public void dispose() {
        // in case of any exceptions during serialization,
        // make sure everything is cleaned up
        if (fontGeneratorPool != null) {
            fontGeneratorPool.dispose();
        }
        super.dispose();
    }
}