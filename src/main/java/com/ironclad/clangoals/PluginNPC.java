package com.ironclad.clangoals;

import lombok.Getter;
import net.runelite.api.NPC;

@Getter
public class PluginNPC
{
    private final int id;
    private final String name;

    public PluginNPC(NPC npc)
    {
        id = npc.getId();
        name = npc.getName();
    }
}
