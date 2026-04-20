package li.gerard.supermegarenderer;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = SuperMegaRenderer.MODID,
    name = "Super Mega Renderer",
    version = "1.0",
    clientSideOnly = true
)
public class SuperMegaRenderer {
    public static final String MODID = "supermegarenderer";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new RenderExportCommand());
    }
}
