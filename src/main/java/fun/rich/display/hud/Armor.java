//package fun.rich.common.managers.draggables;
//
//import com.google.common.collect.Lists;
//import net.minecraft.client.gui.DrawContext;
//import net.minecraft.client.render.RenderLayer;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.Identifier;
//import net.minecraft.util.math.MathHelper;
//import fun.rich.common.managers.api.draggable.AbstractDraggable;
//
//import java.util.List;

//public class Armor extends AbstractDraggable {
//
//    private static final int SLOT_SIZE = 22;
//    private static final int SLOT_COUNT = 4;
//    private static final Identifier HOTBAR_TEXTURE = Identifier.of("minecraft", "hud/hotbar");
//
//    public Armor() {
//        super("Armor", (int) 0.0F, (int) 0.0F, SLOT_COUNT * SLOT_SIZE, SLOT_SIZE, false);
//    }
//
//    @Override
//    public boolean visible() {
//        return true;
//    }
//
//    @Override
//    public void drawDraggable(DrawContext context) {
//        if (mc.player == null) {
//            return;
//        }
//
//        if (!isDragging()) {
//            int scaledWidth = context.getScaledWindowWidth();
//            int scaledHeight = context.getScaledWindowHeight();
//            setX(scaledWidth / 2 + 91 + 2);
//            setY((int) (scaledHeight - 22F));
//        }
//
//        List<ItemStack> armorList = Lists.reverse(mc.player.getInventory().armor);
//
//        context.getMatrices().push();
//        context.getMatrices().translate(0.0F, 0.0F, -90.0F);
//
//        for (int i = 0; i < SLOT_COUNT; i++) {
//            int slotX = MathHelper.floor(getX() + i * SLOT_SIZE);
//            int slotY = MathHelper.floor(getY());
//
//            context.drawGuiTexture(
//                    RenderLayer::getGuiTextured,
//                    HOTBAR_TEXTURE,
//                    182, 22,
//                    -1, 0,
//                    slotX, slotY,
//                    SLOT_SIZE, SLOT_SIZE
//            );
//
//            if (i < armorList.size()) {
//                ItemStack stack = armorList.get(i);
//                if (!stack.isEmpty()) {
//                    int itemX = slotX + 4;
//                    int itemY = slotY + 3;
//                    context.drawItem(stack, itemX, itemY);
//                    context.drawStackOverlay(mc.textRenderer, stack, itemX, itemY);
//                }
//            }
//        }
//
//        context.getMatrices().pop();
//    }
//}