package com.turningcircles;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.WorldEntity;
import net.runelite.api.events.*;
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

    // going over a crystal event increases your max speed by 0.5
    public int ticksSinceCrystalEvent = -1;
    private int lastCrystalEventTick;

    public boolean isOnBoat = false;

    /// The max speed of the boat without any special considerations
    public double boatSpeedCap = 0;
    /// The base speed of the boat when in moveMode 2
    public double boatBaseSpeed = 0;
    /// The acceleration the boat is capable of
    public double boatAcceleration = 0;
    /// The number of ticks that a speed boost lasts
    public double boostTickDuration = 0;
    /// The move mode. 1 = low speed, 2 = full speed, 3 = reverse, 4 = not moving but will move to 2. 0 = not moving.
    public double moveMode = 0;
    // Store previous move mode here so we can check which move mode to move to from 0
    private double lastMoveMode = 2;

    private static final String SAIL_SPEED_BOOST = "You trim the sails, catching the wind for a burst of speed!";
    private static final String MOTE_SPEED_BOOST = "You release the wind mote for a burst of speed!";
    private static final String CRYSTAL_MOTE_SPEED_BOOST = "The crystal mote grants both a small burst of speed as well as a wind mote.";
    private static final String GWENITH_GLIDE_PORTAL_SPEED_BOOST = "The boat is sucked through the portal and sent somewhere else.";
    private static final String CRYSTALS_REMOVED_SPEED_BOOST = "The crystals have been successfully removed from the helm, granting a burst of speed.";

    private int lastWindBoostTick;
    public int ticksSinceLastWindBoost = -1;

    protected void startUp() {
    }

    protected void shutDown() {
        spawnedEntities.clear();
    }

    @Inject
    public BoatManager(Client client, TurningCirclesConfig config) {
        this.client = client;
        this.config = config;
    }

    /***
     * Returns true if the crystal boost is active for nTicks from now.
     * @param nTicks the number of ticks from now
     * @return
     */
    public boolean isCrystalSpeedBoostActive(int nTicks) {
        return ticksSinceCrystalEvent >= 0 && ticksSinceCrystalEvent + nTicks <= boostTickDuration;
    }

    /***
     * Returns true if the crystal boost is active.
     */
    public boolean isCrystalSpeedBoostActive() {
        return isCrystalSpeedBoostActive(0);
    }

    /***
     * Returns true if the wind speed boost is active for nTicks from now.
     * @param nTicks the number of ticks from now
     */
    public boolean isWindSpeedBoostActive(int nTicks) {
        return ticksSinceLastWindBoost >= 0 && ticksSinceLastWindBoost + nTicks <= boostTickDuration;
    }

    /***
     * Returns true if the wind speed boost is active
     */
    public boolean isWindSpeedBoostActive() {
        return isWindSpeedBoostActive(0);
    }

    /***
     * Returns the actual max speed taking into consideration boosts
     * and move mode, nTicks from now. This considers boosts running out.
     */
    public double getMaxSpeed(int nTicks) {
        if (isReversing())
            return 0.5;

        var cappedSpeed = 1.0; // for half-speed
        if (moveMode == 2 || moveMode == 4 || (moveMode == 0 && lastMoveMode == 4)) {
            cappedSpeed = boatBaseSpeed;
        }
        if (isWindSpeedBoostActive(nTicks))
            cappedSpeed = Math.max(cappedSpeed + 0.5, boatSpeedCap);

        if (isCrystalSpeedBoostActive(nTicks)) {
            cappedSpeed += 0.5; // deliberately don't take max with cappedSpeed as I think it's additional
        }

        return cappedSpeed;
    }

    /***
     * Returns the actual max speed taking into consideration boosts
     * and move mode.
     */
    public double getMaxSpeed() {
        return getMaxSpeed(0);
    }

    @Subscribe
    public void onGameTick(GameTick e) {
        if (lastCrystalEventTick > 0)
            ticksSinceCrystalEvent = client.getTickCount() - lastCrystalEventTick;

        if (lastWindBoostTick > 0)
            ticksSinceLastWindBoost = client.getTickCount() - lastWindBoostTick;
    }

    @Subscribe
    public void onChatMessage(ChatMessage c) {
        if (c.getMessage().contains(CRYSTAL_MOTE_SPEED_BOOST) ||
                c.getMessage().contains(GWENITH_GLIDE_PORTAL_SPEED_BOOST) ||
                c.getMessage().contains(CRYSTALS_REMOVED_SPEED_BOOST)) {
            lastCrystalEventTick = client.getTickCount();
        }
        if (c.getMessage().equals(SAIL_SPEED_BOOST) || c.getMessage().equals(MOTE_SPEED_BOOST)) {
            lastWindBoostTick = client.getTickCount();
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

    @Subscribe
    public void onVarbitChanged(VarbitChanged e) {
        if (e.getVarbitId() == VarbitID.SAILING_BOARDED_BOAT) {
            isOnBoat = e.getValue() == 1;
        } else if (e.getVarbitId() == VarbitID.SAILING_SIDEPANEL_BOAT_SPEEDCAP) {
            boatSpeedCap = e.getValue() / 128f;
        } else if (e.getVarbitId() == VarbitID.SAILING_SIDEPANEL_BOAT_BASESPEED) {
            boatBaseSpeed = e.getValue() / 128f;
        } else if (e.getVarbitId() == VarbitID.SAILING_SIDEPANEL_BOAT_ACCELERATION) {
            boatAcceleration = e.getValue() / 128f;
        } else if (e.getVarbitId() == VarbitID.SAILING_SIDEPANEL_BOAT_MOVE_MODE) {
            lastMoveMode = moveMode;
            moveMode = e.getValue();
        } else if (e.getVarbitId() == VarbitID.SAILING_SIDEPANEL_BOAT_SPEEDBOOST_DURATION) {
            boostTickDuration = e.getValue();
        }
    }

    // Returns the boat world entity if sailing, otherwise null
    public WorldEntity getBoatEntity() {
        if (!isOnBoat)
            return null;

        var playerWvId = client.getLocalPlayer().getWorldView().getId();

        if (spawnedEntities.containsKey(playerWvId)) {
            return spawnedEntities.get(playerWvId);
        }

        return null;
    }

    public boolean isReversing() {
        return moveMode == 3;
    }

    /***
     * Whether the player is currently navigating the boat
     * @return
     */
    public boolean isNavigating() {
        return client.getTopLevelWorldView().getYellowClickAction() == Constants.CLICK_ACTION_SET_HEADING;
    }

}
