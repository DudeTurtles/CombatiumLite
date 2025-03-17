<div align="center">
<h1 class="underlined-title">Combatium Lite</h1>
Combatium Lite is a plugin which aims to port over the Combat Test Snapshots to the latest version as accurately as possible.<br>
This page is a work in progress, which aims to document the changes which this plugin makes to combat.<br>
(I just listed off the changes I could remember, so please let me know if any are missing)
</div>
<div align="center">
<h2 class="underlined-title">Changes</h2>
<h2 class="center">Combat</h2>
</div>
 - Attacks are no longer weakened when a weapon is not fully charged.<br>
 - There are now 2 types of attacks: normal and charged.<br>
 - Visually, the cooldown shows the time for a charged attack, which takes twice as much charge time as a normal attack.<br>
 - You can no longer attack before the cooldown for a normal attack is finished.<br>
   - While visually you can swing (this is due to the limitations of plugins), you cannot deal damage until the cooldown is finished.<br>
 - Cooldowns are faster when compared to vanilla.<br>
 - Similar to pre-1.9 combat, spam attacking is now possible, but not as effective.<br>
 - When spam attacking, you cannot fully charge an attack, and thus will never get the increased reach from full charged attacks (see Reach).<br>
 - A missed attack no longer fully resets your attack cooldown, only giving a 4 tick delay until you can attack again.<br>
 - You can now perform sprinting critical attacks again.<br>
 - Changing weapons no longer resets your attack cooldown.<br>
 - Eating is now interrupted when a player takes damage from attacks or projectiles.<br>
 - Consume time for all liquids, such as potions, soups, etc. is now 1 second, or 20 ticks.<br>
 - You can now hit mobs through passable blocks such as grass, flowers, and cobwebs.<br>

<div align="center">
<h3 class="center">Grace Period</h3>
</div>
 - Attacks now have a grace period. This acts as a buffer for attacks, meaning if you attack before the cooldown is finished, the attack will still go through, but it will wait until your cooldown is up.<br>
 - The grace period varies depending on the attack speed of a weapon, but it tends to be between 1 and 2 ticks.<br>
 - Grace period hits go through 1 tick slower than if the hit had been timed correctly.<br>
 - The grace period helps make combat feel more consistent, and reduces the impact of things like spam clicking.<br>
 - This can be disabled in the config.<br>

<div align="center">
<h3 class="center">Attack Speeds</h3>
</div>
 - As mentioned above, weapon attack speeds have been generally increased.<br>
 - Visual attack speed no longer matches the attack speed listed, or the time before you can hit, but instead the time before you can fully charge an attack.<br>
   - this can be changed in the config, but is enabled by default.<br>
 - Generally, because of this, weapons can attack twice as fast as their visual attack speed.<br>
 - The attack speed of weapons is as follows:<br>
   - Hoes:<br>
     - Wooden: 2.0<br>
     - Stone: 2.5<br>
     - Iron: 3.0<br>
     - Gold, Diamond, Netherite: 3.5<br>
   - Swords: 3.0<br>
   - Tridents: 2.0<br>
   - Axes: 2.0<br>
   - Shovels: 2.0<br>
   - All other weapons use the default value of 2.5<br>

<div align="center">
<h3 class="center">Damage Values</h3>

