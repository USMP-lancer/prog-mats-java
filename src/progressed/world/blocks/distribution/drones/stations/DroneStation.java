package progressed.world.blocks.distribution.drones.stations;

import arc.*;
import arc.Input.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.pooling.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.world.*;
import progressed.entities.units.entity.*;

import static mindustry.Vars.*;

public class DroneStation extends Block{
    public int maxTextLength = 220;
    public float loadSpeed = 1f / 30f;
    public boolean lowFlier = true;
    public String defName = "Frog";
    public Color selectColor = Color.white;

    public TextureRegion input, output;

    public DroneStation(String name){
        super(name);

        solid = true;
        update = true;
        displayFlow = false;
        configurable = saveConfig = true;

        config(String.class, (DroneStationBuild tile, String text) -> { //If you couldn't guess, this was stolen from message blocks
            if(text.length() > maxTextLength){
                return; //no
            }

            tile.stationName.ensureCapacity(text.length());
            tile.stationName.setLength(0);

            text = text.replace("\n", ""); //No new lines
            text = text.trim();
            for(int i = 0; i < text.length(); i++){
                char c = text.charAt(i);
                tile.stationName.append(c);
            }
        });
        config(Boolean.class, (DroneStationBuild b, Boolean bool) -> b.connected = bool);
        config(Integer.class, (DroneStationBuild build, Integer i) -> build.state = StationState.all[i]);
        config(Byte.class, (DroneStationBuild build, Byte hhhhh) -> build.dumpCargo()); //ALL IN THE NAME OF SYNCING
    }

    @Override
    public void load(){
        super.load();

        input = Core.atlas.find(name + "-input", "prog-mats-drone-station-input");
        output = Core.atlas.find(name + "-output", "prog-mats-drone-station-output");
    }

    public class DroneStationBuild extends Building{
        public boolean connected = false, active, loading, loaded, dumping, taken;
        public float load;
        public StationState state = StationState.disconnected;
        public StringBuilder stationName;
        public Vec2 loadPoint = new Vec2(), loadVector = new Vec2();

        @Override
        public void created(){
            super.created();

            if(stationName == null){
                stationName = new StringBuilder(defName + " Station");
            }
        }

        @Override
        public void updateTile(){
            updateLoading();

            if(!active && !connected){
                state = StationState.disconnected;
            }
        }

        public boolean canConnect(StationState state){
            return !(connected || active) || state == this.state;
        }

        public boolean isOrigin(){
            return state == StationState.origin;
        }

        public Color selectColor(){
            return selectColor;
        }

        public void dumpCargo(){
            dumping = !dumping;
        }

        public boolean ready(){
            return false;
        }

        public void connect(int end){
            configure(true);
            configure(end);
        }

        public void disconnect(){
            configure(false);
        }

        public void updateLoading(){
            if(loading){
                load = Mathf.approachDelta(load, isOrigin() ? 1 : 0, loadSpeed);
                if(load == (isOrigin() ? 1f : 0f)){
                    loading = false;
                    loaded = true;
                }

                loadVector.trns(angleTo(loadPoint), dst(loadPoint) * load);
            }else{
                resetLoading();
            }
        }

        public void resetLoading(){
            loadVector.set(0, 0);
            loaded = false;
            taken = false;
        }

        public void updateCargo(DroneUnitEntity d){
            if(isOrigin()) loadPoint.set(d);
        }

        public void loadCargo(DroneUnitEntity d){
            load = 0;
        }

        public void takeCargo(DroneUnitEntity d){
            if(!taken){
                load = 1;
                loadPoint.set(d);
                loadVector.trns(angleTo(loadPoint), dst(loadPoint) * load);
                taken = true;
                loading = true;
            }
        }

        public void setLoading(){
            if(!loaded){
                active = true;
                loading = true;
            }
        }

        @Override
        public boolean canPickup(){
            return false; //I don't want to deal with weirdness
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            drawTeamTop();

            if(isOrigin()){
                Draw.rect(input, x, y);
            }else{
                Draw.rect(output, x, y);
            }
        }

