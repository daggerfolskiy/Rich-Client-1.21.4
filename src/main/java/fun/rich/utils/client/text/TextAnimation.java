package fun.rich.utils.client.text;

import java.util.Arrays;
import java.util.List;
import antidaunleak.api.UserProfile;

public class TextAnimation {
    private final List<String> messages;
    private String currentText = "";
    private int currentMessageIndex = 0;
    private int animationTick = 0;
    private boolean isRemoving = false;
    private boolean showUnderscore = true;
    private int underscoreTick = 0;
    private final int delayTicks = 2;
    private final int pauseTicksMax = 60;
    private int pauseTicks = 0;
    private final int underscoreBlinkTicks = 10;

    public TextAnimation() {
        String username = UserProfile.getInstance().profile("username");
        messages = Arrays.asList(
                "Glad to see you again, " + username + "! Come on in, everything’s ready for you",
                "Welcome back, " + username + "! Ready to dive into the adventure?",
                "Hey " + username + ", let’s make some epic moments today!",
                username + ", the game awaits your legendary skills!"
        );
    }

    public void updateText() {
        if (pauseTicks > 0) {
            pauseTicks--;
            updateUnderscore();
            return;
        }

        if (animationTick >= delayTicks) {
            String fullText = messages.get(currentMessageIndex);
            if (isRemoving) {
                if (currentText.length() > 0) {
                    currentText = currentText.substring(0, currentText.length() - 1);
                } else {
                    isRemoving = false;
                    currentMessageIndex = (currentMessageIndex + 1) % messages.size();
                    pauseTicks = pauseTicksMax;
                }
            } else {
                if (currentText.length() < fullText.length()) {
                    currentText = fullText.substring(0, currentText.length() + 1);
                } else {
                    isRemoving = true;
                    pauseTicks = pauseTicksMax;
                }
            }
            animationTick = 0;
        }
        animationTick++;
        updateUnderscore();
    }

    private void updateUnderscore() {
        underscoreTick++;
        if (underscoreTick >= underscoreBlinkTicks) {
            showUnderscore = !showUnderscore;
            underscoreTick = 0;
        }
    }

    public String getCurrentText() {
        return currentText + (showUnderscore ? "_" : "");
    }
}