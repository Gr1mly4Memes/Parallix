package gr1mly4memes.parallix.common;

public enum WorldThreaderTickPhase {
    NONE,
    WORLD_TICK,
    RECEIVE_TELEPORTS,
    TICK_AFTER_TELEPORT,
    RECOVER_FAILED_TELEPORTS
}
