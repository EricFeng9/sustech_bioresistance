package sustech.bioresistance;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
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
    
    //acidovorax_citrulli_medium Acidovorax citrulli培养基
    public static final RegistryKey<Item> ACIDOVORAX_CITRULLI_MEDIUM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "acidovorax_citrulli_medium"));
    public static final Item ACIDOVORAX_CITRULLI_MEDIUM = register(
            new Item(new Item.Settings()),
            ACIDOVORAX_CITRULLI_MEDIUM_KEY
    );
    
    //e_coli_medium 大肠杆菌培养基
    public static final RegistryKey<Item> E_COLI_MEDIUM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "e_coli_medium"));
    public static final Item E_COLI_MEDIUM = register(
            new Item(new Item.Settings()),
            E_COLI_MEDIUM_KEY
    );
    
    //e_coli_t6ss_medium 装配T6SS的大肠杆菌培养基
    public static final RegistryKey<Item> E_COLI_T6SS_MEDIUM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "e_coli_t6ss_medium"));
    public static final Item E_COLI_T6SS_MEDIUM = register(
            new Item(new Item.Settings()),
            E_COLI_T6SS_MEDIUM_KEY
    );
    
    //metronidazole 甲硝唑
    public static final RegistryKey<Item> METRONIDAZOLE_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "metronidazole"));
    public static final Item METRONIDAZOLE = register(
            new sustech.bioresistance.items.MetronidazoleItem(new Item.Settings()),
            METRONIDAZOLE_KEY
    );

    //streptomycin 链霉素
    public static final RegistryKey<Item> STREPTOMYCIN_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "streptomycin"));
    public static final Item STREPTOMYCIN = register(
            new sustech.bioresistance.items.StreptomycinItem(new Item.Settings()),
            STREPTOMYCIN_KEY
    );
    
    //antifungal-drug 抗真菌药物
    public static final RegistryKey<Item> ANTIFUNGAL_DRUG_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "antifungal_drug"));
    public static final Item ANTIFUNGAL_DRUG = register(
            new sustech.bioresistance.items.AntifungalDrugItem(new Item.Settings()),
            ANTIFUNGAL_DRUG_KEY
    );

    //anti-drug resistant microbial capsules 抗耐药性微生物胶囊
    public static final RegistryKey<Item> ANTI_DRUG_RESISTANT_MICROBIAL_CAPSULES_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "anti_drug_resistant_microbial_capsules"));
    public static final Item ANTI_DRUG_RESISTANT_MICROBIAL_CAPSULES = register(
            new Item(new Item.Settings()),
            ANTI_DRUG_RESISTANT_MICROBIAL_CAPSULES_KEY
    );
    
    //anti-drug resistant microbial ointment 抗耐药性微生物软膏
    public static final RegistryKey<Item> ANTI_DRUG_RESISTANT_MICROBIAL_OINTMENT_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "anti_drug_resistant_microbial_ointment"));
    public static final Item ANTI_DRUG_RESISTANT_MICROBIAL_OINTMENT = register(
            new Item(new Item.Settings()),
            ANTI_DRUG_RESISTANT_MICROBIAL_OINTMENT_KEY
    );

    //syringe 注射器
    public static final RegistryKey<Item> SYRINGE_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "syringe"));
    public static final Item SYRINGE = register(
            new Item(new Item.Settings()),
            SYRINGE_KEY
    );
    
    //hydrogel 水凝胶
    public static final RegistryKey<Item> HYDROGEL_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "hydrogel"));
    public static final Item HYDROGEL = register(
            new Item(new Item.Settings()),
            HYDROGEL_KEY
    );

    //empty capsule 空胶囊
    public static final RegistryKey<Item> EMPTY_CAPSULE_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "empty_capsule"));
    public static final Item EMPTY_CAPSULE = register(
            new Item(new Item.Settings()),
            EMPTY_CAPSULE_KEY
    );
    //e.coli extract 大肠杆菌提取液
    public static final RegistryKey<Item> E_COLI_EXTRACT_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "e_coli_extract"));
    public static final Item E_COLI_EXTRACT = register(
            new Item(new Item.Settings()),
            E_COLI_EXTRACT_KEY
    );
    
    //antibiotic secretion bacteria extract 抗生素分泌菌提取液
    public static final RegistryKey<Item> ANTIBIOTIC_BACTERIA_EXTRACT_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "antibiotic_bacteria_extract"));
    public static final Item ANTIBIOTIC_BACTERIA_EXTRACT = register(
            new Item(new Item.Settings()),
            ANTIBIOTIC_BACTERIA_EXTRACT_KEY
    );
    
    //DNA segment 1
    public static final RegistryKey<Item> DNA_SEGMENT_1_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "dna_segment_1"));
    public static final Item DNA_SEGMENT_1 = register(
            new sustech.bioresistance.items.DnaSegmentItem(new Item.Settings(), "dna_segment_1.description"),
            DNA_SEGMENT_1_KEY
    );
    
    //DNA segment 2
    public static final RegistryKey<Item> DNA_SEGMENT_2_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "dna_segment_2"));
    public static final Item DNA_SEGMENT_2 = register(
            new sustech.bioresistance.items.DnaSegmentItem(new Item.Settings(), "dna_segment_2.description"),
            DNA_SEGMENT_2_KEY
    );
    
    //DNA segment 3
    public static final RegistryKey<Item> DNA_SEGMENT_3_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "dna_segment_3"));
    public static final Item DNA_SEGMENT_3 = register(
            new sustech.bioresistance.items.DnaSegmentItem(new Item.Settings(), "dna_segment_3.description"),
            DNA_SEGMENT_3_KEY
    );

    //生老鼠肉
    public static final RegistryKey<Item> RAW_RAT_MEAT_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "raw_rat_meat"));
    public static final Item RAW_RAT_MEAT = register(
            new sustech.bioresistance.items.RawRatMeatItem(new Item.Settings()),
            RAW_RAT_MEAT_KEY
    );

    //熟老鼠肉
    public static final RegistryKey<Item> COOKED_RAT_MEAT_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "cooked_rat_meat"));
    public static final Item COOKED_RAT_MEAT = register(
            new sustech.bioresistance.items.CookedRatMeatItem(new Item.Settings()),
            COOKED_RAT_MEAT_KEY
    );
    
    //老鼠生成蛋
    public static final RegistryKey<Item> RAT_SPAWN_EGG_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bioresistance.MOD_ID, "rat_spawn_egg"));
    public static final Item RAT_SPAWN_EGG = register(
            new net.minecraft.item.SpawnEggItem(ModEntities.RAT, 0x656565, 0x3B3B3B, new Item.Settings()),
            RAT_SPAWN_EGG_KEY
    );
    
    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ModItemGroups.CUSTOM_ITEM_GROUP_KEY).register((itemGroup) -> {
            //itemGroup.add(EXPLANATORY_LIQUID_B);
            //itemGroup.add(EXPLANATORY_LIQUID_BP);
            //itemGroup.add(EXPLANATORY_LIQUID_P);
            itemGroup.add(MEDIUM);
            itemGroup.add(MEDIUM_STERILIZED);
            itemGroup.add(ACIDOVORAX_CITRULLI_MEDIUM);
            itemGroup.add(E_COLI_MEDIUM);
            itemGroup.add(E_COLI_T6SS_MEDIUM);
            itemGroup.add(METRONIDAZOLE);
            itemGroup.add(STREPTOMYCIN);
            itemGroup.add(ANTIFUNGAL_DRUG);
            itemGroup.add(ANTI_DRUG_RESISTANT_MICROBIAL_CAPSULES);
            itemGroup.add(ANTI_DRUG_RESISTANT_MICROBIAL_OINTMENT);
            itemGroup.add(SYRINGE);
            itemGroup.add(HYDROGEL);
            itemGroup.add(EMPTY_CAPSULE);
            itemGroup.add(E_COLI_EXTRACT);
            itemGroup.add(ANTIBIOTIC_BACTERIA_EXTRACT);
            itemGroup.add(DNA_SEGMENT_1);
            itemGroup.add(DNA_SEGMENT_2);
            itemGroup.add(DNA_SEGMENT_3);
            itemGroup.add(RAW_RAT_MEAT);
            itemGroup.add(COOKED_RAT_MEAT);
            itemGroup.add(RAT_SPAWN_EGG);
        });
    }
}
