<div align="center">
    
# CombatiumLite
![Available for paper](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/supported/paper_vector.svg)<br>
CombatiumLite is a plugin which aims to port over the Combat Test Snapshots to the latest version as accurately as possible.<br><br>
Since the combat test snapshots are no longer being updated, there are plans to keep this plugin both updated for the latest version of minecraft, and to have a separate branch which aims to explore new features and changes to combat, similarly to how the combat test snapshots experimented with pvp. While the experimental branch isn't yet available, it will be in the near future, and will be listed here when available.<br><br>
### [Changes from Vanilla](Changes.md)

</div>

### Configuration
This plugin is configurable through the config.yml file, but it is recommended to use the default settings. The default settings are designed to replicate the Combat Test Snapshots as closely as possible. While some of the config options are simple quality of life options, they help to maintain consistency, and help the modified combat make more sense to newer players. Things like grace period and optimizations are also relatively important to the combat system, and are mainly config options for testing purposes. <br>
That being said, the config options which are disabled by default are generally jokes, and are not included in this statement, as the inclusion of these options is entirely for fun, and up to individual preference, and aren't in any way related to the original combat snapshots.

### Plans
- [ ] Add bedrock support
- [ ] Add 8c support
- [ ] Fix projectile throw velocity

### Known Issues
- [ ] Some actions such as throwing items from your inventory, or throwing projectiles, can sometimes trigger sweeping edge if the item in your main hand has the enchantment.
- - Unfortunately, this is an issue with paper, and is not something that can be fixed to my knowledge.


#### Implementation ideas for plans
 - N/A all significant plans have been implemented


### Additions
 - The mace has been updated to fit the updated combat
   - Mace has updated visual cooldown to match the new combat system
   - Mace enchants aren't boosted from strength potions and critical hits

### Notes
- While this aims to replicate Combat Test Snapshot 8c in vanilla minecraft, certain things may not function as they do in the original snapshot. This may include:
- - Attacks still visually go through when the cooldown is not finished.
- - There is no shield hitting.
- - Shields do not activate on crouch.
- - Some enchantment changes such as Fire Aspect on axes.
- - Changes to bow mechanics.

#### Requirements
- Java 21
- Gradle 8.8

#### Build
- Clone the repository
    - `git clone https://github.com/DudeTurtles/CombatiumLite.git`
- Run `./gradlew shadow shadowJar`
