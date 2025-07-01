<div align="center">
    
# CombatiumLite
![Available for paper](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/supported/paper_vector.svg)<br>
CombatiumLite is a plugin which aims to port over the Combat Test Snapshots to the latest version as accurately as possible.<br><br>
Since the combat test snapshots are no longer being updated, there are plans to keep this plugin both updated for the latest version of minecraft, and potentially to have a separate branch which aims to explore new features and changes to combat, similarly to how the combat test snapshots experimented with pvp.<br><br>
### [Changes from Vanilla](Changes.md)

</div>

### Configuration
This plugin is configurable through the config.yml file, but it is recommended to use the default settings. The default settings are designed to replicate the Combat Test Snapshots as closely as possible. While some of the config options are simple quality of life options, they help to maintain consistency, and help the modified combat make more sense to newer players. Things like grace period are also relatively important to the combat system, and are mainly config options for testing purposes. <br>
That being said, the config options which are disabled by default are not required, and are for specific situations. Please read the notes around the config options prior to changing them. <br>

### Other Support / Bugs
If you are having issues with this plugin or are encountering bugs, feel free to reach out.
- [Discord](https://discord.gg/mxwHCTVj8A)


### Plans
 - N/A all significant plans have been implemented

### Known Issues
 - Issues when connecting on combatify such as:
 - - (As this only happens when connecting with Combatify, we are unsure if it is an issue on combatify's end or ours)
 - - The /enchant command not working
 - - The anvil not working
 - - Armor trims not working
 - - Crossbows not appearing loaded when they are
 - 8c doesn't have bonus reach in creative mode, and so vanilla creative bonus reach should be removed.

#### Implementation ideas for plans
 - N/A all significant plans have been implemented

### Additions
- The mace has been updated to fit the updated combat
    - Mace has updated visual cooldown to match the new combat system
    - Mace enchants aren't boosted from strength potions and critical hits

### Notes
- While this aims to replicate Combat Test Snapshot 8c in vanilla minecraft, certain things may not function as they do in the original snapshot. This may include:
  - Attacks still visually go through when the cooldown is not finished.
  - There is no shield hitting.
  - Shields do not activate on crouch.
  - Changes to bow mechanics.
- If you would like to limit these inaccuracies, playing through the Combatify mod (ensure this is enabled in the plugin config), will make visual attacking accurate.

#### Requirements
- Java 21
- Gradle 8.8

#### Build
- Clone the repository
    - `git clone https://github.com/DudeTurtles/CombatiumLite.git`
- Run `./gradlew shadow shadowJar`
