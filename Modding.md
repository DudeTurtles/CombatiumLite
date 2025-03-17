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

Please keep in mind, if you've updated an item and would like the items lore to reflect the changes, you will need to set the UPDATE_LORE key to true, and then update the item meta.

Items can also be modded to block. An item is considered blockable if it has the blocking animation attached to it.
Unless the item has the below keys for BLOCK_ANGLE, BLOCK_AMOUNT, and KNOCKBACK_REDUCTION, it will use the defaults of 130, 5.0, and 0.5 respectively.
For blockable modding, the block angle is in degrees,  the knockback reduction is added to the players knockback resistance, and the block amount is the amount of damage the blockable will block.

### MAKE SURE TO USE THE CORRECT DATA TYPE FOR THE DATA YOU ARE TRYING TO ACCESS OR SET, OTHERWISE IT WILL NOT WORK.

## Keys
```java
public static final NamespacedKey ATTACK_SPEED = new NamespacedKey("combatium", "attack_speed");
public static final NamespacedKey REACH = new NamespacedKey("combatium", "reach");
public static final NamespacedKey ATTACK_DAMAGE = new NamespacedKey("combatium", "attack_damage");
public static final NamespacedKey BLOCK_ANGLE = new NamespacedKey("combatium", "block_angle");
public static final NamespacedKey BLOCK_AMOUNT = new NamespacedKey("combatium", "block_amount");
public static final NamespacedKey KNOCKBACK_REDUCTION = new NamespacedKey("combatium", "knockback_reduction");
public static final NamespacedKey UPDATE_LORE = new NamespacedKey("combatium", "update_lore");
```

Each of these EXCEPT FOR UPDATE LORE, WHICH IS A BOOLEAN, is a double, so please make sure to access and set them properly.

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
