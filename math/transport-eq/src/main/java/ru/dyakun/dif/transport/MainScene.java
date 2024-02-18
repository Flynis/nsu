package ru.dyakun.dif.transport;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;


public class MainScene implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainScene.class);
    private static final double a = -1;
    private static final double b = 10;

    public TextField hTextField;
    public TextField rTextField;
    public TextField tTextField;
    public TextField delayTextField;
    public Canvas canvas;
    public ColorPicker solutionColorPicker;
    public ColorPicker explicitColorPicker;
    public ColorPicker implicitColorPicker;

    private Stage stage;
    private CoordinateSystem coordSystem;
    private DoubleField hField;
    private DoubleField rField;
    private DoubleField tField;
    private DoubleField delayField;
    private Timeline animation;


    public void setStage(Stage stage) {
        if(this.stage == null) {
            this.stage = stage;
        } else {
            throw new IllegalStateException("Stage is already initialized");
        }
    }

    public void startClick(ActionEvent ignored) {
        double h = hField.getValue();
        double r = rField.getValue();
        double t = tField.getValue();
        double delay = delayField.getValue();
        double tau = r * h;
        int frameCount = (int) (t / tau);
        logger.info("h = {}, tau = {}, r = {}, t = {}, frames = {}, delay = {}", h, tau, r, t, frameCount, delay);
        if(animation != null) {
            animation.stop();
        }
        Scheme implicit = new ImplicitScheme(h, r, a, b);
        Scheme explicit = new ExplicitScheme(h, r, a, b);
        Color solutionColor = solutionColorPicker.getValue();
        Color explicitColor = explicitColorPicker.getValue();
        Color implicitColor = implicitColorPicker.getValue();
        animation = new Timeline(
                new KeyFrame(Duration.millis(delay),
                event -> {
                    double time = explicit.getTime();
                    coordSystem.redraw();
                    coordSystem.drawContinuousFunction(a, b, time, Scheme.V, solutionColor);
                    coordSystem.drawFunction(a, h, explicit.next(), explicitColor);
                    coordSystem.drawFunction(a, h, implicit.next(), implicitColor);
                }));
        animation.setCycleCount(frameCount);
        animation.play();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
       coordSystem = new CoordinateSystem(canvas);
       coordSystem.redraw();
       hField = new DoubleField(hTextField);
       rField = new DoubleField(rTextField);
       tField = new DoubleField(tTextField);
       delayField = new DoubleField(delayTextField);
    }

}
