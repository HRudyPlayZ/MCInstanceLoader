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

import com.hrudyplayz.mcinstanceloader.Main;
import com.hrudyplayz.mcinstanceloader.ModProperties;
import com.hrudyplayz.mcinstanceloader.utils.FileHelper;
import com.hrudyplayz.mcinstanceloader.utils.WebHelper;


@SuppressWarnings("unused")
public class InfoGui extends GuiScreen {
// This class defines the results GUI, in order for it to get displayed.

    // The logo file, displayed on the top.
    public static ResourceLocation logo = new ResourceLocation(ModProperties.MODID, "logo.png");
    private static final int logoWidth = 128, logoHeight = 64;

    // Defines the button size and amount (1 or 2, public variable used as an interface).
    private static final int buttonWidth = 200;
    public static int buttonAmount = 2;

    // Margins
    private static final int logoYPosition = 15;
    private static final int spaceBetweenHeaderAndContent = 10;
    private static final int spaceBetweenLines = 10;
    private static final int spaceBetweenContentAndButtons = 20;

    // Defines the list of texts to display, and the font size.
    public static ArrayList<String> textList = new ArrayList<>();
    private static final int fontHeight = 8;

    public static int closeDelay = Config.closeGameTimer; // Initiliazes the countdown to the value from config.
    public static boolean doNotLaunchMoreTimers = false; //Somehow initGui is getting called twice so this is a workaround.

    // Defines the buttons to display on the GUI
    public GuiButton firstButton;
    public GuiButton secondButton;

    // The parent gui screen to redirect to when pressing escape.
    public GuiScreen parentGuiScreen;

    // Class constructor, used by the GuiOpenEventHandler class.
    public InfoGui(GuiScreen parentGuiScreen) {
        this.parentGuiScreen = parentGuiScreen;
    }


    @Override
    public void initGui() {
    // Initializes the GUI and buttons and handles the close countdown.

        Keyboard.enableRepeatEvents(true);

        int buttonYpos = logoYPosition + logoHeight + spaceBetweenHeaderAndContent + textList.size() * (spaceBetweenLines + fontHeight) + spaceBetweenContentAndButtons;

        // Initializes the screen buttons depending on the selected button amount.
        this.buttonList.clear();
        String text;
        String text2;
        switch (buttonAmount) {
            case 1:
                text = I18n.format("menu.quit");
                if (Main.hasUpdate) text = "Install update";

                firstButton = createButton(0, this.width / 2 - (buttonWidth / 2), buttonYpos, buttonWidth, text);
                break;

            case 2:
                text = I18n.format("menu.quit");
                if (Main.hasUpdate) text = I18n.format("gui.mcinstanceloader.installupdate");

                text2 = I18n.format("gui.mcinstanceloader.openlogfile");
                if (Main.hasUpdate) text2 = I18n.format("gui.mcinstanceloader.continue");

                firstButton = createButton(0, this.width / 2 - (buttonWidth + 2), buttonYpos, buttonWidth, text);
                secondButton = createButton(1, this.width / 2 + 2, buttonYpos, buttonWidth, text2);
                break;
        }

        // Handles the timer. Creates it if it doesn't exist and sets its task to update the quit game button's text and to close the game at the end.
        if (closeDelay > 0 && !doNotLaunchMoreTimers && !Main.hasUpdate) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    firstButton.displayString = I18n.format("menu.quit") + EnumChatFormatting.GOLD + " (" + closeDelay + ")";
                    if (closeDelay <= 0) mc.shutdown();
                    closeDelay -= 1;
                    doNotLaunchMoreTimers = true;
                }
            }, 0, 1000);
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
                case 0: // First button (Quit game, update)
                    if (Main.hasUpdate) {
                        Main.hasUpdate = false;

                        if (!WebHelper.downloadFile(Main.updateUrl, "mods" + File.separator + Main.updateFileName)) {
                            textList.clear();
                            this.buttonList.clear();
                            Main.throwError("Failed to download the updated file");
                            return;
                        }

                        String[] list = FileHelper.listDirectory("mods", false);
                        String file = "";
                        for (String s : list) {
                            if (s.contains("mcinstanceloader-" + ModProperties.VERSION + ".jar")) {
                                file = s;
                                break;
                            }
                        }

                        if (file.length() > 0) FileHelper.overwriteFile("mods" + File.separator + file, new String[0]);
                    }
                    
                    this.mc.shutdown();
                    break;

                case 1: // Second button (Open logs, continue anyways)
                    if (Main.hasUpdate) {
                        // Sets the has update variable to false.
                        Main.hasUpdate = false;

                        // Clears the current GUI
                        this.buttonList.clear();
                        textList.clear();

                        // Runs the second phase again. (for real this time).
                        Main.secondPhase();

                        // Switches the GUI to the OptionalModsGui
                        this.mc.displayGuiScreen(new OptionalModsGui(this.parentGuiScreen));
                    }
                    else {
                        try {
                            Desktop.getDesktop().open(new File(Config.configFolder + "details.log"));
                        }
                        catch (IOException ignored) {}
                    }

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
            this.drawCenteredString(textList.get(i), this.width / 2, logoYPosition + logoHeight + spaceBetweenHeaderAndContent + i * (spaceBetweenLines + fontHeight), 0xFFFFFF);
        }

        // Logo
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager().bindTexture(logo); // Binds the logo texture, so it gets displayed by the drawTexturedModalRect function.

        this.drawTexturedModalRect(this.width / 2 - logoWidth / 2, logoYPosition, logoWidth, logoHeight);

        GL11.glDisable(GL11.GL_BLEND);
        super.drawScreen(x, y, renderPartialTicks);
    }


    public GuiButton createButton(int id, int x, int y, int width, String text) {
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
