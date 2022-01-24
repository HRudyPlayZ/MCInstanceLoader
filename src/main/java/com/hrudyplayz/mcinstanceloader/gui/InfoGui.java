package com.hrudyplayz.mcinstanceloader.gui;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.util.EnumChatFormatting;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import com.hrudyplayz.mcinstanceloader.Config;

import com.hrudyplayz.mcinstanceloader.ModProperties;

public class InfoGui extends GuiScreen {
// This class defines the results GUI, in order for it to get displayed.

    private final GuiScreen parentGuiScreen;

    // The logo file, displayed on the top.
    public static ResourceLocation logo = new ResourceLocation(ModProperties.MODID, "logo.png");
    private static final int logoWidth = 128, logoHeight = 64;

    // Defines the button size, amount (1 or 2, public variable used as an interface), and the vertical space between them.
    private static final int buttonWidth = 200;
    public static int buttonAmount = 2;
    private static final int verticalMargin = 35;

    // Defines the list of texts to display, and the font size.
    public static ArrayList<String> textList = new ArrayList<>();
    private static final int fontHeight = 8;

    public static int closeDelay = Config.closeGameTimer; // Initiliazes the countdown to the value from config.
    public static boolean doNotLaunchMoreTimers = false; //Somehow initGui is getting called twice so this is a workaround.

    // Defines the buttons to display on the GUI
    public GuiButton quitButton;
    public GuiButton openLogButton;

    // Class constructor, used by the GuiOpenEventHandler class.
    public InfoGui(GuiScreen parentGuiScreen) {
        this.parentGuiScreen = parentGuiScreen;
    }


    @Override
    public void initGui() {
    // Initializes the GUI and buttons and handles the close countdown.

        Keyboard.enableRepeatEvents(true);

        int buttonYpos = logoHeight + textList.size() * 12 + textList.size() * fontHeight + 2 * verticalMargin;

        // Initializes the screen buttons depending on the selected button amount.
        this.buttonList.clear();
        switch (buttonAmount) {
            case 1:
                quitButton = createButton(0, this.width / 2 - (buttonWidth / 2), buttonYpos, buttonWidth,  I18n.format("menu.quit"));
                break;
            case 2:
                quitButton = createButton(0, this.width / 2 - (buttonWidth + 2), buttonYpos, buttonWidth,  I18n.format("menu.quit"));
                openLogButton = createButton(1, this.width / 2 + 2, buttonYpos, buttonWidth, I18n.format("gui.mcinstanceloader.openlogfile"));
                break;
        }

        // Handles the timer. Creates it if it doesn't exist and sets its task to update the quit game button's text and to close the game at the end.
        if (closeDelay > 0) {
            if (!doNotLaunchMoreTimers) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        quitButton.displayString = I18n.format("menu.quit") + EnumChatFormatting.GOLD + " (" + closeDelay + ")";
                        if (closeDelay <= 0) mc.shutdown();
                        closeDelay -= 1;
                        doNotLaunchMoreTimers = true;
                    }
                }, 0, 1000);
            }
        }

    }


    @Override
    public void onGuiClosed() {
    // Handles what should happen after the GUI gets closed.

        Keyboard.enableRepeatEvents(false);
    }


    @Override
    protected void actionPerformed(GuiButton button) {
    // Defines the behavior for every button.

        if (button.enabled) {
            switch (button.id) {
                case 0: // Quit game button
                    this.mc.shutdown();
                    break;

                case 1: // Open logs button
                    try {
                        Desktop.getDesktop().open(new File(Config.configFolder + "details.log"));
                    }
                    catch (IOException ignored) {}

                    break;
            }
        }
    }


    @Override
    public void drawScreen(int x, int y, float renderPartialTicks) {
    // Draws the GUI on the screen.


        this.drawDefaultBackground(); // Dirt background.

        // Texts
        for (int i = 0; i < textList.size(); i += 1) {
            this.drawCenteredString(textList.get(i), this.width / 2, verticalMargin + logoHeight + (i + 1) * 12 + (i + 1) * fontHeight, 0xFFFFFF);
        }

        // Logo
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager().bindTexture(logo); // Binds the logo texture, so it gets displayed by the drawTexturedModalRect function.

        this.drawTexturedModalRect(this.width / 2 - logoWidth / 2, verticalMargin, logoWidth, logoHeight);

        GL11.glDisable(GL11.GL_BLEND);
        super.drawScreen(x, y, renderPartialTicks);
    }


    public GuiButton createButton (int id, int x, int y, int width, String text) {
    // Helper function to create buttons more easily, and return them to modify them afterwards.

        GuiButton result = new GuiButton(id, x, y, width, 20, text);
        this.buttonList.add(result);
        return result;
    }


    public void drawCenteredString(String string, int x, int y, int color) {
    // Helper function to draw a centered string.

        this.fontRendererObj.drawStringWithShadow(string, x - this.fontRendererObj.getStringWidth(string.replaceAll("\\P{InBasic_Latin}", "")) / 2, y, color);
    }


    public void drawTexturedModalRect(int x, int y, int width, int height) {
    // Helper function to draw a scaled picture at certain coordinates.

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, 0.0, 1.0);
        tessellator.addVertexWithUV(x + width, y + height, 0, 1.0, 1.0);
        tessellator.addVertexWithUV(x + width, y, 0, 1.0, 0.0);
        tessellator.addVertexWithUV(x, y, 0, 0.0, 0.0);
        tessellator.draw();
    }

}
