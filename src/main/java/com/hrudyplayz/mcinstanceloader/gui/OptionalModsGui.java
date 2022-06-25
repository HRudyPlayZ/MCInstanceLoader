package com.hrudyplayz.mcinstanceloader.gui;

import java.io.File;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import com.hrudyplayz.mcinstanceloader.Config;
import com.hrudyplayz.mcinstanceloader.Main;
import com.hrudyplayz.mcinstanceloader.ModProperties;
import com.hrudyplayz.mcinstanceloader.resources.OptionalResourcesHandler;
import com.hrudyplayz.mcinstanceloader.resources.PackConfigParser;

@SuppressWarnings("unused")
public class OptionalModsGui extends GuiScreen {
// This class defines the optional mods GUI, in order for it to get displayed.

	// The logo file, displayed on the top.
	public static ResourceLocation logo = new ResourceLocation(ModProperties.MODID, "logo.png");
	private static final int logoWidth = 128, logoHeight = 64;

	// Behavior variables
	public static int currentMenu = 0;
	public static int currentPage = 0;
	public static OptionalResourcesHandler[] optionalList;
	public static ArrayList<ArrayList<OptionalResourcesHandler.MenuOption>> pageList = new ArrayList<>();
	public static ArrayList<OptionalResourcesHandler.MenuOption> checkedOptions = new ArrayList<>();

	// Button sizes.
	private static final int checkboxWidth = 20;
	private static final int arrowButtonWidth = 50;
	private static final int buttonWidth = 150;

	// Margins
	private static final int logoYPosition = -5;
	private static final int spaceBetweenTextAndLogo = -7;
	private static final int spaceBetweenHeaderAndContent = 15;
	private static final int spaceBetweenOptions = 45;
	private static int buttonHeight = 0;
	private static final int horizontalMargin = 20;


	// Defines the list of texts to display, and the font size.
	public static String title = "";
	public static ArrayList<String> titleList = new ArrayList<>();
	public static ArrayList<String> descriptionList = new ArrayList<>();
	public static final int amountOfItemsPerPage = 3;
	private static final int fontHeight = 8;

	public static boolean runOnceDone = false; // Only runs the runOnce function once.
	public static boolean cantClick = false; // Prevents double clicking on buttons.

	public GuiScreen parentGuiScreen;

	// Class constructor, used by the GuiOpenEventHandler class.
	public OptionalModsGui(GuiScreen parentGuiScreen) {
		this.parentGuiScreen = parentGuiScreen;
	}


	@Override
	public void initGui() {
	// Initializes the GUI and buttons and handles the close countdown.

		Keyboard.enableRepeatEvents(true);
		buttonHeight = this.height - 27;
		runOnce();
		refreshGui();
	}

	public void runOnce() {
	// This method is only ran once after the GUI is loaded.

		if (runOnceDone) return;

		optionalList = PackConfigParser.parseMenus(Config.configFolder + "temp" + File.separator + "optionals.packconfig");
		updateChecked();

		runOnceDone = true;
	}

	private void updateChecked() {
	// Updates the checked options list whenever the menu changed.

		if (currentMenu > optionalList.length - 1) return;

		checkedOptions = new ArrayList<>();

		for (OptionalResourcesHandler.MenuOption o : optionalList[currentMenu].list) {
			if (o.isDefault) toggleChecked(o);
		}
	}

	private void toggleChecked(OptionalResourcesHandler.MenuOption option) {
	// Switches (adds / removes) an option to the checked options list.

		if (!checkedOptions.contains(option)) {
			if (checkedOptions.size() > 0 && optionalList[currentMenu].maximumCheckedAmount > 0 &&
			    optionalList[currentMenu].maximumCheckedAmount >= optionalList[currentMenu].minimumCheckedAmount &&
			    checkedOptions.size() + 1 > optionalList[currentMenu].maximumCheckedAmount) checkedOptions.remove(0);

			checkedOptions.add(option);
		}
		else checkedOptions.remove(option);
	}


	private void postMenu() {
	// Does the final steps and then displays the final gui screen

		Main.thirdPhase();
		this.mc.displayGuiScreen(new InfoGui(this.parentGuiScreen));
	}


