package com.ironclad.clangoals;

import net.runelite.api.Client;
import net.runelite.api.clan.ClanSettings;

public class ClanUtils
{
    // Confirm that the current character is
    // a member of the clan.
    public static boolean isMemberOfClan (Client client)
    {
        final ClanSettings clan = client.getClanSettings();

        return !(clan == null || !clan.getName().equals("IronClad"));
    }
}
