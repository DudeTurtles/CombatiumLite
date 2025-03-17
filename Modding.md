# Weapon Modding

Combatium supports weapon modding from external plugins. This functionality is disabled by default, but can be enabled in the config.yml file.
This allows developers to ensure functionality with their plugins when modifying things like reach, damage, and attack speed.
Below is a brief explanation of how to implement weapon modding in your plugin.

## Persistent Data Containers
Combatium Lite uses Persistent Data Containers to store weapon data. This allows for easy access to weapon data, and ensures that the data is saved and loaded correctly.
This can be accessed from the ItemMeta of an item, using the .getPersistentDataContainer() method.
This container can be queried for the data using the .get() method, and can be set using the .set() method.
For either of these, you will need to use the proper key for the data you are trying to access. The keys used for Combatium will be listed below.<br>
For reach, this will be updated for the player next time they get an item check, which ensures an item is as it should be.<br>
This usually happens any time the player crafts the item, attacks with it, picks it up, interacts with it in their hand, or clicks it in an inventory.<br>

For attack speed, Combatium Lite uses rounded ticks to match CTS 8c. This means that to know the exact cooldown of something, divide 20 by it, and round to the nearest integer.<br>
For example, a sword with an attack speed of 3 will have 20/3 = 6.666666666666667, which will be rounded to 7 ticks of cooldown time.
Please keep this in mind when setting attack speed, as it will be rounded.

### MAKE SURE TO USE THE CORRECT DATA TYPE FOR THE DATA YOU ARE TRYING TO ACCESS OR SET, OTHERWISE IT WILL NOT WORK.

## Keys
```java
public static final NamespacedKey ATTACK_SPEED = new NamespacedKey("combatium", "attack_speed");
public static final NamespacedKey REACH = new NamespacedKey("combatium", "reach");
public static final NamespacedKey ATTACK_DAMAGE = new NamespacedKey("combatium", "attack_damage");
```

Each of these are stored as doubles, so please make sure to access and set them properly.

## Examples
This is a short example method which sets a weapons attack speed.
```java
public void setAttackSpeed(ItemStack item, double attackSpeed) {
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();
    container.set(new NamespacedKey("combatium", "attack_speed"), PersistentDataType.DOUBLE, attackSpeed);
    item.setItemMeta(meta);
}
```
