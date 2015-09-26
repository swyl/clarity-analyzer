package skadistats.clarity.analyzer.replay;

import javafx.beans.property.*;
import javafx.beans.property.adapter.ReadOnlyJavaBeanIntegerPropertyBuilder;
import skadistats.clarity.processor.entities.UsesEntities;
import skadistats.clarity.processor.runner.ControllableRunner;
import skadistats.clarity.source.MappedFileSource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

@ApplicationScoped
@UsesEntities
public class ReplayController {

    @Inject
    private ObservableEntityList entityList;

    private class TickingTask extends TimerTask {
        @Override
        public void run() {
            if (getTick() >= getLastTick()) {
                setPlaying(false);
                return;
            }
            if (!changing.getValue()) {
                getRunner().setDemandedTick(getTick() + 1);
            }
        }
    }
    private Timer timer;


    private Property<PropertySupportRunner> runner = new SimpleObjectProperty<>();
    private IntegerProperty tick = new SimpleIntegerProperty(0);
    private IntegerProperty lastTick = new SimpleIntegerProperty(0);
    private BooleanProperty playing = new SimpleBooleanProperty(false);
    private BooleanProperty changing = new SimpleBooleanProperty(false);

    @PostConstruct
    public void init() {
        playing.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (timer == null) {
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new TickingTask(), 0L, 1000L / 30L);
                }
            } else {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }
        });
    }

    public void load(File f) throws IOException, NoSuchMethodException {
        haltIfRunning();
        entityList.clear();
        PropertySupportRunner r = new PropertySupportRunner(new MappedFileSource(f.getAbsoluteFile()));
        lastTick.bind(new ReadOnlyJavaBeanIntegerPropertyBuilder().bean(r).name("lastTick").build());
        tick.bind(new ReadOnlyJavaBeanIntegerPropertyBuilder().bean(r).name("tick").build());
        runner.setValue(r);
        r.runWith(this, entityList);
        r.setDemandedTick(0);
    }

    @PreDestroy
    public void haltIfRunning() {
        if (getRunner() != null) {
            getRunner().halt();
        }
    }

    public ControllableRunner getRunner() {
        return runner.getValue();
    }

    public Property<PropertySupportRunner> runnerProperty() {
        return runner;
    }

    public void setRunner(PropertySupportRunner runner) {
        this.runner.setValue(runner);
    }

    public int getTick() {
        return tick.get();
    }

    public IntegerProperty tickProperty() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick.set(tick);
    }

    public int getLastTick() {
        return lastTick.get();
    }

    public IntegerProperty lastTickProperty() {
        return lastTick;
    }

    public void setLastTick(int lastTick) {
        this.lastTick.set(lastTick);
    }

    public boolean getPlaying() {
        return playing.get();
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    public boolean getChanging() {
        return changing.get();
    }

    public BooleanProperty changingProperty() {
        return changing;
    }

    public void setChanging(boolean changing) {
        this.changing.set(changing);
    }
}
