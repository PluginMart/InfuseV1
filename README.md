# Infuse V1
This is a recreation of the original Infuse SMP S1, not to be confused with Infuse SMP Revamped.  

When you kill someone with no negative effects, you get a positive potion effect.  
When you kill someone with any negative effects, you lose one of them.  
When you die with no positive effects, you gain a negative effect.  
When you die with any positive effects, you lose one of them.  

## Custom Items
There is 1 custom item:  
### The Good Potion
Removes one of your negative effects or gives you a positive effect.  

![effect recipe](https://cdn.modrinth.com/data/cached_images/5f5216214cd00f14bf60de8f8c32b02a800691b0.png)

## Commands
`/infuse`  
 |- `help`: Shows the help message.  
 |- `reload`: Reloads the config.  
|- `drain`: Drains a positive effect from the player.  
 |- `setscore <player> <score>`: Sets a player's score.  Also rerolls their effects.  
 |- `getscore <player>`: Gets a player's score.  
 \\- `give <player> <item> [count]`: Gives a player an infuse item.

`/drain`: Drains a positive effect from the player.

## Permissions
- `infusev1.help`: Lets players use `/infuse help`
- `infusev1.reload`: Lets players use `/infuse reload`
- `infusev1.drain`: Lets players use `/infuse drain`
- `infusev1.setscore`: Lets players use `/infuse setscore`
- `infusev1.getscore`: Lets players use `/infuse getscore`
- `infusev1.give`: Lets playerss use `/infuse give`

## Config
```yml
# Starting score of any player.  A positive score will give the player that many positive effects.  A negative score will give the player that many negative effects.
# Can be any number between max_score and min_score
starting_score: 0

# Maximum and minimum scores.  A player's score cannot go past these bounds.
# These should be equal to or smaller than the number of effects in positive_effects and negative_effects
max_score: 8
min_score: -8

# True if players should lose an effect when they die to something other than a player.
lose_effect_on_natural_death: true

# True if players should get an effect when they kill someone at the minimum score.
get_effect_on_min_score: false

# A list of all the positive effects
# Use effects from here: https://minecraft.wiki/w/Effect#List_of_effects
# Effects should be in lowercase for proper conversion to Identifiers
# Warning: Effect names may change across versions.  If you run into errors, look for a list for your version.
# You can also use custom effects defined in datapacks by their identifier
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
# Use effects from here: https://minecraft.wiki/w/Effect#List_of_effects
negative_effects:
 - weakness
 - slowness
 - mining_fatigue
 - jump_boost
 - slow_falling
 - glowing
 - unluck
 - hunger

# The level of each effect
effect_level: 1

# Overrides for the "effect_level" config.
# Lets you change the level that a specific effect (positive or negative) will get boosted to.
override_levels:
 haste: 2

###
### Recipes
###

# IMPORTANT: Recipes will not be updated until the server restarts!
recipes:
 good_potion:
  # Defines the type of recipe.
  # Can be "shaped" or "shapeless"
  # For shaped recipes:
  #   'shape' is a list of strings representing each row of the recipe.
  #   'ingredients' is a map of characters to items in the recipe.
  # For shapeless recipes:
  #   'ingredients' is a list of items in the recipe.
  #
  # Item tags are supported.
  # This works pretty similarly to recipes in datapacks, except the keys are a bit different.
  # See https://minecraft.wiki/w/Recipe_(Java_Edition)#crafting_shaped for info on recipes.
  type: shaped
  shape:
   - "DND"
   - "DBD"
   - "DND"
  ingredients:
   D: diamond_block
   N: netherite_ingot
   B: glass_bottle
```
