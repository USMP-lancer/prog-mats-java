package progressed.content;

import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.type.statuseffects.*;

import static mindustry.content.StatusEffects.*;

public class PMStatusEffects implements ContentList{
    public static StatusEffect
    incendiaryBurn,

    //Anti-vaxxers are quivering in fear
    vcFrenzy, vcDisassembly, vcWeaken, vcCorvus;

    @Override
    public void load(){
        incendiaryBurn = new StatusEffect("incend-burn"){{
            color = Pal.lightPyraFlame;
            damage = 3.6f;
            effect = PMFx.incendBurning;
            transitionDamage = 14f;

            init(() -> {
                opposite(wet, freezing);
                affinity(tarred, (unit, result, time) -> {
                    unit.damagePierce(transitionDamage);
                    Fx.burning.at(unit.x + Mathf.range(unit.bounds() / 2f), unit.y + Mathf.range(unit.bounds() / 2f));
                    result.set(incendiaryBurn, Math.min(time + result.time, 450f));
                });
            });
        }};

        //Anti-vaxxers are quivering in fear
        vcFrenzy = new ExclusiveStatusEffect("frenzy"){{
            color = Color.valueOf("E25656");
            damageMultiplier = 3f;
            speedMultiplier = 3f;
            reloadMultiplier = 3f;
            healthMultiplier = 0.25f;
        }};

        vcDisassembly = new ExclusiveStatusEffect("disassembly"){{
            color = Color.darkGray;
            reloadMultiplier = 0.6f;
            speedMultiplier = 0.7f;
            healthMultiplier = 0.6f;
            damage = 1f;
        }};

        vcWeaken = new ExclusiveStatusEffect("weaken"){{
            color = Pal.sapBulletBack;
            healthMultiplier = 0.7f;
            damageMultiplier = 0.5f;
        }};

        vcCorvus = new ExclusiveStatusEffect("corvus-19"){{ //lmao
            color = Pal.heal;
            speedMultiplier = 0.8f;
            reloadMultiplier = 0.8f;
            damage = 8f;
            hideDetails = false;
        }};

        afterLoad();
    }

    public void afterLoad(){
        ((ExclusiveStatusEffect)vcFrenzy).exclusives = Seq.with(vcDisassembly, vcWeaken, vcCorvus);
        ((ExclusiveStatusEffect)vcDisassembly).exclusives = Seq.with(vcFrenzy, vcWeaken, vcCorvus);
        ((ExclusiveStatusEffect)vcWeaken).exclusives = Seq.with(vcFrenzy, vcDisassembly, vcCorvus);
        ((ExclusiveStatusEffect)vcCorvus).exclusives = Seq.with(vcFrenzy, vcDisassembly, vcWeaken);
    }
}