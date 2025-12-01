package fun.rich.display.screens.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import fun.rich.features.module.ModuleCategory;
import fun.rich.utils.client.managers.file.exception.FileLoadException;
import fun.rich.utils.client.managers.file.exception.FileSaveException;
import fun.rich.common.discord.DiscordManager;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.display.screens.clickgui.MenuScreen;
import fun.rich.display.screens.clickgui.components.AbstractComponent;
import fun.rich.Rich;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.display.geometry.Render2D;
import fun.rich.utils.math.calc.Calculate;
import antidaunleak.api.UserProfile;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import org.lwjgl.glfw.GLFW;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import net.minecraft.client.MinecraftClient;
import fun.rich.utils.display.scissor.ScissorAssist;
import org.joml.Matrix4f;
import net.minecraft.util.math.MathHelper;
import com.mojang.blaze3d.systems.RenderSystem;

@Setter @Accessors(chain = true)
public class BackgroundComponent extends AbstractComponent {
    private String editingConfig = null;
    private String newName = "";
    private int editCursor = 0;
    private boolean isDefaultTab = true;
    private float highlightX = 55f;
    private List<Map<String, Object>> configs = new ArrayList<>();
    private String configInput = "";
    private boolean editingInput = false;
    private int inputCursor = 0;
    private float scroll = 0f;
    private float smoothedScroll = 0f;
    private boolean loadedConfigs = false;
    private boolean cloudLoading = false;
    private long cloudLoadStartTime = 0;
    private boolean cloudDataReady = false;
    private List<Map<String, Object>> tempConfigs = new ArrayList<>();
    private float loadingAlpha = 0f;
    private float configsAlpha = 0f;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String currentTime = LocalTime.now().format(formatter);
        String point = " • ";
        DiscordManager discord = Rich.getInstance().getDiscordManager();
        Rich.getInstance().getScissorManager().push(matrix.peek().getPositionMatrix(), 0, 0, window.getScaledWidth(), window.getScaledHeight());

