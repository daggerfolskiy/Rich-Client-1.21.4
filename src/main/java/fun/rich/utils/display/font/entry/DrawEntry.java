package fun.rich.utils.display.font.entry;

import fun.rich.utils.display.font.glyph.Glyph;

public record DrawEntry(float atX, float atY, int color, Glyph toDraw) {
}
