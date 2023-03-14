package com.tastygamesstudio.phone;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Represents a pool of {@link FreeTypeFontGenerator} to make it possible to reuse
 * font generators for the same TTF file. For instance when skin defines several {@link BitmapFont}s
 * with the same source TTF.
 */
class FontGeneratorPool implements Disposable {
    private final ObjectMap<FileHandle, FreeTypeFontGenerator> pool = new ObjectMap<>();

    /**
     * @param ttfFile font file get a generator for
     * @return an instance of {@link FreeTypeFontGenerator} from pool if any, or creates
     * a new instance on pool and returns it.
     */
    public FreeTypeFontGenerator getFor(FileHandle ttfFile) {
        FreeTypeFontGenerator generator = pool.get(ttfFile);
        if (generator == null) {
            generator = new FreeTypeFontGenerator(ttfFile);
            pool.put(ttfFile, generator);
        }
        return generator;
    }

    /**
     * Disposes all instances of {@link FreeTypeFontGenerator} and clears this pull.
     */
    @Override
    public void dispose() {
        for (FreeTypeFontGenerator generator : pool.values()) {
            generator.dispose();
        }

        pool.clear();
    }
}
