package com.ironclad.clangoals.batches;

import com.ironclad.clangoals.PluginItem;
import com.ironclad.clangoals.service.ApiService;
import lombok.Setter;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.*;

@Setter
public class LootBatchQueue extends BatchQueue
{
    private ApiService api;
    private ItemManager itemManager;
    private long account;

    @Override
    public void flush()
    {
        if (items.isEmpty()) return;

        // Convert list to just loot data effectively
        // stripping away NPC data.
        ArrayList<QueueItem> loot = items.stream().flatMap(l -> {
            NpcLootReceived npcLootReceived = (NpcLootReceived) l.getData();
            Collection<ItemStack> items = npcLootReceived.getItems();

            return items.stream().map(i -> {
                PluginItem pluginItem = new PluginItem(i.getId(), i.getQuantity());

                // Pass down the ItemManager so that we
                // can gather the item name.
                pluginItem.setItemManager(itemManager);

                return new QueueItem(pluginItem);
            });
        }).collect(Collectors.toCollection(ArrayList::new));

        this.api.batchUpdateLoot(this.account, loot);

        this.resetQueue();
    }
}
