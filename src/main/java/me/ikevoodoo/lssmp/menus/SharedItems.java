import me.ikevoodoo.smpcore.SMPPlugin;
import me.ikevoodoo.smpcore.items.ItemClickResult;
import me.ikevoodoo.smpcore.items.ItemClickState;
import me.ikevoodoo.smpcore.text.messaging.MessageBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SharedItems {

    public static void register(SMPPlugin plugin) {

        plugin.createItem()
                .id("empty")
                .friendlyName(MessageBuilder.messageOf("Empty"))
                .name(() -> MessageBuilder.messageOf(" "))
                .material(() -> Material.GRAY_STAINED_GLASS_PANE)
                .bind((player, itemStack, itemClickResult) -> new ItemClickResult(ItemClickState.SUCCESS, true))
                .register();

        plugin.createItem()
                .id("next")
                .friendlyName(MessageBuilder.messageOf("Next"))
                .name(() -> MessageBuilder.messageOf("§a§lNext"))
                .material(() -> getPlayerHead("steve")) // Example player head
                .bind((player, stack) -> plugin.getMenuHandler().get(player).next(player))
                .register();

        plugin.createItem()
                .id("prev")
                .friendlyName(MessageBuilder.messageOf("Prev"))
                .name(() -> MessageBuilder.messageOf("§a§lPrevious"))
                .material(() -> getPlayerHead("alex")) // Example player head
                .bind((player, stack) -> plugin.getMenuHandler().get(player).previous(player))
                .register();
    }

    private static ItemStack getPlayerHead(String name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD); // Use PLAYER_HEAD material
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta(); // Get the SkullMeta
        if (skullMeta != null) {
            skullMeta.setOwner(name); // Set the player name for the head
            head.setItemMeta(skullMeta); // Set the modified meta back to the item
        }
        return head; // Return the head ItemStack
    }
}