        blur.render(ShapeProperties.create(matrix, x, y, width, height).round(8).quality(64)
                .color(new Color(0, 0, 0, 200).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(8)
                .softness(22)
                        .thickness(0.1f)
                        .outlineColor(new Color(18, 19, 20, 225).getRGB())
                .color(
                new Color(18, 19, 20, 175).getRGB(),
                new Color(0, 2, 5, 175).getRGB(),
                new Color(0, 2, 5, 175).getRGB(),
                new Color(18, 19, 20, 175).getRGB())
                .build());

        List<Map<String, Object>> displayedConfigs = new ArrayList<>();
        if (MenuScreen.INSTANCE.getCategory() == ModuleCategory.CONFIGS) {
            if (!loadedConfigs) {
                refreshConfigs();
                loadedConfigs = true;
            }
            if (cloudLoading && !isDefaultTab) {
                if (cloudDataReady && (System.currentTimeMillis() - cloudLoadStartTime >= 3000)) {
                    configs = tempConfigs;
                    cloudLoading = false;
                }
                loadingAlpha = Calculate.interpolate(loadingAlpha, 1f, 0.1f);
                configsAlpha = Calculate.interpolate(configsAlpha, 0f, 0.1f);
            } else {
                loadingAlpha = Calculate.interpolate(loadingAlpha, 0f, 0.1f);
                configsAlpha = Calculate.interpolate(configsAlpha, 1f, 0.1f);
            }
            rectangle.render(ShapeProperties.create(context.getMatrices(), x + 55F, y + 38, 70, 15)
                    .round(3).thickness(2).softness(1).outlineColor(new Color(54, 54, 56, 255).getRGB()).color(
                            new Color(31, 27, 35, 75).getRGB(), new Color(31, 27, 35, 75).getRGB(), new Color(31, 27, 35, 75).getRGB(), new Color(31, 27, 35, 75).getRGB()).build());
            float targetX = isDefaultTab ? 55f : 90f;
            highlightX = Calculate.interpolate(highlightX, targetX, 0.2f);
            rectangle.render(ShapeProperties.create(context.getMatrices(), x + highlightX, y + 38, 35, 15)
                    .round(3).thickness(0).softness(0).outlineColor(new Color(54, 54, 56, 0).getRGB()).color(
                            new Color(65, 65, 65, 255).getRGB(), new Color(65, 65, 65, 255).getRGB(), new Color(65, 65, 65, 255).getRGB(), new Color(65, 65, 65, 255).getRGB()).build());
            rectangle.render(ShapeProperties.create(context.getMatrices(), x + 43F, y + 60, width - 43F, 0.5F)
                    .color(new Color(55, 55, 70, 250).getRGB(), new Color(55, 55, 70, 15).getRGB(), new Color(55, 55, 70, 250).getRGB(), new Color(55, 55, 70, 15).getRGB()).build());
            Fonts.getSize(16, Fonts.Type.DEFAULT).drawString(matrix, "Default", x + 60F, y + 43, ColorAssist.getText(0.7f));
            Fonts.getSize(16, Fonts.Type.DEFAULT).drawString(matrix, "Cloud", x + 97F, y + 43, ColorAssist.getText(0.7f));
            blur.render(ShapeProperties.create(context.getMatrices(), x + 340F, y + 38, 80, 15)
                    .round(3).quality(64)
                    .color(new Color(0, 0, 0, 200).getRGB())
                    .build());

            rectangle.render(ShapeProperties.create(context.getMatrices(), x + 340F, y + 38, 80, 15)
                    .round(3)
                    .softness(2)
                    .thickness(0.5f)
                    .outlineColor(new Color(18, 19, 20, 225).getRGB())
                    .color(
                            new Color(18, 19, 20, 175).getRGB(),
                            new Color(0, 2, 5, 175).getRGB(),
                            new Color(0, 2, 5, 175).getRGB(),
                            new Color(18, 19, 20, 175).getRGB())
                    .build());

            blur.render(ShapeProperties.create(context.getMatrices(), x + 292F, y + 38, 40, 15)
                    .round(3).quality(64)
                    .color(new Color(0, 0, 0, 200).getRGB())
                    .build());

            rectangle.render(ShapeProperties.create(context.getMatrices(), x + 292F, y + 38, 40, 15)
                    .round(3)
                    .softness(2)
                    .thickness(0.5f)
                    .outlineColor(new Color(18, 19, 20, 225).getRGB())
                    .color(
                            new Color(18, 19, 20, 175).getRGB(),
                            new Color(0, 2, 5, 175).getRGB(),
                            new Color(0, 2, 5, 175).getRGB(),
                            new Color(18, 19, 20, 175).getRGB())
                    .build());

            rectangle.render(ShapeProperties.create(matrix, x + 405F, y + 42, 0.5f, 7)
                    .color(new Color(155, 155, 155, 55).getRGB()).build());


            Fonts.getSize(20, Fonts.Type.GUIICONS).drawString(matrix, "M", x + 296F, y + 42f, ColorAssist.getText(1f));


                    Fonts.getSize(16, Fonts.Type.REGULAR).drawString(matrix, "Save", x + 307F, y + 43.5f, ColorAssist.getText(1f));

            blur.render(ShapeProperties.create(context.getMatrices(), x + 250F, y + 38, 38, 15)
                    .round(3).quality(64)
                    .color(new Color(0, 0, 0, 200).getRGB())
                    .build());

            rectangle.render(ShapeProperties.create(context.getMatrices(), x + 250F, y + 38, 38, 15)
                    .round(3)
                    .softness(2)
                    .thickness(0.5f)
                    .outlineColor(new Color(18, 19, 20, 225).getRGB())
                    .color(
                            new Color(18, 19, 20, 175).getRGB(),
                            new Color(0, 2, 5, 175).getRGB(),
                            new Color(0, 2, 5, 175).getRGB(),
                            new Color(18, 19, 20, 175).getRGB())
                    .build());   Fonts.getSize(21, Fonts.Type.GUIICONS).drawString(matrix, "O", x + 253F, y + 43, ColorAssist.getText(1f));

            Fonts.getSize(16, Fonts.Type.REGULAR).drawString(matrix, "Clear", x + 263F, y + 43.5f, ColorAssist.getText(1f));

            String placeholder = isDefaultTab ? "Поиск" : "Добавить по ID";
            String inputDisplay = (configInput.isEmpty() && !editingInput) ? placeholder : configInput;
            Fonts.getSize(15, Fonts.Type.REGULAR).drawString(matrix, inputDisplay, x + 343F, y + 43.5f, ColorAssist.getText(0.6f));
            Fonts.getSize(26, Fonts.Type.ICONS).drawString(matrix, "U", x + 405, y + 41f, ColorAssist.getText(0.6f));
            if (editingInput && System.currentTimeMillis() % 1000 < 500) {
                float curWidth = Fonts.getSize(15, Fonts.Type.REGULAR).getStringWidth(configInput.substring(0, inputCursor));
                Fonts.getSize(15, Fonts.Type.DEFAULT).drawString(matrix, "|", x + 342F + curWidth, y + 43.5f - 0.5f, ColorAssist.getText(0.7f));
            }
            displayedConfigs = isDefaultTab ? configs.stream().filter(m -> ((String) m.get("name")).toLowerCase().contains(configInput.toLowerCase())).collect(Collectors.toList()) : configs;
            int configsPerRow = 2;
            int numConfigs = displayedConfigs.size();
            int rows = (numConfigs + configsPerRow - 1) / configsPerRow;
            float contentHeight = numConfigs > 0 ? 50 + (rows - 1) * 55f : 0f;
            float viewHeight = height - 70f;
            float maxScrollAmount = Math.max(0f, contentHeight - viewHeight) + 7;
            if (numConfigs < 7) {
                maxScrollAmount = 0f;
                scroll = 0f;
                smoothedScroll = 0f;
            }
            scroll = MathHelper.clamp(scroll, -maxScrollAmount, 0f);
            smoothedScroll = Calculate.interpolate(smoothedScroll, scroll, 0.2f);
            Matrix4f positionMatrix = matrix.peek().getPositionMatrix();
            ScissorAssist scissorManager = Rich.getInstance().getScissorManager();
            float listX = x + 43f;
            float listY = y + 65f;
            float listWidth = width - 43f - 15f;
            float listHeight = viewHeight;
            scissorManager.push(positionMatrix, listX, listY, listWidth, listHeight);
            if (!isDefaultTab) {
                RenderSystem.setShaderColor(1f, 1f, 1f, configsAlpha);
            }
            float configY = y + 70 + smoothedScroll;
            int index = 0;
            for (Map<String, Object> map : displayedConfigs) {
                String config = (String) map.get("name");
                float configX = x + 55 + (index % configsPerRow) * 190;
                if (index % configsPerRow == 0 && index > 0) configY += 55;
                if (configY + 50 > y + 60 && configY < y + height) {

                    blur.render(ShapeProperties.create(matrix, configX, configY, 180, 50)
                            .round(5).quality(64)
                            .color(new Color(0, 0, 0, 200).getRGB())
                            .build());

                    rectangle.render(ShapeProperties.create(matrix, configX, configY, 180, 50)
                            .round(5)
                            .softness(2)
                            .thickness(0.1f)
                            .outlineColor(new Color(28, 29, 30, 225).getRGB())
                            .color(
                                    new Color(18, 19, 20, 175).getRGB(),
                                    new Color(0, 2, 5, 175).getRGB(),
                                    new Color(0, 2, 5, 175).getRGB(),
                                    new Color(18, 19, 20, 175).getRGB())
                            .build());

                   rectangle.render(ShapeProperties.create(context.getMatrices(), configX, configY + 22, 180, 0.5F)
                            .color(new Color(55, 55, 70, 250).getRGB(), new Color(55, 55, 70, 15).getRGB(), new Color(55, 55, 70, 250).getRGB(), new Color(55, 55, 70, 15).getRGB()).build());
                    rectangle.render(ShapeProperties.create(context.getMatrices(), configX, configY, 20.5f, 19)
                            .round(1, 7, 4, 1).thickness(2).softness(1).outlineColor(new Color(54, 54, 56, 255).getRGB()).color(
                                    new Color(55, 55, 55, 255).getRGB(), new Color(55, 55, 55, 255).getRGB(), new Color(55, 55, 55, 255).getRGB(), new Color(55, 55, 55, 255).getRGB()).build());
                    Fonts.getSize(26, Fonts.Type.ICONSCATEGORY).drawString(matrix, "F", configX + 3.5F, configY + 5, ColorAssist.getText());
                    String displayName = config;
                    if (config.equals(editingConfig)) {
                        displayName = newName;
                        Fonts.getSize(16, Fonts.Type.DEFAULT).drawString(matrix, displayName, configX + 25F, configY + 9, ColorAssist.getText());
                        if (System.currentTimeMillis() % 1000 < 500) {
                            float curWidth = Fonts.getSize(16, Fonts.Type.DEFAULT).getStringWidth(newName.substring(0, editCursor));
                            Fonts.getSize(16, Fonts.Type.DEFAULT).drawString(matrix, "|", configX + 24F + curWidth, configY + 9 - 0.5f, ColorAssist.getText());
                        }
                    } else {
                        Fonts.getSize(16, Fonts.Type.DEFAULT).drawString(matrix, displayName, configX + 25F, configY + 9, ColorAssist.getText());
                    }
                    Number createdNum = (Number) map.getOrDefault("created", 0L);
                    Number updatedNum = (Number) map.getOrDefault("updated", 0L);
                    long createdTime = createdNum.longValue();
                    long updatedTime = updatedNum.longValue();
                    String createdStr = createdTime == 0 ? "unknown" : LocalDateTime.ofInstant(Instant.ofEpochMilli(createdTime), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    String updatedStr = updatedTime == 0 ? "unknown" : LocalDateTime.ofInstant(Instant.ofEpochMilli(updatedTime), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    Fonts.getSize(11, Fonts.Type.REGULAR).drawString(matrix, "Created: " + createdStr, configX + 4F, configY + 35, ColorAssist.getText(1f));
                    String updateText = "Updated: " + updatedStr;
                    boolean hasUpdate = (boolean) map.getOrDefault("has_update", false);
                    if (hasUpdate) {
                        updateText += " (доступно обновлений: 1)";
                    }
                    Fonts.getSize(11, Fonts.Type.REGULAR).drawString(matrix, updateText, configX + 4F, configY + 28, ColorAssist.getText(1f));
                    String author = (String) map.getOrDefault("owner", isDefaultTab ? UserProfile.getInstance().profile("username") : "Unknown");
                    Fonts.getSize(11, Fonts.Type.REGULAR).drawString(matrix, "Author: " + author, configX + 4F, configY + 42, ColorAssist.getText(1f));
                    Object avatarObj = map.getOrDefault("avatar_hash", discord.getAvatarId());
                    String avatarHash;
                    if (avatarObj instanceof Map) {
                        Map<String, String> idMap = (Map<String, String>) avatarObj;
                        avatarHash = idMap.get("namespace") + ":" + idMap.get("path");
                    } else {
                        avatarHash = avatarObj.toString();
                    }
                    Render2D.drawTexture(context, Identifier.of(avatarHash), configX + 157F, configY + 3, 16, 7.5f, 0, 15, 21, ColorAssist.getGuiRectColor(1));

                    blur.render(ShapeProperties.create(context.getMatrices(), configX + 162F, configY + 35, 14, 15)
                            .round(3, 0, 3, 0).quality(64)
                            .color(new Color(0, 0, 0, 200).getRGB())
                            .build());

                    rectangle.render(ShapeProperties.create(context.getMatrices(), configX + 162F, configY + 35, 14, 15)
                            .round(3, 0, 3, 0)
                            .softness(2)
                            .thickness(0.1f)
                            .outlineColor(new Color(18, 19, 20, 225).getRGB())
                            .color(
                                    new Color(28, 29, 30, 175).getRGB(),
                                    new Color(0, 2, 5, 175).getRGB(),
                                    new Color(0, 2, 5, 175).getRGB(),
                                    new Color(28, 29, 30, 175).getRGB())
                            .build());

                    blur.render(ShapeProperties.create(context.getMatrices(), configX + 146F, configY + 35, 14, 15)
                            .round(3, 0, 3, 0).quality(64)
                            .color(new Color(0, 0, 0, 200).getRGB())
                            .build());

                    rectangle.render(ShapeProperties.create(context.getMatrices(), configX + 146F, configY + 35, 14, 15)
                            .round(3, 0, 3, 0)
                            .softness(2)
                            .thickness(0.1f)
                            .outlineColor(new Color(18, 19, 20, 225).getRGB())
                            .color(
                                    new Color(28, 29, 30, 175).getRGB(),
                                    new Color(0, 2, 5, 175).getRGB(),
                                    new Color(0, 2, 5, 175).getRGB(),
                                    new Color(28, 29, 30, 175).getRGB())
                            .build());

                    blur.render(ShapeProperties.create(context.getMatrices(), configX + 130.25F, configY + 35, 14, 15)
                            .round(3, 0, 3, 0).quality(64)
                            .color(new Color(0, 0, 0, 200).getRGB())
                            .build());

                    rectangle.render(ShapeProperties.create(context.getMatrices(), configX + 130.25F, configY + 35, 14, 15)
                            .round(3, 0, 3, 0)
                            .softness(2)
                            .thickness(0.1f)
                            .outlineColor(new Color(18, 19, 20, 225).getRGB())
                            .color(
                                    new Color(28, 29, 30, 175).getRGB(),
                                    new Color(0, 2, 5, 175).getRGB(),
                                    new Color(0, 2, 5, 175).getRGB(),
                                    new Color(28, 29, 30, 175).getRGB())
                            .build());

                    blur.render(ShapeProperties.create(context.getMatrices(), configX + 114.35F, configY + 35, 14, 15)
                            .round(3, 0, 3, 0).quality(64)
                            .color(new Color(0, 0, 0, 200).getRGB())
                            .build());

                    rectangle.render(ShapeProperties.create(context.getMatrices(), configX + 114.35F, configY + 35, 14, 15)
                            .round(3, 0, 3, 0)
                            .softness(2)
                            .thickness(0.1f)
                            .outlineColor(new Color(18, 19, 20, 225).getRGB())
                            .color(
                                    new Color(28, 29, 30, 175).getRGB(),
                                    new Color(0, 2, 5, 175).getRGB(),
                                    new Color(0, 2, 5, 175).getRGB(),
                                    new Color(28, 29, 30, 175).getRGB())
                            .build());

                    Fonts.getSize(31, Fonts.Type.GUIICONS).drawString(matrix, "P", configX + 164F, configY + 36f, ColorAssist.getText(1f));
                    Fonts.getSize(21, Fonts.Type.GUIICONS).drawString(matrix, "N", configX + 149F, configY + 39.5f, ColorAssist.getText(1f));
                    Fonts.getSize(22, Fonts.Type.GUIICONS).drawString(matrix, "M", configX + 133F, configY + 38.5f, ColorAssist.getText(1f));
                    Fonts.getSize(24, Fonts.Type.GUIICONS).drawString(matrix, "O", configX + 117F, configY + 38, ColorAssist.getText(1f));
                }
                index++;
            }
            if (!isDefaultTab) {
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }
            scissorManager.pop();
            if (maxScrollAmount > 0) {
                float scrollbarWidth = 4;
                float scrollbarX = x + width - 10;
                float scrollbarY = y + 65;
                float scrollbarHeight = height - 70;
                rectangle.render(ShapeProperties.create(context.getMatrices(), scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight)
                        .round(2).color(new Color(30, 30, 30, 100).getRGB()).build());
                float handleHeight = Math.max(20, scrollbarHeight * (viewHeight / contentHeight));
                float scrollRatio = maxScrollAmount > 0 ? (-smoothedScroll) / maxScrollAmount : 0;
                float handleY = scrollbarY + (scrollbarHeight - handleHeight) * scrollRatio;
                rectangle.render(ShapeProperties.create(context.getMatrices(), scrollbarX, handleY, scrollbarWidth, handleHeight)
                        .round(2).color(new Color(100, 100, 100, 150).getRGB()).build());
            }
        } else {
            loadedConfigs = false;
        }
        rectangle.render(ShapeProperties.create(context.getMatrices(), x + 42.5f, y, 0.5F, height)
                .color(new Color(55, 55, 70, 15).getRGB(), new Color(55, 55, 70, 50).getRGB(), new Color(55, 55, 70, 15).getRGB(), new Color(55, 55, 70, 250).getRGB()).build());

        rectangle.render(ShapeProperties.create(context.getMatrices(), x + 43F, y + 28, width - 43F, 0.5F)
                .color(new Color(55, 55, 70, 250).getRGB(), new Color(55, 55, 70, 15).getRGB(), new Color(55, 55, 70, 250).getRGB(), new Color(55, 55, 70, 15).getRGB()).build());

        blur.render(ShapeProperties.create(matrix, x + 10.5f, y + 10f, 20, 20).round(5).quality(64)
                .color(new Color(0, 0, 0, 200).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, x + 10.5f, y + 10f, 20, 20).round(5)
                .softness(22)
                .thickness(0.1f)
                .outlineColor(new Color(18, 19, 20, 225).getRGB())
                .color(
                        new Color(18, 19, 20, 175).getRGB(),
                        new Color(0, 2, 5, 175).getRGB(),
                        new Color(0, 2, 5, 175).getRGB(),
                        new Color(18, 19, 20, 175).getRGB())
                .build());


        Fonts.getSize(26, Fonts.Type.ICONS).drawString(matrix, "A ", x + 14f, y + 15F, new Color(225, 225, 255, 255).getRGB());

        String icon;
        switch (MenuScreen.INSTANCE.getCategory()) {
            case COMBAT -> {
                icon = "A";
                Fonts.getSize(17, Fonts.Type.ICONSCATEGORY).drawString(matrix, icon, x + 55f, y + 14.5f, new Color(225, 225, 255, 255).getRGB());
            }
            case MOVEMENT -> {
                icon = "B";
                Fonts.getSize(18, Fonts.Type.ICONSCATEGORY).drawString(matrix, icon, x + 54f, y + 14f, new Color(225, 225, 255, 255).getRGB());
            }
            case RENDER -> {
                icon = "C";
                Fonts.getSize(17, Fonts.Type.ICONSCATEGORY).drawString(matrix, icon, x + 54f, y + 14f, new Color(225, 225, 255, 255).getRGB());
            }
            case PLAYER -> {
                icon = "D";
                Fonts.getSize(17, Fonts.Type.ICONSCATEGORY).drawString(matrix, icon, x + 54f, y + 14f, new Color(225, 225, 255, 255).getRGB());
            }
            case MISC -> {
                icon = "E";
                Fonts.getSize(18, Fonts.Type.ICONSCATEGORY).drawString(matrix, icon, x + 54f, y + 14f, new Color(225, 225, 255, 255).getRGB());
            }
            case CONFIGS -> {
                icon = "F";
                Fonts.getSize(17, Fonts.Type.ICONSCATEGORY).drawString(matrix, icon, x + 54f, y + 14f, new Color(225, 225, 255, 255).getRGB());
            }
            case AUTOBUY -> {
                icon = "H";
                Fonts.getSize(33, Fonts.Type.ICONSCATEGORY).drawString(matrix, icon, x + 54f, y + 9f, new Color(225, 225, 255, 255).getRGB());
            }
            default -> {
                icon = MenuScreen.INSTANCE.getCategory().getReadableName().substring(0, 1);
                Fonts.getSize(21, Fonts.Type.ICONSCATEGORY).drawString(matrix, icon, x + 50f, y + 13.5f, new Color(225, 225, 255, 255).getRGB());
            }
        }

        if (MenuScreen.INSTANCE.getCategory() == ModuleCategory.CONFIGS) {
            if (displayedConfigs.isEmpty()) {
                String message = "Тута пуста :(";
                float textAlpha = 0.7f;
                if (!isDefaultTab && cloudLoading) {
                    long time = System.currentTimeMillis() - cloudLoadStartTime;
                    int dotCount = (int) (time / 500 % 3) + 1;
                    message = "Loading" + ".".repeat(dotCount);
                    textAlpha = loadingAlpha;
                } else if (!cloudLoading) {
                    textAlpha = configsAlpha;
                }
                Fonts.getSize(20, Fonts.Type.DEFAULT).drawString(matrix, message, x + width / 2 - Fonts.getSize(20, Fonts.Type.DEFAULT).getStringWidth(message) / 2 + 10, y + height / 2 + 15, ColorAssist.getText(textAlpha));
            }
        }

        if (MenuScreen.INSTANCE.getCategory() == ModuleCategory.CONFIGS) {
            Fonts.getSize(15, Fonts.Type.DEFAULT).drawString(matrix, point + MenuScreen.INSTANCE.getCategory().getReadableName() + " | Beta", x + 63, y + 13.5f, new Color(245, 245, 255, 255).getRGB());
        } else {
            Fonts.getSize(15, Fonts.Type.DEFAULT).drawString(matrix, point + MenuScreen.INSTANCE.getCategory().getReadableName(), x + 63, y + 13.5f, new Color(245, 245, 255, 255).getRGB());

        }
        Rich.getInstance().getScissorManager().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            editingInput = false;
        }
        if (MenuScreen.INSTANCE.getCategory() == ModuleCategory.CONFIGS && button == 0) {
            if (Calculate.isHovered(mouseX, mouseY, x + 55, y + 38, 35, 15)) {
                isDefaultTab = true;
                loadingAlpha = 0f;
                configsAlpha = 1f;
                refreshConfigs();
                return true;
            }
            if (Calculate.isHovered(mouseX, mouseY, x + 90, y + 38, 35, 15)) {
                isDefaultTab = false;
                loadingAlpha = 0f;
                configsAlpha = 0f;
                refreshConfigs();
                return true;
            }
            if (Calculate.isHovered(mouseX, mouseY, x + 340, y + 38, 80, 15)) {
                editingInput = true;
                inputCursor = configInput.length();
                return true;
            }
            if (Calculate.isHovered(mouseX, mouseY, x + 292, y + 38, 40, 15)) {
                if (isDefaultTab) {
                    createDefaultConfig();
                } else {
                    createCloudConfig();
                }
                refreshConfigs();
                return true;
            }
            if (Calculate.isHovered(mouseX, mouseY, x + 250, y + 38, 38, 15)) {
                clearAllConfigs();
                refreshConfigs();
                return true;
            }
            List<Map<String, Object>> displayedConfigs = isDefaultTab ? configs.stream().filter(m -> ((String)m.get("name")).toLowerCase().contains(configInput.toLowerCase())).collect(Collectors.toList()) : configs;
            float configY = y + 70 + smoothedScroll;
            int index = 0;
            for (Map<String, Object> map : displayedConfigs) {
                String config = (String)map.get("name");
                float configX = x + 55 + (index % 2) * 190;
                if (index % 2 == 0 && index > 0) configY += 55;
                double nameX = configX + 25;
                double nameY = configY + 9;
                double nameWidth = Fonts.getSize(16, Fonts.Type.DEFAULT).getStringWidth(config);
                double nameHeight = 10;
                if (Calculate.isHovered(mouseX, mouseY, nameX, nameY - 2, nameWidth, nameHeight)) {
                    if (isDefaultTab) {
                        editingConfig = config;
                        newName = config;
                        editCursor = newName.length();
                    } else {
                        MinecraftClient.getInstance().keyboard.setClipboard(config);
                    }
                    return true;
                }
                if (Calculate.isHovered(mouseX, mouseY, configX + 162, configY + 35, 14, 15)) {
                    if (isDefaultTab) {
                        try {
                            File dir = new File(Rich.getInstance().getClientInfoProvider().clientDir(), "Custom");
                            File configFile = new File(dir, config + ".json");
                            String json = new String(Files.readAllBytes(configFile.toPath()));
                            File tempFile = new File(Rich.getInstance().getClientInfoProvider().configsDir(), "temp.json");
                            Files.write(tempFile.toPath(), json.getBytes());
                            Rich.getInstance().getFileController().loadFile("temp.json");
                            tempFile.delete();
                        } catch (IOException | FileLoadException e) {
                        }
                    } else {
                        loadCloudConfig(config);
                        isDefaultTab = true;
                        refreshConfigs();
                    }
                    return true;
                }
                if (Calculate.isHovered(mouseX, mouseY, configX + 146, configY + 35, 14, 15)) {
                    if (isDefaultTab) {
                        try {
                            File dir = new File(Rich.getInstance().getClientInfoProvider().clientDir(), "Custom");
                            File configFile = new File(dir, config + ".json");
                            String json = new String(Files.readAllBytes(configFile.toPath()));
                            File tempFile = new File(Rich.getInstance().getClientInfoProvider().configsDir(), "temp.json");
                            Files.write(tempFile.toPath(), json.getBytes());
                            Rich.getInstance().getFileController().loadFile("temp.json");
                            tempFile.delete();
                        } catch (IOException | FileLoadException e) {
                        }
                    } else {
                        loadCloudConfig(config);
                        isDefaultTab = true;
                        refreshConfigs();
                    }
                    return true;
                }
                if (Calculate.isHovered(mouseX, mouseY, configX + 130.25, configY + 35, 14, 15)) {
                    if (isDefaultTab) {
                        String cloudId = (String) map.get("cloud_id");
                        if (cloudId != null) {
                            updateFromCloud(config, cloudId);
                        } else {
                            try {
                                File dir = new File(Rich.getInstance().getClientInfoProvider().clientDir(), "Custom");
                                File configFile = new File(dir, config + ".json");
                                String json = getCurrentConfigJson();
                                Files.write(configFile.toPath(), json.getBytes());
                            } catch (IOException e) {
                            }
                        }
                    } else {
                        saveCloudConfig(config);
                    }
                    refreshConfigs();
                    return true;
                }
                if (Calculate.isHovered(mouseX, mouseY, configX + 114.35, configY + 35, 14, 15)) {
                    if (isDefaultTab) {
                        File dir = new File(Rich.getInstance().getClientInfoProvider().clientDir(), "Custom");
                        File file = new File(dir, config + ".json");
                        file.delete();
                    } else {
                        removeCloudConfig(config);
                    }
                    refreshConfigs();
                    return true;
                }
                index++;
            }
        } else {
            editingConfig = null;
            editingInput = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (MenuScreen.INSTANCE.getCategory() == ModuleCategory.CONFIGS &&
                Calculate.isHovered(mouseX, mouseY, x + 43, y + 65, width - 43 - 15, height - 70)) {
            scroll += amount * 20;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editingInput) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                if (!isDefaultTab) {
                    if (configInput.length() == 8 && configInput.matches("\\d+")) {
                        loadCloudConfig(configInput);
                        isDefaultTab = true;
                    }
                }
                configInput = "";
                inputCursor = 0;
                editingInput = false;
                refreshConfigs();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                configInput = "";
                inputCursor = 0;
                editingInput = false;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (inputCursor > 0) {
                    configInput = configInput.substring(0, inputCursor - 1) + configInput.substring(inputCursor);
                    inputCursor--;
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
                if (inputCursor > 0) {
                    inputCursor--;
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                if (inputCursor < configInput.length()) {
                    inputCursor++;
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                String clip = MinecraftClient.getInstance().keyboard.getClipboard().trim();
                if (!isDefaultTab) {
                    clip = clip.replaceAll("\\D", "");
                    int avail = 8 - configInput.length();
                    if (clip.length() > avail) clip = clip.substring(0, avail);
                } else {
                    int avail = 15 - configInput.length();
                    if (clip.length() > avail) clip = clip.substring(0, avail);
                }
                configInput = configInput.substring(0, inputCursor) + clip + configInput.substring(inputCursor);
                inputCursor += clip.length();
                return true;
            }
        } else if (editingConfig != null) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                if (isDefaultTab) {
                    File dir = new File(Rich.getInstance().getClientInfoProvider().clientDir(), "Custom");
                    File oldFile = new File(dir, editingConfig + ".json");
                    File newFile = new File(dir, newName + ".json");
                    if (!newName.isEmpty() && newName.length() <= 15 && oldFile.exists() && (!newFile.exists() || newName.equals(editingConfig))) {
                        oldFile.renameTo(newFile);
                    }
                }
                editingConfig = null;
                refreshConfigs();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                editingConfig = null;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (editCursor > 0) {
                    newName = newName.substring(0, editCursor - 1) + newName.substring(editCursor);
                    editCursor--;
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
                if (editCursor > 0) {
                    editCursor--;
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                if (editCursor < newName.length()) {
                    editCursor++;
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                String clip = MinecraftClient.getInstance().keyboard.getClipboard().trim();
                int avail = 15 - newName.length();
                if (clip.length() > avail) clip = clip.substring(0, avail);
                newName = newName.substring(0, editCursor) + clip + newName.substring(editCursor);
                editCursor += clip.length();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (editingInput) {
            if (isDefaultTab && configInput.length() < 15 && Character.isLetterOrDigit(chr)) {
                configInput = configInput.substring(0, inputCursor) + chr + configInput.substring(inputCursor);
                inputCursor++;
                return true;
            } else if (!isDefaultTab && configInput.length() < 8 && Character.isDigit(chr)) {
                configInput = configInput.substring(0, inputCursor) + chr + configInput.substring(inputCursor);
                inputCursor++;
                return true;
            }
        } else if (editingConfig != null && newName.length() < 15 && Character.isLetterOrDigit(chr)) {
            newName = newName.substring(0, editCursor) + chr + newName.substring(editCursor);
            editCursor++;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    private void refreshConfigs() {
        if (isDefaultTab) {
            configs = getLocalConfigs();
        } else {
            if (cloudLoading) return;
            cloudLoading = true;
            cloudDataReady = false;
            configs = new ArrayList<>();
            cloudLoadStartTime = System.currentTimeMillis();
            new Thread(() -> {
                List<Map<String, Object>> cl = getCloudConfigs();
                MinecraftClient.getInstance().execute(() -> {
                    tempConfigs = cl;
                    cloudDataReady = true;
                });
            }).start();
        }
    }

    private List<Map<String, Object>> getLocalConfigs() {
        List<Map<String, Object>> localConfigs = new ArrayList<>();
        File dir = new File(Rich.getInstance().getClientInfoProvider().clientDir(), "Custom");
        File[] configFiles = dir.listFiles();
        if (configFiles != null) {
            for (File configFile : configFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".json")) {
                    String configName = configFile.getName().replace(".json", "");
                    long mod = configFile.lastModified();
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", configName);
                    map.put("created", mod);
                    map.put("updated", mod);
                    String jsonContent = "";
                    try {
                        jsonContent = new String(Files.readAllBytes(configFile.toPath()));
                    } catch (IOException e) {
                    }
                    if (!jsonContent.isEmpty()) {
                        Gson gson = new Gson();
                        Map<String, Object> configData = gson.fromJson(jsonContent, Map.class);
                        String cloudId = (String) configData.get("cloud_id");
                        if (cloudId != null) {
                            map.put("cloud_id", cloudId);
                            Map<String, Object> metadata = getCloudMetadata(cloudId);
                            if (metadata != null) {
                                long serverUpdated = ((Number) metadata.get("updated")).longValue();
                                if (serverUpdated > mod) {
                                    map.put("has_update", true);
                                }
                                map.put("owner", metadata.get("owner"));
                                map.put("avatar_hash", metadata.get("avatar_hash"));
                            }
                        }
                    }
                    localConfigs.add(map);
                }
            }
        }
        return localConfigs;
    }

    private List<Map<String, Object>> getCloudConfigs() {
        try {
            if (!Rich.getInstance().getCloudConfigClient().isConnected()) {
                return new ArrayList<>();
            }

            Gson gson = new Gson();
            Map<String, Object> request = new HashMap<>();
            request.put("command", "list");
            request.put("username", UserProfile.getInstance().profile("username"));
            request.put("uuid", UserProfile.getInstance().profile("uid"));
            String message = gson.toJson(request);
            String response = Rich.getInstance().getCloudConfigClient().sendAndWaitForResponse(message);
            if (response == null) return new ArrayList<>();
            Map<String, Object> respMap = gson.fromJson(response, Map.class);
            if ((Boolean) respMap.get("success")) {
                return (List<Map<String, Object>>) respMap.get("data");
            }
        } catch (Exception e) {
            System.err.println("Failed to get cloud configs: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private void createDefaultConfig() {
        Random random = new Random();
        File dir = new File(Rich.getInstance().getClientInfoProvider().clientDir(), "Custom");
        String name;
        do {
            name = "RichConfig" + String.format("%03d", random.nextInt(1000));
        } while (new File(dir, name + ".json").exists());
        try {
            File configFile = new File(dir, name + ".json");
            String json = getCurrentConfigJson();
            Files.write(configFile.toPath(), json.getBytes());
        } catch (IOException e) {
        }
    }

    private void createCloudConfig() {
        Random random = new Random();
        String id;
        do {
            id = String.format("%08d", random.nextInt(100000000));
        } while (getCloudConfigJson(id) != null);
        saveCloudConfig(id);
    }

    private void clearAllConfigs() {
        if (isDefaultTab) {
            File dir = new File(Rich.getInstance().getClientInfoProvider().clientDir(), "Custom");
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".json")) {
                        f.delete();
                    }
                }
            }
        } else {
            for (Map<String, Object> map : configs) {
                removeCloudConfig((String) map.get("name"));
            }
        }
    }

    private String getCurrentConfigJson() {
        String json = "";
        File dir = new File(Rich.getInstance().getClientInfoProvider().clientDir(), "Custom");
        File temp = new File(dir, "temp.json");
        try {
            Rich.getInstance().getFileController().saveFile("temp.json");
            json = new String(Files.readAllBytes(temp.toPath()));
        } catch (FileSaveException | IOException e) {
        } finally {
            temp.delete();
        }
        return json;
    }

    private String getCloudConfigJson(String name) {
        try {
            if (!Rich.getInstance().getCloudConfigClient().isConnected()) {
                return null;
            }

            Gson gson = new Gson();
            Map<String, Object> request = new HashMap<>();
            request.put("command", "load");
            request.put("username", UserProfile.getInstance().profile("username"));
            request.put("uuid", UserProfile.getInstance().profile("uid"));
            request.put("configName", name);
            String message = gson.toJson(request);
            String response = Rich.getInstance().getCloudConfigClient().sendAndWaitForResponse(message);
            if (response == null) return null;
            Map<String, Object> respMap = gson.fromJson(response, Map.class);
            if ((Boolean) respMap.get("success")) {
                Object data = respMap.get("data");
                return gson.toJson(data);
            }
        } catch (Exception e) {
            System.err.println("Failed to get cloud config: " + e.getMessage());
        }
        return null;
    }

    private Map<String, Object> getCloudMetadata(String name) {
        try {
            if (!Rich.getInstance().getCloudConfigClient().isConnected()) {
                return null;
            }

            Gson gson = new Gson();
            Map<String, Object> request = new HashMap<>();
            request.put("command", "metadata");
            request.put("username", UserProfile.getInstance().profile("username"));
            request.put("uuid", UserProfile.getInstance().profile("uid"));
            request.put("configName", name);
            String message = gson.toJson(request);
            String response = Rich.getInstance().getCloudConfigClient().sendAndWaitForResponse(message);
            if (response == null) return null;
            Map<String, Object> respMap = gson.fromJson(response, Map.class);
            if ((Boolean) respMap.get("success")) {
                return (Map<String, Object>) respMap.get("data");
            }
        } catch (Exception e) {
            System.err.println("Failed to get cloud metadata: " + e.getMessage());
        }
        return null;
    }

    private void saveCloudConfig(String name) {
        String json = getCurrentConfigJson();
        if (json.isEmpty()) return;
        Gson gson = new Gson();
        Map<String, Object> data = new LinkedHashMap<>();
        long now = System.currentTimeMillis();
        String existing = getCloudConfigJson(name);
        long created = now;
        if (existing != null) {
            Map<String, Object> oldData = gson.fromJson(existing, Map.class);
            created = ((Number) oldData.get("created")).longValue();
        }
        data.put("owner", UserProfile.getInstance().profile("username"));
        data.put("created", created);
        data.put("updated", now);
        data.put("avatar_hash", Rich.getInstance().getDiscordManager().getAvatarId().toString());
        Map<String, Object> configData = gson.fromJson(json, Map.class);
        data.putAll(configData);
        json = gson.toJson(data);
        saveCloudConfigWithJson(name, json);
    }

    private void saveCloudConfigWithJson(String name, String json) {
        if (json.isEmpty()) return;
        try {
            if (!Rich.getInstance().getCloudConfigClient().isConnected()) {
                System.err.println("Cannot save: WebSocket not connected");
                return;
            }

            Gson gson = new Gson();
            Map<String, Object> request = new HashMap<>();
            request.put("command", "save");
            request.put("username", UserProfile.getInstance().profile("username"));
            request.put("uuid", UserProfile.getInstance().profile("uid"));
            request.put("configName", name);
            request.put("configData", gson.fromJson(json, Map.class));
            String message = gson.toJson(request);
            Rich.getInstance().getCloudConfigClient().sendAndWaitForResponse(message);
        } catch (Exception e) {
            System.err.println("Failed to save cloud config: " + e.getMessage());
        }
    }

    private void loadCloudConfig(String name) {
        String json = getCloudConfigJson(name);
        if (json == null) return;
        Gson gson = new Gson();
        Map<String, Object> data = gson.fromJson(json, Map.class);
        String owner = (String) data.get("owner");
        long created = ((Number) data.get("created")).longValue();
        long updated = ((Number) data.get("updated")).longValue();
        Object avatarObj = data.get("avatar_hash");
        String avatarHash;
        if (avatarObj instanceof Map) {
            Map<String, String> idMap = (Map<String, String>) avatarObj;
            avatarHash = idMap.get("namespace") + ":" + idMap.get("path");
        } else {
            avatarHash = String.valueOf(avatarObj);
        }
        data.remove("owner");
        data.remove("created");
        data.remove("updated");
        data.remove("avatar_hash");
        data.put("cloud_id", name);
        json = gson.toJson(data);
        File dir = new File(Rich.getInstance().getClientInfoProvider().clientDir(), "Custom");
        File temp = new File(dir, "temp.json");
        try {
            Files.write(temp.toPath(), json.getBytes());
            Rich.getInstance().getFileController().loadFile("temp.json");
        } catch (IOException | FileLoadException e) {
        } finally {
            temp.delete();
        }
        String baseName = owner + " Config";
        String localName = baseName;
        if (!owner.equals(UserProfile.getInstance().profile("username"))) {
            int num = 1;
            while (new File(dir, localName + ".json").exists()) {
                localName = baseName + " (" + num++ + ")";
            }
        } else {
            localName = "Cloud_" + name;
        }
        try {
            Files.write(new File(dir, localName + ".json").toPath(), json.getBytes());
        } catch (IOException e) {
        }
    }

    private void updateFromCloud(String localName, String cloudId) {
        String json = getCloudConfigJson(cloudId);
        if (json == null) return;
        Gson gson = new Gson();
        Map<String, Object> data = gson.fromJson(json, Map.class);
        data.remove("owner");
        data.remove("created");
        data.remove("updated");
        data.remove("avatar_hash");
        data.put("cloud_id", cloudId);
        json = gson.toJson(data);
        File dir = new File(Rich.getInstance().getClientInfoProvider().clientDir(), "Custom");
        File temp = new File(dir, "temp.json");
        try {
            Files.write(temp.toPath(), json.getBytes());
            Rich.getInstance().getFileController().loadFile("temp.json");
            Files.write(new File(dir, localName + ".json").toPath(), json.getBytes());
        } catch (IOException | FileLoadException e) {
        } finally {
            temp.delete();
        }
    }

    private void removeCloudConfig(String name) {
        try {
            if (!Rich.getInstance().getCloudConfigClient().isConnected()) {
                System.err.println("Cannot remove: WebSocket not connected");
                return;
            }

            Gson gson = new Gson();
            Map<String, Object> request = new HashMap<>();
            request.put("command", "remove");
            request.put("username", UserProfile.getInstance().profile("username"));
            request.put("uuid", UserProfile.getInstance().profile("uid"));
            request.put("configName", name);
            String message = gson.toJson(request);
            Rich.getInstance().getCloudConfigClient().sendAndWaitForResponse(message);
        } catch (Exception e) {
            System.err.println("Failed to remove cloud config: " + e.getMessage());
        }
    }
}