        //This is all stolen from message block
        @Override
        public void drawSelect(){
            if(renderer.pixelator.enabled()) return;

            Font font = Fonts.outline;
            GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
            CharSequence text = stationName == null || stationName.length() == 0 ? "[lightgray]" + Core.bundle.get("empty") : stationName;
            boolean ints = font.usesIntegerPositions();
            font.setUseIntegerPositions(false);
            font.getData().setScale(1f / 3f);
            layout.setText(font, text);

            font.setColor(selectColor);
            float dx = x + offset, dy = y + offset + size * tilesize / 2f + 3;
            font.draw(text, dx, dy + layout.height + 1, Align.center);
            dy -= 1f;
            Lines.stroke(2f, Color.darkGray);
            Lines.line(dx - layout.width / 2f - 2f, dy, dx + layout.width / 2f + 1.5f, dy);
            Lines.stroke(1f, selectColor);
            Lines.line(dx - layout.width / 2f - 2f, dy, dx + layout.width / 2f + 1.5f, dy);

            font.setUseIntegerPositions(ints);
            font.setColor(Color.white);
            font.getData().setScale(1f);
            Draw.reset();
            Pools.free(layout);
        }

        @Override
        public void buildConfiguration(Table table){
            table.button(Icon.pencil, () -> {
                if(mobile){
                    Core.input.getTextInput(new TextInput(){{
                        text = stationName.toString();
                        multiline = true;
                        maxLength = maxTextLength;
                        accepted = str -> {
                            if(!str.equals(text)) configure(str);
                        };
                    }});
                }else{
                    BaseDialog dialog = new BaseDialog("@pm-drone-editname");
                    dialog.setFillParent(false);
                    TextArea a = dialog.cont.add(new TextArea(stationName.toString().replace("\r", "\n"))).size(380f, 160f).get();
                    a.setMaxLength(maxTextLength);
                    dialog.buttons.button("@ok", () -> {
                        if(!a.getText().equals(stationName.toString())) configure(a.getText());
                        dialog.hide();
                    }).size(130f, 60f);
                    dialog.update(() -> {
                        if(tile.block() != DroneStation.this){
                            dialog.hide();
                        }
                    });
                    dialog.show();
                }
                deselect();
            }).tooltip("@pm-drone-editname").size(40f);

            ImageButton dump = table.button(Icon.trash, Styles.defaulti, () -> configure((byte)0)).tooltip("@pm-drone-dump").size(40).get();
            dump.getStyle().over = Tex.buttonDown;
            dump.getStyle().checked = Tex.buttonOver;
            dump.update(() -> {
                dump.setChecked(dumping);
            });
        }

        @Override
        public void handleString(Object value){
            stationName.setLength(0);
            stationName.append(value);
        }

        @Override
        public void updateTableAlign(Table table){
            Vec2 pos = Core.input.mouseScreen(x, y + size * tilesize / 2f + 1);
            table.setPosition(pos.x, pos.y, Align.bottom);
        }

        @Override
        public Object config(){
            return stationName.toString();
        }

        @Override
        public void configure(Object value){
            //save last used config (Only save name changes, do not save state changes)
            if(value instanceof String) block.lastConfig = value; //Only save name changes
            Call.tileConfig(player, self(), value);
        }

        @Override
        public void remove(){
            connected = active = loading = false;
            state = StationState.disconnected;

            super.remove();
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.bool(connected);
            write.bool(active);
            write.bool(loading);
            write.bool(loaded);
            write.bool(dumping);
            write.bool(taken);
            write.i(state.ordinal());
            write.str(stationName.toString());

            write.f(loadVector.x);
            write.f(loadVector.y);
            write.f(loadPoint.x);
            write.f(loadPoint.y);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            connected = read.bool();
            active = read.bool();
            loading = read.bool();
            loaded = read.bool();
            dumping = read.bool();
            taken = read.bool();
            state = StationState.all[read.i()];
            stationName = new StringBuilder(read.str());

            loadVector.set(read.f(), read.f());
            loadPoint.set(read.f(), read.f());
        }
    }

    public enum StationState{
        origin,
        destination,
        disconnected;

        public static final StationState[] all = values();
    }
}
