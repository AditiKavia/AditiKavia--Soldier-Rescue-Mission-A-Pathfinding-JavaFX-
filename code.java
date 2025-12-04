
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.SequentialTransition;


import java.util.*;

public class DSA_Project extends Application {

    final int CELL_SIZE = 40, GRID_W = 20, GRID_H = 20;
    final int PATH_STEP_DURATION_MS = 500;

    Point source = new Point(0, 0);//source coordinate
    Point target = new Point(13, 13);//dest coordinate

    Set<Point> dangerZones = new HashSet<>();
    Pane root;
    Random rand = new Random();

    private ImageView background;
    private ImageView targetSoldierMate;

    private final Image soldierRight = new Image("file:/home/aditi/Downloads/r.png");//image class is final so that no changes in future could happen 
    private final Image soldierLeft = new Image("file:/home/aditi/Downloads/l.png");
    private final Image kidnapped_commando = new Image("file:/home/aditi/Downloads/kidnapped_commando.png");

    static class Point {
        int x, y;
        Point(int x, int y) 
        {this.x = x; this.y = y;}
        public boolean equals(Object o) 
        {
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return p.x == x && p.y == y;
        }
        public int hashCode() { return Objects.hash(x, y);}
    }

    static class Node {
        Point point;
        double g, f;
        Node parent;
        Node(Point point, double g, double f, Node parent) {
            this.point = point; this.g = g; this.f = f; this.parent = parent;
        }
    }

    @Override
    public void start(Stage stage) {
        root = new Pane();

        background = new ImageView(new Image("file:/home/aditi/Downloads/jungle.jpg"));
        background.setFitWidth(GRID_W * CELL_SIZE);//fit image width to screen
        background.setFitHeight(GRID_H * CELL_SIZE);//fit image h to screen
        root.getChildren().add(background);//add background to root

        drawGridOverlay();

        drawCell(source, Color.WHITE);  // Draw source cell (white)
        targetSoldierMate = placeMateSoldier(target);// Add soldier image for target cell
        root.getChildren().add(targetSoldierMate);

       Scene scene = new Scene(root, CELL_SIZE * GRID_W+250, CELL_SIZE * GRID_H+150);//create a new scene object 
            //root i.e root pane which hold all ui element , 

        stage.setScene(scene);//attach the scene to window(stage)
        stage.setTitle("Soldier Rescue Mission With A* Pathfinding");
        stage.show();



//wait 2 sec to draw danger zone
        PauseTransition pause1 = new PauseTransition(Duration.seconds(2));
        pause1.setOnFinished(e -> {
            generateDangerZones();
            drawDangerZones();

         //pause 2 sec  before drawing path
            PauseTransition pause2 = new PauseTransition(Duration.seconds(2));
            pause2.setOnFinished(e2 -> {
            List<Point> path = aStarPath(source, target);
            drawPath(path);

            // Pause 2 seconds before animating soldiers to target
                PauseTransition pause3 = new PauseTransition(Duration.seconds(2));
                pause3.setOnFinished(e3 -> {
                    List<Point> pathToTarget = aStarPath(source, target);//actual shortest  path 

                    List<ImageView> soldiers = new ArrayList<>();
                    for (int i = 0; i < 2; i++) 
                    {
                        ImageView soldier = makeSoldier(source, true);//create soldier at sourcce ,right side view
                        root.getChildren().add(soldier);//add to soldier to grid
                        animateSoldier(soldier, pathToTarget, i * (PATH_STEP_DURATION_MS / 2), true);
                        soldiers.add(soldier);//adding into list so that program can remember the soldiers
                    }

                    // Calculate total time to reach target and schedule return trip
                    long totalPathDuration = (pathToTarget.size() * PATH_STEP_DURATION_MS) + 2000;

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() 
                        {
                            javafx.application.Platform.runLater(() -> 
                            {
                                dangerZones.clear();
                                generateDangerZones();
                                root.getChildren().retainAll(background, targetSoldierMate);
                                drawGridOverlay();
                                drawDangerZones();
                                drawCell(source, Color.WHITE);

                                List<Point> returnPath = aStarPath(target, source);//now start is target and dest is base camp
                                drawPath(returnPath);

                                // Remove stationary mate image before join
                                root.getChildren().remove(targetSoldierMate);

                               // soldiers.forEach(s -> root.getChildren().add(s));
for (ImageView s : soldiers) {
    root.getChildren().add(s);
}

                                // New soldier representing rescued mate joins team at target
                                ImageView newSoldier = makeSoldier(target, false);
                                root.getChildren().add(newSoldier);
                                soldiers.add(newSoldier);

                                for (int i = 0; i < soldiers.size(); i++) {
                                    animateSoldier(soldiers.get(i), returnPath, i * (PATH_STEP_DURATION_MS / 2), false);
                                }

                               long totalReturnDuration = (returnPath.size() * PATH_STEP_DURATION_MS) + 2000;

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        javafx.application.Platform.runLater(() -> showMissionSuccess());
                                    }
                                }, totalReturnDuration);
                            });
                        }
                    }, totalPathDuration);
                });
                
