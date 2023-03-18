package io.gitlab.jfronny.meteoradditions.util;

public class MServerInfo {
    public String name;
    public String address;
    public String playerCountLabel;
    public int playerCount;
    public int playercountMax;
    public String label;
    public long ping;
    public String version = null;

    public MServerInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
