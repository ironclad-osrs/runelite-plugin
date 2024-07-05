package com.ironclad.clangoals;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.game.ItemManager;

@Setter
@Getter
public class PluginItem
{
    private ItemManager itemManager;

    public int id;
    public int quantity;

    public PluginItem(int id, int quantity)
    {
        this.id = id;
        this.quantity = quantity;
    }

    public String getName()
    {
        try {
            return itemManager.getItemComposition(this.id).getName();
        } catch (Exception err) {
            return "";
        }
    }
}
