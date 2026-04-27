package net.salesianos.common;

import java.io.Serializable;

public class PlayerScore implements Serializable, Comparable<PlayerScore> {

    private static final long serialVersionUID = 1L;
    private final String name;
    private int score;

    public PlayerScore(String name) {
        this.name = name;
        this.score = 0;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void sumScore(int score) {
        this.score += score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(PlayerScore other) {
        int cmp = Integer.compare(other.score, this.score);
        if (cmp != 0) return cmp;
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return name + ": " + score + " pts";
    }
}