                pause3.play();
            });
            pause2.play();
        });
        pause1.play();
    

            }
    private ImageView placeMateSoldier(Point p) {
        ImageView soldier = new ImageView(kidnapped_commando);
        soldier.setFitWidth(CELL_SIZE * 0.8);
        soldier.setFitHeight(CELL_SIZE * 0.8);
        soldier.setX(p.x * CELL_SIZE + (CELL_SIZE - soldier.getFitWidth()) / 2.0);
        soldier.setY(p.y * CELL_SIZE + (CELL_SIZE - soldier.getFitHeight()) / 2.0);
        return soldier;
    }

    private List<Point> reconstructPath(Node end) 
    {
        LinkedList<Point> path = new LinkedList<>();
        for (Node n = end; n != null; n = n.parent) {
            path.addFirst(n.point);
        }
        return path;
    }

    private List<Point> neighbors(Point p) {
        return Arrays.asList(
                new Point(p.x + 1, p.y),
                new Point(p.x - 1, p.y),
                new Point(p.x, p.y + 1),
                new Point(p.x, p.y - 1)
        );
    }

    private List<Point> aStarPath(Point start, Point end) 
    {
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<Point, Node> allNodes = new HashMap<>();
        Set<Point> visited = new HashSet<>();

        Node startNode = new Node(start, 0, heuristic(start, end), null);
        open.add(startNode);
        allNodes.put(start, startNode);

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.point.equals(end))
                return reconstructPath(current);

            visited.add(current.point);

            for (Point nb : neighbors(current.point)) {
                if (nb.x < 0 || nb.x >= GRID_W || nb.y < 0 || nb.y >= GRID_H) continue;
                if (dangerZones.contains(nb) || visited.contains(nb)) continue;

                double g = current.g + 1;
                double f = g + heuristic(nb, end);
                Node nbNode = allNodes.getOrDefault(nb, new Node(nb, Double.POSITIVE_INFINITY, f, null));

                if (g < nbNode.g) {
                    nbNode.g = g;
                    nbNode.f = f;
                    nbNode.parent = current;
                    allNodes.put(nb, nbNode);
                    open.add(nbNode);
                }
            }
        }
        return Collections.emptyList();
    }

    private double heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private void drawPath(List<Point> path) 
    {
        for (int i = 1; i < path.size(); i++) {
            Point a = path.get(i - 1);
            Point b = path.get(i);
//draw line btw pt a,b 
            Line line = new Line(
                    a.x * CELL_SIZE + CELL_SIZE / 2.0,//a.x * cel size give x coordinate but to place the point in mid of cell add extra  length i.e cellsize/2
                    a.y * CELL_SIZE + CELL_SIZE / 2.0,
                    b.x * CELL_SIZE + CELL_SIZE / 2.0,
                    b.y * CELL_SIZE + CELL_SIZE / 2.0
            );
            line.setStroke(Color.GOLD);//set stroke sets the border or outline od shapes life rect,sq,line
            line.setStrokeWidth(2);
            
            root.getChildren().add(line);
    /*  Add line to the visible display list  i.e root is the main 
    window to hold all ui element ,get children list of  allvisible element*/

        }
    }

    private ImageView makeSoldier(Point p, boolean facingRight)
     {
        ImageView soldier = new ImageView(facingRight ? soldierRight : soldierLeft);
        soldier.setFitWidth(CELL_SIZE * 0.6);//soldier fit 60 percent of cell
        soldier.setFitHeight(CELL_SIZE * 0.6);
        soldier.setX(p.x * CELL_SIZE + (CELL_SIZE - soldier.getFitWidth()) / 2.0);//x=5,cellsie=50 ,h=30 so -50 is pixelpos but it will sitck to edge to convert it to center of cell  add -(cellsize-soldier width ie 50-30 that give extra but /2 give excat post)
        soldier.setY(p.y * CELL_SIZE + (CELL_SIZE - soldier.getFitHeight()) / 2.0);
       /* p.x * CELL_SIZE           = 2 * 50 = 100          // Cell left edge
   CELL_SIZE - soldier width = 50 - 30  = 20          // Extra space (20px)
   Offset = 20 / 2           = 10                     // Half on each side
Final X = 100 + 10        = 110                    */
       
        return soldier;
    }

    private void animateSoldier(ImageView soldier, List<Point> path, int delayMs, boolean facingRight)
     {
        soldier.setImage(facingRight ? soldierRight : soldierLeft);

        Path fxPath = new Path();
        fxPath.getElements().add(new MoveTo(
                path.get(0).x * CELL_SIZE + CELL_SIZE / 2.0,
                path.get(0).y * CELL_SIZE + CELL_SIZE / 2.0
        ));

        for (Point pt : path) {
            fxPath.getElements().add(new LineTo(
                    pt.x * CELL_SIZE + CELL_SIZE / 2.0,
                    pt.y * CELL_SIZE + CELL_SIZE / 2.0
            ));
        }

PathTransition transition = new PathTransition(
    Duration.millis(PATH_STEP_DURATION_MS * path.size()),//time
                fxPath,//path to animate 
                soldier//node to animate 
        );
        transition.setDelay(Duration.millis(delayMs));//before they move delay in time of all soldiers so they dont all move at same time
        transition.setInterpolator(javafx.animation.Interpolator.LINEAR);//IMP: interpolator is a class in animation packagedecide the speed of animation over time ,here linear so constant speed 
        transition.play();
    }

    void generateDangerZones() {
        while (dangerZones.size() < (GRID_W * GRID_H * 3 / 10)) {
            int x = rand.nextInt(GRID_W);
            int y = rand.nextInt(GRID_H);
            Point p = new Point(x, y);
            if (!p.equals(source) && !p.equals(target))
                dangerZones.add(p);
        }
    }

    private void drawGridOverlay() {
        for (int y = 0; y < GRID_H; y++) {
            for (int x = 0; x < GRID_W; x++) {
                Rectangle r = new Rectangle(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                r.setFill(Color.TRANSPARENT);//make inside of grid as transparent
                r.setStroke(Color.rgb(0, 0, 0, 0.16));//black color with 16% opacity 
                root.getChildren().add(r);
            }
        }
    }

    private void drawCell(Point p, Color color) {
        Rectangle r = new Rectangle(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        r.setFill(color);
        r.setStroke(Color.BLACK);
        root.getChildren().add(r);
    }

    private void drawDangerZones() 
    {
        for (Point dz : dangerZones) {
            Rectangle r = new Rectangle(dz.x * CELL_SIZE, dz.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);//x,y ,width,height
            r.setFill(Color.rgb(255, 102, 102, 0.5));
            r.setStroke(Color.PINK);
            root.getChildren().add(r);
        }
    }

    private void showMissionSuccess() 
    {
        Label successLabel = new Label("MISSION SUCCESSFUL!");
        successLabel.setTextFill(Color.GOLD);
        successLabel.setFont(new Font("Arial Black", 50));
        successLabel.setLayoutX((CELL_SIZE * GRID_W) / 2.0 - 200);
        successLabel.setLayoutY((CELL_SIZE * GRID_H) / 2.0 - 50);
        root.getChildren().add(successLabel);

        FadeTransition fade = new FadeTransition(Duration.seconds(4), successLabel);
        fade.setFromValue(0);//start from transparent
        fade.setToValue(1);//then becomes clearly  visible 
        fade.play();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
