package com.hyser.hysercore.abilities;

import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Ability {
    private String id;
    private String name;
    private List<String> description;
    private String permission;
    private int cooldown;
    private boolean enabled;
    private List<Object> triggers;
    private List<Object> actions;
    private List<Object> conditions;
    
    public Ability(String id, ConfigurationSection config) {
        this.id = id;
        this.name = config.getString("name", id);
        // Soporte para descriptions multilínea
        if (config.isList("description")) {
            this.description = config.getStringList("description");
        } else {
            this.description = new ArrayList<>();
            String singleDesc = config.getString("description", "");
            if (!singleDesc.isEmpty()) {
                this.description.add(singleDesc);
            }
        }
        this.permission = config.getString("permission", "hysercore.ability." + id);
        this.cooldown = config.getInt("cooldown", 0);
        this.enabled = config.getBoolean("enabled", true);
    }
    
    public boolean canUse(Player player) {
        if (!enabled) return false;
        // Permisos removidos - todas las abilities disponibles para todos
        
        // Las condiciones se verifican en el manager
        
        return true;
    }
    
    public void execute(Player player) {
        // Las acciones se ejecutan en el manager
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getDescription() { return description; }
    
    public String getDescriptionAsString() {
        if (description == null || description.isEmpty()) {
            return "";
        }
        return String.join(" ", description);
    }
    public String getPermission() { return permission; }
    public int getCooldown() { return cooldown; }
    public boolean isEnabled() { return enabled; }
    public List<Object> getTriggers() { return triggers; }
    public List<Object> getActions() { return actions; }
    public List<Object> getConditions() { return conditions; }
    
    // Setters
    public void setTriggers(List<Object> triggers) { this.triggers = triggers; }
    public void setActions(List<Object> actions) { this.actions = actions; }
    public void setConditions(List<Object> conditions) { this.conditions = conditions; }
}