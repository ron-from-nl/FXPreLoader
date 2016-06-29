package rdj;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Preloader;
import javafx.application.Preloader.ProgressNotification;
import javafx.application.Preloader.StateChangeNotification;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class FXPreloader extends Preloader
{
    private Stage stage;
    private StackPane infoStackpane;
    private Label titleLabel, statusLabel;
    private ProgressBar progressBar;
    private VBox widgetVBox;
    private Rectangle bgRectangle;
    private Rectangle infoRectangle;
    private Sphere sphere;
    private PerspectiveCamera pcam;
    private PointLight plight;
    private Group subSceneGroup,infoGroup;
    private Scene scene;
    private SubScene subScene;
    private StackPane stackpane;
    private RotateTransition sphereSpinTransition;
    
    @Override public void start(Stage stage) throws Exception
    {
        this.stage = stage;
	this.stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (WindowEvent window) ->	{ System.exit(0); });
        this.stage.setTitle("Preloader");
        
        this.stage.initStyle(StageStyle.UNDECORATED);

        int scenewidth = 800;
        int sceneheight = 120;
        int widgetwidth = 715;

        // Create Info Widget Block
        bgRectangle = createRect(scenewidth, sceneheight, false,false,false,0.4); // round, line, trans, opaque
	infoRectangle = createRect((widgetwidth+(widgetwidth*0.1)),(sceneheight-(sceneheight/10)), true, true, true, 0.4);
        titleLabel =  createLabel("Application Preloader",32, widgetwidth);
        statusLabel = createLabel("Loading: Main Application",16, widgetwidth);
        progressBar = createPBar(0, widgetwidth);
        widgetVBox = new VBox(titleLabel,createLabel("",8,widgetwidth),progressBar,statusLabel); widgetVBox.setAlignment(Pos.CENTER);
	infoStackpane = new StackPane(bgRectangle,infoRectangle,widgetVBox); // Last one gets mouseevents
        
        // Create SubScene Block
        sphere = new Sphere(100,16); sphere.setDrawMode(DrawMode.LINE); sphere.setCullFace(CullFace.NONE);
        sphere.setTranslateX(1700); sphere.setTranslateY(-130); sphere.setTranslateZ(1800);

        double flipDuration = 10;
	sphereSpinTransition = new RotateTransition(Duration.seconds(flipDuration), sphere);
	sphereSpinTransition.setInterpolator(Interpolator.EASE_BOTH);
	sphereSpinTransition.setAxis(Rotate.Y_AXIS);
	sphereSpinTransition.setByAngle(360);
	sphereSpinTransition.setCycleCount(Timeline.INDEFINITE);
	sphereSpinTransition.setAutoReverse(true);
        
        Task task = new Task<Void>() { @Override protected Void call() throws Exception
        {
                sphereSpinTransition.play();
                return null;
        }}; new Thread(task).start();

        pcam = new PerspectiveCamera(true);
	pcam.setNearClip(0.1);
	pcam.setFarClip(10000000000000d);
        pcam.setFieldOfView(20);
//	plight = new PointLight(Color.WHITE);

	subSceneGroup =         new Group(infoStackpane,sphere);
        subScene = new SubScene(subSceneGroup, scenewidth, sceneheight, true, SceneAntialiasing.BALANCED); subScene.setCamera(pcam);

        // Combining all blocks
        stackpane = new StackPane(subScene,infoStackpane); // sphere & info
        
        scene = new Scene(stackpane, scenewidth, sceneheight, true, SceneAntialiasing.BALANCED);
        
        this.stage.setScene(scene);
        this.stage.show();
    }
 
    @Override synchronized public void handleProgressNotification(ProgressNotification progressNote)
    {
//        Task task = new Task<Void>() { @Override protected Void call() throws Exception
//        {
//            double x = progressBar.getProgress();
//            double t = progressNote.getProgress();
//            for (x = 1; x < t; x += 0.01) {progressBar.setProgress(x); try {Thread.sleep(10); } catch (InterruptedException ex) {}
//        return null; }}; new Thread(task).start();
//        progressBar.setProgress(progressNote.getProgress());
    }
 
    @Override synchronized public void handleStateChangeNotification(StateChangeNotification stateChangeNote)
    {
//        status("handleStateChangeNotification: "+stateChangeNote.toString());
//        status(stateChangeNote.getType().name());
//        if (stateChangeNote.getType() == StateChangeNotification.Type.BEFORE_START) { stage.hide(); }
    }
 
    @Override synchronized public void handleApplicationNotification(PreloaderNotification preloaderNote)
    {
        if(preloaderNote instanceof ProgressNotification)
        {
            Task task = new Task<Void>() { @Override protected Void call() throws Exception
            {
//                progressBar.setProgress(((ProgressNotification) preloaderNote).getProgress()); 

                double x = progressBar.getProgress();
                double t = ((ProgressNotification) preloaderNote).getProgress();
                for (x = x; x < t; x += 0.01)
                {
                    progressBar.setProgress(x);
                    try { Thread.sleep(10); } catch (InterruptedException ex) {  }
                }
            return null; }}; new Thread(task).start();
        }
        else if (preloaderNote instanceof StateChangeNotification)
        {
            status("handleApplicationNotification: "+preloaderNote.toString()); /*stage.hide();*/ 
            status(((StateChangeNotification) preloaderNote).getType().name());
            if (((StateChangeNotification) preloaderNote).getType() == StateChangeNotification.Type.BEFORE_LOAD) { status("Loading..."); }
            if (((StateChangeNotification) preloaderNote).getType() == StateChangeNotification.Type.BEFORE_INIT) { status("Initializing..."); }
            if (((StateChangeNotification) preloaderNote).getType() == StateChangeNotification.Type.BEFORE_START) { status("Starting..."); }
        }
        else if (preloaderNote instanceof ErrorNotification)
        {
            if (((ErrorNotification) preloaderNote).getLocation().toLowerCase().equals("info"))
            {
                status(((ErrorNotification) preloaderNote).getDetails() + " " + ((ErrorNotification) preloaderNote).getCause().getMessage());
            }
            else if (((ErrorNotification) preloaderNote).getLocation().toLowerCase().equals("application"))
            {
                if (((ErrorNotification) preloaderNote).getDetails().toLowerCase().equals("title"))
                {
                    this.stage.setTitle(((ErrorNotification) preloaderNote).getCause().getMessage());
                    titleLabel.setText(((ErrorNotification) preloaderNote).getCause().getMessage());
                }
            }
            else if (((ErrorNotification) preloaderNote).getLocation().toLowerCase().equals("progress"))
            {
                if (((ErrorNotification) preloaderNote).getDetails().toLowerCase().equals("show")) {this.stage.show();}
                else if (((ErrorNotification) preloaderNote).getDetails().toLowerCase().equals("hide")) {this.stage.hide();}
            }
            else if (((ErrorNotification) preloaderNote).getLocation().toLowerCase().equals("error"))
            {
                status(((ErrorNotification) preloaderNote).getDetails() + " " + ((ErrorNotification) preloaderNote).getCause().getMessage());
            }
        }
    }
    
    private Rectangle createRect(double width, double height, boolean rounded, boolean line, boolean transparant, double opaque)
    {
	Rectangle rect = new Rectangle(0, 0, width, height);
	if (rounded) 
        {
            rect.setArcWidth(Math.min(width, height)*0.5);
            rect.setArcHeight(Math.min(width, height)*0.5);
        }
        if (transparant) {rect.setFill(Color.TRANSPARENT);} else {rect.setFill(Color.WHITESMOKE);}
        if (line) {rect.setStroke(Color.BLACK);rect.setStrokeWidth(2);}
	rect.setOpacity(opaque);
	return rect;
    }
    
    private Label createLabel(String value, int fontsize, int labelsize)
    {
	Label label = new Label(value);
	label.setPrefWidth(labelsize);
	label.setFont(Font.font(Font.getDefault().getName(),FontWeight.NORMAL, fontsize));
	label.setTextFill(Color.GREY);
	label.setAlignment(Pos.CENTER);
	return label;
    }

    private ProgressBar createPBar(int value, int width)
    {
        ProgressBar pbar = new ProgressBar(value);
        pbar.setVisible(false);
        pbar.setPrefWidth(width);
        pbar.setProgress(value);
        pbar.setVisible(true);
	return pbar;
    }

    private void status(String str)
    {
        statusLabel.setText(str);
        System.out.println(str);
    }
 }