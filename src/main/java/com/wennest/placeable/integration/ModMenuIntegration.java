package com.wennest.placeable.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.wennest.placeable.PlaceableConfig;
import com.wennest.placeable.PlaceablePlants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//? if >=26 {
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
//?} else {
/*import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
*///?}

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    /**
     * Resolve a localized display name for a plant entry.
     *
     * <p>{@link I18n} is a client-only class
     * ({@code net.minecraft.client.resource.language.I18n}); referencing it
     * from the server-loadable {@link PlaceablePlants} enum would risk
     * {@code NoClassDefFoundError} on a dedicated server. Keeping the lookup
     * inside this {@code @Environment(EnvType.CLIENT)} class confines the
     * client-only dependency to the client side.
     */
    //? if >=26 {
    private static net.minecraft.network.chat.Component tr(String key) { return net.minecraft.network.chat.Component.translatable(key); }
    private static net.minecraft.network.chat.Component lt(String s) { return net.minecraft.network.chat.Component.literal(s); }
    //?} else {
    /*private static Text tr(String key) { return Text.translatable(key); }
    private static Text lt(String s) { return Text.literal(s); }
    *///?}

    private static String translateName(PlaceablePlants plant) {
        //? if >=26 {
        return I18n.get(plant.getBlock().getDescriptionId());
        //?} else {
        /*return I18n.translate(plant.getBlock().getTranslationKey());
        *///?}
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            PlaceableConfig config = AutoConfig.getConfigHolder(PlaceableConfig.class).getConfig();
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(tr("mod.name"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // General Category
            ConfigCategory genericCategory = builder.getOrCreateCategory(
                    tr("config.placeable.category.general")
            );
            genericCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            tr("config.placeable.option.enable"),
                            config.enable
                    )
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.enable = newValue)
                    .build()
            );
            genericCategory.addEntry(entryBuilder
                    .startBooleanToggle(
                            tr("config.placeable.option.placed_without_top_rim"),
                            config.placedWithoutTopRim
                    )
                    .setTooltip(
                            tr("config.placeable.option.placed_without_top_rim.tooltip")
                    )
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> config.placedWithoutTopRim = newValue)
                    .build()
            );

            // Allowed Plants Category
            ConfigCategory allowedPlantsCategory = builder.getOrCreateCategory(
                    tr("config.placeable.category.allowed_plants")
            );
            for (PlaceablePlants plants : PlaceablePlants.values()) {
                boolean current = config.allowPlaceablePlants.get(plants);
                allowedPlantsCategory.addEntry(entryBuilder
                        .startBooleanToggle(
                                lt(translateName(plants)),
                                current
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> config.allowPlaceablePlants.put(plants, newValue))
                        .build()
                );
            }

            // Saving
            builder.setSavingRunnable(() ->
                    AutoConfig.getConfigHolder(PlaceableConfig.class).save()
            );

            return builder.build();
        };
    }
}
