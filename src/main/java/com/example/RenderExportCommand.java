package com.example;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderExportCommand extends CommandBase {

    @Override
    public String getName() {
        return "renderexport";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/renderexport [size=512]  (size: 16–4096)";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        int size = 512;
        if (args.length >= 1) {
            size = parseInt(args[0], 16, 4096);
        }

        sender.sendMessage(new TextComponentString(
            "[SuperMegaRenderer] Exporting all items at " + size + "x" + size + "px, game will freeze briefly..."));

        try {
            int count = ItemExporter.export(size);
            sender.sendMessage(new TextComponentString(
                "[SuperMegaRenderer] Done! Exported " + count + " icons to the 'item_renders' folder."));
        } catch (Exception e) {
            sender.sendMessage(new TextComponentString("[SuperMegaRenderer] Export failed: " + e.getMessage()));
            SuperMegaRenderer.LOGGER.error("Export failed", e);
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
