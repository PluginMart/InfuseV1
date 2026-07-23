# Infuse V1
This is a recreation of the original Infuse SMP S1, not to be confused with Infuse SMP Revamped.  

When you kill someone with no negative effects, you get a positive potion effect.  
When you kill someone with any negative effects, you lose one of them.  
When you die with no positive effects, you gain a negative effect.  
When you die with any positive effects, you lose one of them.  

## Custom Items
There are 3 custom items:  
### Reviver
Lets you revive a player who was deathbanned after losing too many effects.  

![reviver recipe](https://cdn.modrinth.com/data/cached_images/3a1e884129cd5b342da916fbfe4503d79391c6c6.png)

### Enhancer
Boosts the level of your positive effects for a short time.  

![enhancer recipe](https://cdn.modrinth.com/data/cached_images/a5d28f21914e79575640307c4f0067e43ea40f7a.png)

### Infuse Effect
Removes one of your negative effects or gives you a positive effect.  

![effect recipe](https://cdn.modrinth.com/data/cached_images/5f5216214cd00f14bf60de8f8c32b02a800691b0.png)

## Commands
`/infuse`  
 |- `help`: Shows the help message.  
 |- `reload`: Reloads the config.  
 |- `revive <player>`: Revives a dead player.  
 |- `setscore <player> <score>`: Sets a player's score.  Also rerolls their effects.  
 |- `getscore <player>`: Gets a player's score.  
 \\- `give <player> <item> [count]`: Gives a player an infuse item.  

## Config
```yml
# Starting score of any player.  A positive score will give the player that many positive effects.  A negative score will give the player that many negative effects.
# Can be any number between -max_negative and max_positive
starting_score: 0

# Max and minimum scores.  A player's score cannot go past these bounds.
max_score: 8
min_score: -9

# The score that a player will get banned at.
# Setting the score to anything >= 0 will disable deathbans.
# If the config is lower than min_score, players will not be banned.
ban_score: -9

# The score that a player will be set to when a reviver is used on them.
revive_score: 0

# Maximum number of positive effects a player can have.
# Should be between 0 and the number of effects in positive_effects
max_positive: 8

# Maximum number of negative effects a player can have.
# Should be between 0 and the number of effects in negative_effects
max_negative: 8

# A list of all the positive effects
# Use effects from here: https://jd.papermc.io/paper/26.2/org/bukkit/potion/PotionEffectType.html
# Effects should be in lowercase for proper conversion to NamespacedKeys
# Warning: Effect names may change across versions.  If you run into errors, look for a list for your version.
# You can also use custom effects defined in datapacks by their namespaced key
positive_effects:
- strength
- speed
- haste
- fire_resistance
- health_boost
- dolphins_grace
- luck
- water_breathing

# A list of all the negative effects
# Use effects from here: https://jd.papermc.io/paper/26.2/org/bukkit/potion/PotionEffectType.html
negative_effects:
- weakness
- slowness
- mining_fatigue
- jump_boost
- slow_falling
- glowing
- unluck
- hunger

# The default level of each effect
effect_level: 1

# Overrides for the "effect_level" config.
# Lets you change the level that a specific effect (positive or negative) will get boosted to.
override_levels:
  haste: 2

###
### Enhancer Configs
###

# The duration of the enhancer's effects in seconds
enhancer_duration: 90

# The level that your effects will be boosted to
enhanced_level: 3

# Overrides for the "boost_level" config.
# Lets you change the level specific effects get boosted to.
enhanced_override_levels:
  strength: 2
  health_boost: 2

###
### Recipes
###

recipes:
  infuse_effect:
    type: shaped
    shape:
      - "DND"
      - "DBD"
      - "DND"
    ingredients:
      D: DIAMOND_BLOCK
      N: NETHERITE_INGOT
      B: GLASS_BOTTLE
  reviver:
    type: shaped
    shape:
      - "GBE"
      - "DND"
      - "EBG"
    ingredients:
      D: DRAGON_BREATH
      N: NETHER_STAR
      B: NETHERITE_BLOCK
      G: GHAST_TEAR
      E: FERMENTED_SPIDER_EYE
  enhancer:
    type: shaped
    shape:
      - "NGN"
      - "GBG"
      - "NGN"
    ingredients:
      G: ENCHANTED_GOLDEN_APPLE
      N: NETHERITE_INGOT
      B: GLASS_BOTTLE
```
