package net.dravigen.tesseractUtils.GUI;

import net.dravigen.tesseractUtils.utils.GuiUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.GuiButton;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ResourceLocation;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class GuiSliderConfig extends GuiButton {

    private static final ResourceLocation resourceLocation = new ResourceLocation("tesseract_utils:textures/gui/button.png");

    public float sliderValue;
    public boolean dragging = false;
    private final String id;

    public GuiSliderConfig(int id, int posX, int posY, int width, int height, String stringId, String string, float value) {
        super(id, posX, posY, width, height, string);
        this.id = stringId;
        this.sliderValue = value;
    }

    protected int getHoverState(boolean bl) { return 0; }

    protected void mouseDragged(Minecraft minecraft, int i, int j) {
        minecraft.getTextureManager().bindTexture(resourceLocation);

        if (this.drawButton) {
            if (this.dragging) {
                this.sliderValue = (float) (i - (this.xPosition + 4)) / (float) (this.width - 8);

                if (this.sliderValue < 0.0F) {
                    this.sliderValue = 0.0F;
                }

                if (this.sliderValue > 1.0F) {
                    this.sliderValue = 1.0F;
                }

                GuiUtils gu = GuiUtils.getInstance();
                gu.setSliderConfig(this.id, this.sliderValue);
                this.displayString = gu.getSliderDisplay(this.id);
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 0, this.width / 2, this.height);
            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 0, this.width / 2, this.height);

            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)), this.yPosition, 0, 0, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, 200- 4 , 0, 4, 20);
        }
    }

    public boolean mousePressed(Minecraft minecraft, int i, int j) {
        if (super.mousePressed(minecraft, i, j)) {
            this.sliderValue = (float)(i - (this.xPosition + 4)) / (float)(this.width - 8);
            if (this.sliderValue < 0.0F) {
                this.sliderValue = 0.0F;
            }

            if (this.sliderValue > 1.0F) {
                this.sliderValue = 1.0F;
            }

            GuiUtils gu = GuiUtils.getInstance();
            gu.setSliderConfig(this.id, this.sliderValue);
            this.displayString = gu.getSliderDisplay(this.id);
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    public void mouseReleased(int i, int j) {
        this.dragging = false;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof GuiConfigSettingsScreen configSettingsScreen){
            mc.displayGuiScreen(new GuiConfigSettingsScreen(configSettingsScreen.parentScreen));
        } else if (mc.currentScreen instanceof GuiShapeMenuScreen shapeMenu) {
            mc.displayGuiScreen(new GuiShapeMenuScreen(shapeMenu.parentScreen));
        }
    }
}