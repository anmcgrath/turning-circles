package com.turningcircles;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.WorldEntity;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WorldEntityDespawned;
import net.runelite.api.events.WorldEntitySpawned;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Singleton
public class BoatManager {

    private final Client client;
    private final TurningCirclesConfig config;

    // track boats
    private final Map<Integer, WorldEntity> spawnedEntities = new HashMap<>();

    // going over a crystal mote increases your max speed by 0.5 for 50 ticks
    private final int CRYSTAL_MOTE_TICKS = 50;
    public int ticksSinceMote = -1;
    private int lastMoteTick;

    protected void startUp(){
    }

    protected void shutDown(){
        spawnedEntities.clear();
    }

    @Inject
    public BoatManager(Client client, TurningCirclesConfig config) {
        this.client = client;
        this.config = config;
    }

    public boolean isCrystalMoteSpeedBoostActive() {
        return ticksSinceMote >= 0 && ticksSinceMote <= CRYSTAL_MOTE_TICKS;
    }

    @Subscribe
    public void onGameTick(GameTick e) {
        if (lastMoteTick > 0)
            ticksSinceMote = client.getTickCount() - lastMoteTick;
    }

    @Subscribe
    public void onChatMessage(ChatMessage c) {
        if (c.getMessage().contains("The crystal mote grants")) {
            lastMoteTick = client.getTickCount();
        }
    }


    @Subscribe
    public void onWorldEntitySpawned(WorldEntitySpawned e) {
        // keep track of the world views that a boat is in
        // when the player's world view id matches then they are on the boat
        spawnedEntities.put(e.getWorldEntity().getWorldView().getId(), e.getWorldEntity());
    }

    @Subscribe
    public void onWorldEntityDespawned(WorldEntityDespawned e) {
        spawnedEntities.remove(e.getWorldEntity().getWorldView().getId(), e.getWorldEntity());
    }


    // Returns the boat world entity if sailing, otherwise null
    public WorldEntity getBoatEntity() {
        if (!isOnBoat())
            return null;

        var playerWvId = client.getLocalPlayer().getWorldView().getId();

        if (spawnedEntities.containsKey(playerWvId)) {
            return spawnedEntities.get(playerWvId);
        }

        return null;
    }

    public boolean isOnBoat() {
        return client.getVarbitValue(VarbitID.SAILING_BOARDED_BOAT) == 1;
    }

    public boolean isNavigating() {
        return client.getTopLevelWorldView().getYellowClickAction() == Constants.CLICK_ACTION_SET_HEADING;
    }

}
