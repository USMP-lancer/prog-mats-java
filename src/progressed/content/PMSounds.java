package progressed.content;

import arc.*;
import arc.assets.*;
import arc.assets.loaders.SoundLoader.*;
import arc.audio.*;
import mindustry.*;

/**
 * @author GlennFolker
 * For how to make this. Just copy over and adjust the code.
 */
public class PMSounds{
    public static Sound
        pixelShoot = new Sound(),
        pixelHit = new Sound(),
        harbingerCharge = new Sound(),
        harbingerBlast = new Sound(),
        popeshadowCharge = new Sound(),
        popeshadowBlast = new Sound(),
        swordStab = new Sound(),
        rocketLaunch = new Sound(),
        nuclearExplosion = new Sound(),
        sentinelWarning = new Sound(),
        pulseBeam = new Sound(),
        moonPiss = new Sound(),
        loudMoonPiss = new Sound();

    public static void load() {
        if(Vars.headless) return;

        pixelShoot = loadSound("pixel-shoot");
        pixelHit = loadSound("pixel-hit");
        harbingerCharge = loadSound("harbinger-charge");
        harbingerBlast = loadSound("harbinger-blast");
        popeshadowCharge = loadSound("popeshadow-charge");
        popeshadowBlast = loadSound("popeshadow-blast");
        swordStab = loadSound("sword-stab");
        nuclearExplosion = loadSound("nuclear-explosion");
        rocketLaunch = loadSound("rocket");
        sentinelWarning = loadSound("sentinel-warning");
        pulseBeam = loadSound("pulse-beam");
        moonPiss = loadSound("piss");
        loudMoonPiss = loadSound("piss-loud");
    }

    protected static Sound loadSound(String soundName){
        String name = "sounds/" + soundName;
        String path = Vars.tree.get(name + ".ogg").exists() ? name + ".ogg" : name + ".mp3";

        Sound sound = new Sound();

        AssetDescriptor<?> desc = Core.assets.load(path, Sound.class, new SoundParameter(sound));
        desc.errored = Throwable::printStackTrace;

        return sound;
    }
}