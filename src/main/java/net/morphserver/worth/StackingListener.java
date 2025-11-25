package net.morphserver.worth;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener that manually merges item stacks that only differ by the
 * worth-lore line that MorphWorth adds.
 *
 * Why this exists:
 *  - Vanilla Minecraft refuses to stack items if their lore differs.
 *  - MorphWorth stores the total stack worth in lore, so stacks with
 *    different amounts will have different lore.
 *  - To keep "worth based on amount" AND still allow stacking, we
 *    intercept clicks and perform the merge ourselves, then recalculate
 *    the worth for both slot and cursor items.
 */
public class StackingListener implements Listener {

    private final WorthManager worthManager;

    public StackingListener(WorthManager worthManager) {
        this.worthManager = worthManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // We only care about cases where both cursor and slot contain items.
        if (cursor == null || cursor.getType().isAir()) {
            return;
        }
        if (current == null || current.getType().isAir()) {
            return;
        }

        // Material must match.
        if (cursor.getType() != current.getType()) {
            return;
        }

        // Items must be the same ignoring the worth-lore line.
        if (!worthManager.isSameItemIgnoringWorthLore(cursor, current)) {
            return;
        }

        ClickType click = event.getClick();

        // Only handle simple left/right clicks where the player
        // explicitly puts the cursor stack onto the clicked slot.
        if (click != ClickType.LEFT && click != ClickType.RIGHT) {
            return;
        }

        int maxStack = current.getMaxStackSize();
        int slotAmount = current.getAmount();
        int cursorAmount = cursor.getAmount();

        // If slot is already full, there's nothing to merge.
        if (slotAmount >= maxStack) {
            return;
        }

        if (click == ClickType.LEFT) {
            // LEFT click = move as many items from cursor into the slot as possible.
            int space = maxStack - slotAmount;
            if (space <= 0) {
                return;
            }

            int toMove = Math.min(space, cursorAmount);
            int newSlotAmount = slotAmount + toMove;
            int newCursorAmount = cursorAmount - toMove;

            event.setCancelled(true);

            // Update slot item
            ItemStack newSlot = current.clone();
            newSlot.setAmount(newSlotAmount);
            worthManager.applyWorthLore(newSlot);
            event.setCurrentItem(newSlot);

            // Update cursor item (either leftover stack or empty)
            if (newCursorAmount > 0) {
                ItemStack newCursor = cursor.clone();
                newCursor.setAmount(newCursorAmount);
                worthManager.applyWorthLore(newCursor);
                event.setCursor(newCursor);
            } else {
                event.setCursor(null);
            }
        } else if (click == ClickType.RIGHT) {
            // RIGHT click = move exactly one item into the slot if there is space.
            if (cursorAmount <= 0) {
                return;
            }
            if (slotAmount >= maxStack) {
                return;
            }

            event.setCancelled(true);

            int newSlotAmount = slotAmount + 1;
            int newCursorAmount = cursorAmount - 1;

            ItemStack newSlot = current.clone();
            newSlot.setAmount(newSlotAmount);
            worthManager.applyWorthLore(newSlot);
            event.setCurrentItem(newSlot);

            if (newCursorAmount > 0) {
                ItemStack newCursor = cursor.clone();
                newCursor.setAmount(newCursorAmount);
                worthManager.applyWorthLore(newCursor);
                event.setCursor(newCursor);
            } else {
                event.setCursor(null);
            }
        }
    }
}
