{
  "schemaVersion": 1,
  "id": "${data.spellId?json_string}",
  "displayName": "${data.spellName?json_string}",
  "baseCost": ${data.baseCost?c},
  "strengthScaling": ${data.strengthScaling?c},
  "controlScaling": ${data.controlScaling?c},
  "element": "${data.element?json_string}",
  "channelled": ${data.channelled?c},
  "hooks": {
    "onCast": <#if data.onCast??>"${data.onCast.name?json_string}"<#else>null</#if>,
    "whileCharging": <#if data.whileCharging??>"${data.whileCharging.name?json_string}"<#else>null</#if>,
    "whileChannelling": <#if data.whileChannelling??>"${data.whileChannelling.name?json_string}"<#else>null</#if>
  }
}