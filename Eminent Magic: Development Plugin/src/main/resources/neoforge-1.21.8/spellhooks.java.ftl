package ${package};

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import ${package}.procedures.*;
import emmy.eminentmagic.spell.SpellTrigger;
import emmy.eminentmagic.spell.SpellTriggerEvent;

@EventBusSubscriber(modid = ${JavaModName}.MODID)
public final class ${name}SpellHooks {
	private ${name}SpellHooks() {
	}

	@SubscribeEvent
	public static void onSpellTrigger(SpellTriggerEvent event) {
		if (!event.spell().id().equals(ResourceLocation.fromNamespaceAndPath(${JavaModName}.MODID, "${data.spellId}"))) {
			return;
		}
<#macro invoke trigger procedure="">
<#if procedure?has_content>
		if (event.trigger() == SpellTrigger.${trigger}) {
			${procedure.name}Procedure.execute(<#list procedure.getDependencies(generator.getWorkspace()) as dependency>event.context().get("${dependency.name}")<#sep>, </#list>);
		}
</#if>
</#macro>
<@invoke procedure=(data.onCast!"") trigger="ON_CAST"/>
<@invoke procedure=(data.whileCharging!"") trigger="WHILE_CHARGING"/>
<@invoke procedure=(data.whileChannelling!"") trigger="WHILE_CHANNELLING"/>
	}
}