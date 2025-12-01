package fun.rich.display.hud;

import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import fun.rich.utils.display.font.FontRenderer;
import fun.rich.utils.client.packet.network.Network;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.interactions.simulate.Simulations;
import fun.rich.utils.client.managers.api.draggable.AbstractDraggable;
import fun.rich.utils.display.font.Fonts;

import java.awt.*;
import java.util.Objects;

public class PlayerInfo extends AbstractDraggable {
    public PlayerInfo() {
        super("Player Info", 0, 0, 60, 0, false);
    }

    @Override
    public void drawDraggable(DrawContext context) {
        int offset = PlayerInteractionHelper.isChat(mc.currentScreen) ? -28 : -15;
        setY(window.getScaledHeight() + offset);

        BlockPos blockPos = Objects.requireNonNull(mc.player).getBlockPos();
        FontRenderer font = Fonts.getSize(14, Fonts.Type.DEFAULT);

        String bps = "Bps: " + Calculate.round(Simulations.getSpeedSqrt(mc.player) * 20.0F, 0.25F);
        String tps = "Tps: " + Calculate.round(Network.TPS, 0.1F);
        String xyz = "Xyz: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ();
        String text = xyz + " • " + tps + " • " + bps;

        float width = font.getStringWidth(text) + 10;
        setWidth((int) width);
        font.drawGradientString(context.getMatrices(), text, getX() + 3, getY() + 6.5F, new Color(225, 225, 255, 255).getRGB(), new Color(255, 255, 255, 255).getRGB());
    }
}