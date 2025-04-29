package sustech.bioresistance;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {
    public static Item register(Item item, RegistryKey<Item> registryKey) {
        // Register the item.
        Item registeredItem = Registry.register(Registries.ITEM, registryKey.getValue(), item);

        // Return the registered item!
        return registeredItem;
    }

    //explanatory liquid B 降解液B
    public static final RegistryKey<Item> EXPLANATORY_LIQUID_B_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "explanatory_liquid_b"));
    public static final Item EXPLANATORY_LIQUID_B = register(
            new Item(new Item.Settings()),
            EXPLANATORY_LIQUID_B_KEY
    );

    //explanatory liquid B-P 降解液B-P
    public static final RegistryKey<Item> EXPLANATORY_LIQUID_BP_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "explanatory_liquid_bp"));
    public static final Item EXPLANATORY_LIQUID_BP = register(
            new Item(new Item.Settings()),
            EXPLANATORY_LIQUID_BP_KEY
    );
    //explanatory liquid P 降解液P
    public static final RegistryKey<Item> EXPLANATORY_LIQUID_P_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "explanatory_liquid_p"));
    public static final Item EXPLANATORY_LIQUID_P = register(
            new Item(new Item.Settings()),
            EXPLANATORY_LIQUID_P_KEY
    );
    //medium 培养基
    public static final RegistryKey<Item> MEDIUM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID,"medium"));
    public static final Item MEDIUM = register(
            new Item(new Item.Settings()),
            MEDIUM_KEY
    );
    //medium 培养基(已消毒?)
    public static final RegistryKey<Item> MEDIUM_STERILIZED_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "medium_sterilized"));
    public static final Item MEDIUM_STERILIZED = register(
            new Item(new Item.Settings()),
            MEDIUM_STERILIZED_KEY
    );

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ModItemGroups.CUSTOM_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(EXPLANATORY_LIQUID_B);
            itemGroup.add(EXPLANATORY_LIQUID_BP);
            itemGroup.add(EXPLANATORY_LIQUID_P);
            itemGroup.add(MEDIUM);
            itemGroup.add(MEDIUM_STERILIZED);

        });
    }

}