	private void refreshGui() {
	// Updates the screen elements based on the current menu and page.

		// Clears every element list of the GUI.
		this.buttonList.clear();
		titleList.clear();
		descriptionList.clear();

		// Continues to the next steps when we leave the final menu.
		if (currentMenu >= optionalList.length) {
			postMenu();
			return;
		}

		// List of the option elements splited by the defined amount of items per page.
		pageList = OptionalResourcesHandler.splitList(amountOfItemsPerPage, optionalList[currentMenu].list);

		// If there is no options at all, skips to the next menu.
		if (pageList.size() == 0) {
			currentMenu += 1;
			currentPage = 0;
			updateChecked();
			refreshGui();
			return;
		}

		// Sets the displayed menu title to the current menu's one.
		title = optionalList[currentMenu].title;

		// Adds the option elements (title, description, button) according to the amount of elements in the current page.
		for (int i = 0; i < pageList.get(currentPage).size(); i += 1) {

			// If the option is checked, we change the title color and add the "X" text to the button.
			String checkmark = "";
			String titleColor = "";
			if (checkedOptions.contains(pageList.get(currentPage).get(i))) {
				checkmark = "X";
				titleColor = "" + EnumChatFormatting.DARK_GREEN;
			}

			// Creates the button
			createButton(i, horizontalMargin, logoYPosition + logoHeight + spaceBetweenTextAndLogo + spaceBetweenHeaderAndContent + spaceBetweenOptions * i, checkboxWidth, checkmark);

			// Creates the title and descriptions
			titleList.add(titleColor + pageList.get(currentPage).get(i).name);
			descriptionList.add(EnumChatFormatting.GRAY + pageList.get(currentPage).get(i).description);
		}

		// x values for the bottom button, varies based on the amount of pages.
		int nextButtonX = horizontalMargin;
		int leftArrowX = horizontalMargin;
		int rightArrowX = horizontalMargin + arrowButtonWidth + 5;

		// If there is more than 2 pages, we add the first and final page buttons ("<<" and ">>").
		if (pageList.size() > 2) {
			nextButtonX = horizontalMargin + 4 * arrowButtonWidth + 20;
			leftArrowX = horizontalMargin + arrowButtonWidth + 5;
			rightArrowX = horizontalMargin + 2 * arrowButtonWidth + 10;

			if (currentPage > 0) createButton(10, horizontalMargin, buttonHeight, arrowButtonWidth, "<<");
			else createButton(10, horizontalMargin, buttonHeight, arrowButtonWidth, EnumChatFormatting.GRAY + "<<");

			if (currentPage < pageList.size() - 1) createButton(13, horizontalMargin + 3 * arrowButtonWidth + 15, buttonHeight, arrowButtonWidth, ">>");
			else createButton(13, horizontalMargin + 3 * arrowButtonWidth + 15, buttonHeight, arrowButtonWidth, EnumChatFormatting.GRAY + ">>");
		}

		// If there is more than one page, we add the previous and next page buttons ("<" and ">").
		if (pageList.size() > 1) {
			if (pageList.size() == 2) nextButtonX = horizontalMargin + 2 * arrowButtonWidth + 10;

			if (currentPage > 0) createButton(11, leftArrowX, buttonHeight, arrowButtonWidth, "<");
			else createButton(11, leftArrowX, buttonHeight, arrowButtonWidth, EnumChatFormatting.GRAY + "<");

			if (currentPage < pageList.size() - 1) createButton(12, rightArrowX, buttonHeight, arrowButtonWidth, ">");
			else createButton(12, rightArrowX, buttonHeight, arrowButtonWidth, EnumChatFormatting.GRAY + ">");
		}

		// Adds the "Confirm and continue" button, with the appropriate color.
		String color = "";
		if (!(checkedOptions.size() >= optionalList[currentMenu].minimumCheckedAmount)) color = "" + EnumChatFormatting.GRAY;
		createButton(14, nextButtonX, buttonHeight, buttonWidth, color + I18n.format("gui.mcinstanceloader.confirm"));
	}

