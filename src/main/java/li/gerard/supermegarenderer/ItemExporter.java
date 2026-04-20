package li.gerard.supermegarenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

@SideOnly(Side.CLIENT)
public class ItemExporter {

    public static int export(int size) {
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem renderItem = mc.getRenderItem();

        File outputDir = new File(mc.gameDir, "item_renders");
        outputDir.mkdirs();

        Framebuffer fb = new Framebuffer(size, size, true);
        int count = 0;

        try {
            for (Item item : Item.REGISTRY) {
                if (item == null) continue;

                ResourceLocation reg = item.getRegistryName();
                if (reg == null) continue;

                NonNullList<ItemStack> variants = NonNullList.create();
                try {
                    item.getSubItems(CreativeTabs.SEARCH, variants);
                } catch (Exception ignored) {}

                if (variants.isEmpty()) {
                    variants.add(new ItemStack(item));
                }

                for (ItemStack stack : variants) {
                    if (stack.isEmpty()) continue;
                    String filename = reg.toString().replace(':', '_').replace('/', '_')
                        + "_" + stack.getMetadata() + ".png";
                    File outFile = new File(outputDir, filename);

                    try {
                        renderToFile(renderItem, fb, stack, size, outFile);
                        count++;
                    } catch (Exception e) {
                        SuperMegaRenderer.LOGGER.warn("Skipping {}: {}", filename, e.getMessage());
                    }
                }
            }
        } finally {
            fb.deleteFramebuffer();
            mc.getFramebuffer().bindFramebuffer(true);
        }

        SuperMegaRenderer.LOGGER.info("Exported {} icons to {}", count, outputDir.getAbsolutePath());
        return count;
    }

    private static void renderToFile(RenderItem renderItem, Framebuffer fb, ItemStack stack, int size, File outFile) throws Exception {
        fb.bindFramebuffer(true);

        // Do NOT ASK ME WHAT IS GOING ON HERE
        GlStateManager.clearColor(0f, 0f, 0f, 0f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(
            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,       GL11.GL_ONE_MINUS_SRC_ALPHA
        );
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0, 16, 16, 0, 1000, 3000);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.translate(0f, 0f, -2000f); // not a fan of this depth yet tbh
        RenderHelper.enableGUIStandardItemLighting();
        renderItem.renderItemAndEffectIntoGUI(stack, 0, 0);
        RenderHelper.disableStandardItemLighting();
        // XD
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ByteBuffer buf = ByteBuffer.allocateDirect(size * size * 4);
        GL11.glReadPixels(0, 0, size, size, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int i = ((size - 1 - y) * size + x) * 4;
                int r = buf.get(i)     & 0xFF;
                int g = buf.get(i + 1) & 0xFF;
                int b = buf.get(i + 2) & 0xFF;
                int a = buf.get(i + 3) & 0xFF;
                img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        ImageIO.write(img, "PNG", outFile);
    }
}
