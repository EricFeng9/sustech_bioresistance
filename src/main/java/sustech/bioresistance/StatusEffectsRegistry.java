package sustech.bioresistance;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class StatusEffectsRegistry {
    public static final StatusEffect PLAGUE = new PlagueStatusEffect();

    public static void register() {
        Registry.register(Registries.STATUS_EFFECT, new Identifier("examplemod", "plague"), PLAGUE);
    }

    public static class PlagueStatusEffect extends StatusEffect {
        public PlagueStatusEffect() {
            super(StatusEffectCategory.HARMFUL, 0x8B4513); // 棕色
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            // 每5秒造成伤害
            if (entity.age % 100 == 0) {
                entity.damage(entity.getDamageSources().magic(), 1.0f);
            }
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }
    }
}
