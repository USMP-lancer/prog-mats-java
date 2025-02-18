package progressed.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.graphics.*;

import static arc.graphics.g2d.Draw.rect;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Mathf.*;
import static mindustry.Vars.*;

public class PMDrawf{
    private static Vec2 vec1 = new Vec2(), vec2 = new Vec2(), vec3 = new Vec2(), vec4 = new Vec2();

    public static void plus(float x, float y, float diameter, float angle, Color color, float alpha){
        color(color, alpha);
        for(int i = 0; i < 2; i++){
            Fill.rect(x, y, diameter / 3, diameter, angle + i * 90f);
        }
    }

    public static void cross(float x, float y, float width, float length, float angle){
        for(int i = 0; i < 4; i++){
            Drawf.tri(x, y, width, length, i * 90f + angle);
        }
    }

    public static void cross(float x, float y, float size, float angle){
        cross(x, y, size, size, angle);
    }

    public static void shadowAlpha(TextureRegion region, float x, float y, float rotation, float alpha){
        color(Tmp.c1.set(Pal.shadow).mul(1f, 1f, 1f, alpha));
        rect(region, x, y, rotation);
        color();
    }

    public static void vecLine(float x, float y, Vec2 v1, Vec2 v2, boolean cap){
        line(v1.x + x, v1.y + y, v2.x + x, v2.y + y, cap);
    }

    public static void lineAngleCenter(float x, float y, float angle, float length, boolean cap){
        vec1.trns(angle, length);

        line(x - vec1.x / 2, y - vec1.y / 2, x + vec1.x / 2, y + vec1.y / 2, cap);
    }

    public static void pill(float x, float y, float angle, float length, float width){
        stroke(width);
        lineAngleCenter(x, y, angle, length - width, false);
        
        for(int i = 0; i < 2; i++){
            Tmp.v1.trns(angle + 180f * i, length / 2f - width / 2f);
            Fill.circle(x + Tmp.v1.x, y + Tmp.v1.y, width / 2f);
        }
    }

    public static void target(float x, float y, float angle, float radius, Color ringColor, Color spikeColor, float alpha){
        color(Pal.gray, alpha);
        stroke(3);
        poly(x, y, 4, 7f * radius, angle);
        spikes(x, y, 3f * radius, 6f * radius, 4, angle);
        color(ringColor, alpha);
        stroke(1);
        poly(x, y, 4, 7f * radius, angle);
        color(spikeColor);
        spikes(x, y, 3f * radius, 6f * radius, 4, angle);
        color();
    }

    public static void target(float x, float y, float angle, float radius, Color color, float alpha){
        target(x, y, angle, radius, color, color, alpha);
    }

    //Too much duplicated code, how to condense down laser drawing?
    /** Meltdown laser drawing */
    public static void laser(Team team,  float x, float y, float length, float width, float angle, float scale, float[] tscales, float[] strokes, float[] lenscales, float[] pullscales, float oscScl, float oscMag, float spaceMag, Color[] colors, Color lightColor, float alpha){
        for(int s = 0; s < colors.length; s++){
            color(Tmp.c1.set(colors[s]).a(colors[s].a * alpha).mul(1f + absin(Time.time, 1f, 0.1f)));
            for(int i = 0; i < tscales.length; i++){
                Tmp.v1.trns(angle + 180f, (pullscales[i] - 1f) * spaceMag);
                stroke((width + absin(Time.time, oscScl, oscMag / scale)) * strokes[s] * tscales[i] * scale);
                lineAngle(x + Tmp.v1.x, y + Tmp.v1.y, angle, length * lenscales[i], false);
            }
        }

        Tmp.v1.trns(angle, (pullscales[pullscales.length - 1] - 1f) * spaceMag);
        Tmp.v2.trns(angle, length * lenscales[lenscales.length - 1]);
        Drawf.light(team, x + Tmp.v1.x, y + Tmp.v1.y, x + Tmp.v2.x, y + Tmp.v2.y, width * 2f, lightColor, 0.7f * alpha);
    }

    public static void laser(Team team, float x, float y, float length, float width, float angle, float scale, float[] tscales, float[] strokes, float[] lenscales, float oscScl, float oscMag, float spaceMag, Color[] colors, Color lightColor, float alpha){
        laser(team, x, y, length, width, angle, scale, tscales, strokes, lenscales, lenscales, oscScl, oscMag, spaceMag, colors, lightColor, alpha);
    }

    public static void laser(Team team, float x, float y, float length, float width, float angle, float scale, float[] tscales, float[] strokes, float[] lenscales, float oscScl, float oscMag, float spaceMag, Color[] colors, Color lightColor){
        laser(team, x, y, length, width, angle, scale, tscales, strokes, lenscales, oscScl, oscMag, spaceMag, colors, lightColor, 1f);
    }

    public static void vertConstruct(float x, float y, TextureRegion region, float rotation, float progress, float speed, float time){
        vertConstruct(x, y, region, Pal.accent, rotation, progress, speed, time);
    }

    public static void vertConstruct(float x, float y, TextureRegion region, Color color, float rotation, float progress, float speed, float time){
        PMShaders.vertBuild.region = region;
        PMShaders.vertBuild.progress = progress;
        PMShaders.vertBuild.color.set(color);
        PMShaders.vertBuild.color.a = speed;
        PMShaders.vertBuild.time = -time / 20f;

        shader(PMShaders.vertBuild);
        rect(region, x, y, rotation);
        shader();

        reset();
    }

    /** Draws a sprite that should be light-wise correct, Provided sprites myst be similar in shape */
    public static void spinSprite(TextureRegion light, TextureRegion dark, float x, float y, float r){
        float mr = mod(r, 360f);
        alpha(1f);
        rect(dark, x, y, r);
        alpha(0.5f * (Mathf.cosDeg(mr) + 1f));
        rect(light, x, y, r);
        alpha(1f);
    }

    public static void ellipse(float x, float y, float rad, float wScl, float hScl, float rot){
        float sides = circleVertices(rad);
        float space = 360 / sides;
        float r1 = rad - getStroke() / 2f, r2 = rad + getStroke() / 2f;

        for(int i = 0; i < sides; i++){
            float a = space * i;
            vec1.trns(rot,
                r1 * wScl * cosDeg(a),
                r1 * hScl * sinDeg(a)
            );
            vec2.trns(rot,
                r1 * wScl * cosDeg(a + space),
                r1 * hScl * sinDeg(a + space)
            );
            vec3.trns(rot,
                r2 * wScl * cosDeg(a + space),
                r2 * hScl * sinDeg(a + space)
            );
            vec4.trns(rot,
                r2 * wScl * cosDeg(a),
                r2 * hScl * sinDeg(a)
            );
            Fill.quad(
                x + vec1.x, y + vec1.y,
                x + vec2.x, y + vec2.y,
                x + vec3.x, y + vec3.y,
                x + vec4.x, y + vec4.y
            );
        }
    }

    /**
     * Color flash to entire screen made by
     * @author sunny
     * Customization with method made by
     * @author MEEPofFaith
     * */
    public static void flash(Color fromColor, Color toColor, float seconds, Interp fade){
        if(!headless){
            Image flash = new Image();
            flash.touchable = Touchable.disabled;
            flash.setColor(fromColor);
            flash.setFillParent(true);
            flash.actions(Actions.color(toColor, seconds));
            flash.actions(Actions.fadeOut(seconds, fade), Actions.remove());
            flash.update(() -> {
                if(!Vars.state.isGame()){
                    flash.remove();
                }
            });
            Core.scene.add(flash);
        }
    }
}