package org.fipro.mafia.common;

public final class MafiaBossConstants {

    private MafiaBossConstants() {
        // private default constructor for constants class
        // to avoid someone extends the class
    }

    public static final String TOPIC_BASE = "org/fipro/mafia/Boss/";
    public static final String TOPIC_CONVINCE = TOPIC_BASE + "CONVINCE";
    public static final String TOPIC_ENCASH = TOPIC_BASE + "ENCASH";
    public static final String TOPIC_SOLVE = TOPIC_BASE + "SOLVE";
    public static final String TOPIC_ALL = TOPIC_BASE + "*";

    public static final String PROPERTY_KEY_TARGET = "target";

}