#### Axes
Wooden: 5<br>
Stone: 5<br>
Gold: 5<br>
Iron: 6<br>
Diamond: 7<br>
Netherite: 8<br><br>
#### Hoes
Wooden: 2<br>
Stone: 2<br>
Gold: 2<br>
Iron: 3<br>
Diamond: 3<br>
Netherite: 4<br><br>
#### Pickaxes
Wooden: 3<br>
Stone: 3<br>
Gold: 3<br>
Iron: 4<br>
Diamond: 5<br>
Netherite: 6<br><br>
#### Shovels
Wooden: 2<br>
Stone: 2<br>
Gold: 2<br>
Iron: 3<br>
Diamond: 4<br>
Netherite: 5<br><br>
#### Swords
Wooden: 4<br>
Stone: 4<br>
Gold: 4<br>
Iron: 5<br>
Diamond: 6<br>
Netherite: 7<br><br>
</div>
<div align="center">
<h3 class="center">Shield Changes</h3>
</div>
 - Shields no longer have a cooldown when starting to block.<br>
 - Shields now have a decreased arc of 100 degrees (was 180).<br>
 - Shields no longer block all damage, only blocking up to 5 hearts from most attacks.<br>
   - Shields still block all damage from projectiles.<br>
 - Shields now only block half of knockback from attacks.<br>
 - Shield now always are disabled for 1.6 seconds after being hit with an axe, unless the axe has the enchantment Cleaving.<br>

<div align="center">
<h3 class="center">Damage Calculations</h3>
</div>
 - Critical hits now include enchantments in the 50% damage boost.<br>
 - Strength potions now give a percent damage boost, similar to critical hits, giving a 20% damage boost per level.<br>
   - Strength and critical hits do not include each other in their damage calculations, but are stackable additively.<br>
 - Maces fall damage bonus isn't included in crits and strength.<br>

<div align="center">
<h4 class="center">Impaling</h4>
</div>
 - Impaling now impacts any mobs in water, or in rain, instead of just aquatic mobs.<br>

<div align="center">
<h4 class="center">Sweeping Edge</h4>
</div>
 - Sweep attacks can now only be dealt if the weapon has the Sweeping Edge enchantment.<br>
 - Sweeping edge now deals half as much damage as previously.<br>
 - Sweep attacks can now be dealt even if an entity isn't hit.<br>

<div align="center">
<h4 class="center">Cleaving</h4>
</div>
- A new enchantment for axes, which allows them to increase the time they disable shields, as well as deal more damage.<br>
- Cleaving has 3 levels, with each level increasing the time shields are disabled by 0.5 seconds.<br>
- Cleaving increases damage by 2 damage (1 heart) increasing by 1 damage (0.5 hearts) per subsequent level.<br>
- Cleaving is mutually exclusive with Sharpness, Bane of Arthropods, and Smite.<br>

<div align="center">
<h4 class="center">Projectiles</h4>
</div>
 - Projectiles no longer deal invulnerability frames, meaning multiple projectiles can hit an entity at once.<br>
 - Zero damage projectiles such as snowballs and eggs now deal knockback.<br>
 - Snowballs and eggs now have a cooldown of 4 ticks.<br>
 - Snowballs are stackable to 64<br>
 - Tridents can now be shot from dispensers.<br>

<div align="center">
<h2 class="center">Reach</h2>
</div>
 - Reach has changed to be unique to each weapon, with a default value of 2.5<br>
 - Hoes: 3.5<br>
 - Swords: 3.0<br>
 - Tridents: 3.5<br>
 - All other weapons use the default value of 2.5<br>

<div align="center">
<h3 class="center">Charged Attacks</h3>
</div>
 - Charged attacks, instead of dealing the full damage of the weapon, now increase reach by 1.<br>
 - Charged attacks take twice the cooldown of normal attacks.<br>

<div align="center">
<h3 class="center">Hunger</h3>
</div>
 - High saturation no longer gives a fast regeneration boost.<br>
 - Regeneration is now heals every 2 seconds, instead of 4.<br>
 - Regen now only impacts food level, and not saturation.<br>
 - Natural regeneration drains food half as fast.<br> 
 - Players now regen until hunger reaches 7, instead of 18.<br>

<div align="center">
<h3 class="center">Potions</h3>
</div>
 - Health potions now heal 6 hearts per level, instead of 4.<br>
 - Potions can now stack to 16.<br>
   - This does not include splash potions or lingering potions.<br>
 - As mentioned before, strength potions now give a percent damage boost, similar to critical hits, giving a 20% damage boost per level.<br>
 - Damage arrows now deal 1/8th of the damage of the potion, similar to the changed durations on other effects.<br>
