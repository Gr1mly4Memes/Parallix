package gr1mly4memes.parallix.common.scoreboard;

public interface ThreadsafeScoreboard {

    void worldthreader$ensureExclusiveScoreboardAccess();

    void worldthreader$crashIfNoExclusiveScoreboardAccess();
}
