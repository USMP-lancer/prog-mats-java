package progressed.world.blocks.payloads;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class Missile extends Block{
    public BulletType explosion;
    public int explosions = 1;
    public float explosionArea = 0f;
    public float maxDelay;

    public Block prev;
    public float powerUse, constructTime = -1;
    public boolean requiresUnlock = true;

    public float elevation = -1f;

    public TextureRegion topRegion;

    public Missile(String name){
        super(name);

        buildVisibility = BuildVisibility.sandboxOnly;
        category = Category.units;
        health = 50; //volatile, do not destroy
        researchCostMultiplier = 5f;
        solid = true;
        update = true;
        hasShadow = false;
        rebuildable = false;
        drawDisabled = false;
        squareSprite = false;
    }

    @Override
    public void init(){
        if(constructTime < 0) constructTime = buildCost;
        if(elevation < 0) elevation = size / 3f;
        if(explosionArea < 0) explosionArea = size * tilesize;

        super.init();
    }

    @Override
    public void load(){
        super.load();

        region = Core.atlas.find(name + "-outline", name);
        topRegion = Core.atlas.find(name + "-top", region);
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{region, topRegion};
    }

    public void drawBase(Tile tile){
        Draw.z(Layer.blockUnder - 1f);
        Drawf.shadow(region, tile.drawx() - elevation, tile.drawy() - elevation);
        Draw.z(Layer.block);
        Draw.rect(region, tile.drawx(), tile.drawy());
        if(topRegion.found()) Draw.rect(topRegion, tile.drawx(), tile.drawy());
    }

    @Override
    public boolean canBeBuilt(){
        return false;
    }

    public class MissileBuild extends Building{
        @Override
        public void drawCracks(){
            if(explosion != null){
                float f = Mathf.clamp(healthf());
                Tmp.c1.set(Color.red).lerp(Color.white, f + Mathf.absin(Time.time, Math.max(f * 5f, 1f), 1f - f));
                Draw.color(Tmp.c1);
                Draw.rect(topRegion, x, y);
            }
        }

        @Override
        public void onDestroyed(){
            super.onDestroyed();

            //Kaboom
            explode();
        }

        public void explode(){
            if(explosion != null){
                for(int i = 0; i < explosions; i++){
                    Time.run(Mathf.random(maxDelay), () -> {
                        float dst = explosionArea * Mathf.sqrt(Mathf.random());
                        Tmp.v1.setToRandomDirection().setLength(dst);
                        Bullet b = explosion.create(this, Team.derelict, x + Tmp.v1.x, y + Tmp.v1.y, 0f);
                        b.time = b.lifetime;
                    });
                }
            }
        }
    }
}