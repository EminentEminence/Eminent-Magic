<#if data.hasGUITexture?? && data.hasGUITexture()>
{
  "model": {
    "type": "minecraft:select",
    "property": "minecraft:display_context",
    "cases": [
      {
        "when": ["gui", "fixed", "ground"],
        "model": {
          "type": "minecraft:model",
          "model": "${modid}:item/${registryname}_gui"
        }
      }
    ],
    "fallback": {
      "type": "minecraft:model",
      "model": "${modid}:item/${registryname}"
    }
  }
}
<#else>
{
  "model": {
    "type": "minecraft:model",
    "model": "${modid}:item/${registryname}"
  }
}
</#if>
