package com.easyhomes.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class Home {
    private final String name;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final long createdAt;

    public Home(String name, Location location) {
        this.name = name;
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.createdAt = System.currentTimeMillis();
    }

    public Home(String name, String worldName, double x, double y, double z, float yaw, float pitch, long createdAt) {
        this.name = name;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    public String getWorldName() {
        return worldName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // Serialize to ConfigurationSection
    public void save(ConfigurationSection section) {
        section.set("world", worldName);
        section.set("x", x);
        section.set("y", y);
        section.set("z", z);
        section.set("yaw", yaw);
        section.set("pitch", pitch);
        section.set("created", createdAt);
    }

    // Deserialize from ConfigurationSection
    public static Home load(String name, ConfigurationSection section) {
        String worldName = section.getString("world");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        long createdAt = section.getLong("created", System.currentTimeMillis());

        return new Home(name, worldName, x, y, z, yaw, pitch, createdAt);
    }
}