	@Override
	public void onGuiClosed() {
	// Handles what should happen after the GUI gets closed.

		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void handleMouseInput() {
	// Allows for the scroll wheel to scroll through the pages.

		super.handleMouseInput();
		int mWheel = Mouse.getEventDWheel();

		// Scrolling down goes to the next page
		if (mWheel < -1 && currentPage < pageList.size() - 1) {
			currentPage += 1;
			refreshGui();
		}

		// Scrolling up goes to the previous one
		if (mWheel > 1 && currentPage > 0) {
			currentPage -= 1;
			refreshGui();
		}
	}

	@Override
	protected void keyTyped(char c, int keyCode) {
	// Allows for the left/right and page up/ page down keyboard keys to scroll through the pages.
	// Also disables the default behavior of the escape key going back to the main menu.

		if ((keyCode == 205 || keyCode == 209) && currentPage < pageList.size() - 1) {
			currentPage += 1;
			refreshGui();
		}

		if ((keyCode == 203 || keyCode == 201) && currentPage > 0) {
			currentPage -= 1;
			refreshGui();
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
	// Defines the behavior for every button.

		if (cantClick) return;

		if (button.enabled) {
			if (button.id < 10) {
				cantClick = true;
				toggleChecked(pageList.get(currentPage).get(button.id));
				refreshGui();
			}

			if (button.id == 10 && currentPage > 0) {
				cantClick = true;
				currentPage = 0;
				refreshGui();
			}

			if (button.id == 11 && currentPage > 0) {
				cantClick = true;
				currentPage -= 1;
				refreshGui();
			}

			if (button.id == 12 && currentPage < pageList.size() - 1) {
				cantClick = true;
				currentPage += 1;
				refreshGui();
			}

			if (button.id == 13 && currentPage < pageList.size() - 1) {
				cantClick = true;
				currentPage = pageList.size() - 1;
				refreshGui();
			}

			if (button.id == 14 && checkedOptions.size() >= optionalList[currentMenu].minimumCheckedAmount) {
				for (OptionalResourcesHandler.MenuOption o : checkedOptions) o.download();

				currentMenu += 1;
				currentPage = 0;
				updateChecked();
				refreshGui();
			}
		}
	}


	@Override
	public void drawScreen(int x, int y, float renderPartialTicks) {
	// Draws the GUI on the screen.

		cantClick = false;

		this.drawDefaultBackground(); // Dirt background.


		this.drawCenteredString(title, this.width / 2, logoYPosition + logoHeight + spaceBetweenTextAndLogo, 0xFFFFFF); // Menu title

		// Option titles
		for (int i = 0; i < titleList.size(); i += 1) {
			this.fontRendererObj.drawStringWithShadow(titleList.get(i), horizontalMargin + 25, logoYPosition + logoHeight + spaceBetweenTextAndLogo + spaceBetweenHeaderAndContent + spaceBetweenOptions * i + 7, 0xFFFFFF);
		}

		// Option descriptions
		for (int i = 0; i < descriptionList.size(); i += 1) {
			this.fontRendererObj.drawStringWithShadow(descriptionList.get(i), horizontalMargin, logoYPosition + logoHeight + spaceBetweenTextAndLogo + spaceBetweenHeaderAndContent + spaceBetweenOptions * i + 25, 0xFFFFFF);
		}

		// Page number.
		if (pageList.size() > 2) drawCenteredString("Page " + (currentPage + 1) + " out of " + pageList.size(), horizontalMargin + arrowButtonWidth + 55, buttonHeight - 12, 0xFFFFFF);
		else if (pageList.size() > 1) drawCenteredString("Page " + (currentPage + 1) + " out of " + pageList.size(), horizontalMargin + 50, buttonHeight - 12, 0xFFFFFF);

		// Logo
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		this.mc.getTextureManager().bindTexture(logo); // Binds the logo texture, so it gets displayed by the drawTexturedModalRect function.

		this.drawTexturedModalRect(this.width / 2 - logoWidth / 2, logoYPosition, logoWidth, logoHeight);

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
