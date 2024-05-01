package com.medialab.multimedia;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML
    private TextField scenarioField;
    @FXML
    private ChoiceBox<String> levelChoice;
    @FXML
    private ChoiceBox<String> supermineChoice;
    @FXML
    private TextField minesField;
    @FXML
    private TextField timeField;
    @FXML
    private TextField loadField;
    @FXML
    private Button createButton;
    @FXML
    private Button loadButton;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Label timeLabel;
    @FXML
    private Label flagLabel;
    @FXML
    private Label roundLabel;
    @FXML
    private Label minesLabel;

    private static int clock;
    private static boolean winner=false;
    private static int level;
    private static int mines;
    private static int supermine;
    private static int win;
    private static int tracker;
    private static int clicks;
    private static int totaltime;
    private static boolean level1=false;
    private static int W = 360;
    private static int H = 360;

    private static double getTileSize() {
        return ((level1) ? 40.0 : 22.5);
    }
    private static int getXtiles() {
        return (int) (W / getTileSize());
    }

    private static int getYtiles() {
        return (int) (H / getTileSize());
    }

    private static Tile[][] grid = new Tile[getXtiles()][getYtiles()];
    private String[] check= {" "," "," "," "};
    private String[] rounds= {" "," "," "," "," "};
    private int counter = 0; //red flag counter
    private static int frz = 0; //flag that freezes the buttons when user looses


    private Parent createGame() throws IOException {
        clicks=0;
        tracker=0;
        Pane root = new Pane();
        root.setPrefSize(W, H);
        if (level1) {
            win=81-mines;
        }
        else {
            win=256-mines;
        }

        frz = 0;
        String minesfile="";
        int count1 = 0;
        int count2 = 0;
        if(supermine==0) {
            count2=2;
        }


        for (int y = 0; y < getYtiles(); y++) {
            for (int x = 0; x < getXtiles(); x++) {
                boolean randomValue1 = (count1 < mines) && (Math.random() < 0.2);
                boolean randomValue2 = (count2 < 1) && (Math.random() < 0.1) && (randomValue1);

                Tile tile = new Tile(x, y, randomValue1,randomValue2);
                if (randomValue1) {
                    minesfile=minesfile + y+","+x+",";
                    count1++;
                    if (randomValue2) {
                        minesfile= minesfile+"1"+"\n";
                        count2++;
                    }
                    else {
                        minesfile=minesfile+"0"+"\n";
                    }
                }

                grid[x][y] = tile;
                root.getChildren().add(tile);
            }
        }

        for (int y = 0; y < getYtiles(); y++) {
            for (int x = 0; x < getXtiles(); x++) {
                Tile tile = grid[x][y];

                if (tile.hasMine)
                    continue;

                long bombs = getNeighbors(tile).stream().filter(t -> t.hasMine).count();

                if (bombs > 0) {
                    tile.text.setText(String.valueOf(bombs));
                }

            }
        }
        String filePath = "src/main/resources/medialab/mines.txt";
        File file = new File(filePath);
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);

        String[] lines = minesfile.split("\n");
        for (String line : lines) {
            bw.write(line);
            bw.newLine();
        }

        bw.close();
        fw.close();
        return root;
    }
    private Parent solution() {
        frz=1;
        Pane solutionPane = new Pane();
        solutionPane.setPrefSize(W, H);

        for (int y = 0; y < getYtiles(); y++) {
            for (int x = 0; x < getXtiles(); x++) {
                Tile tile = grid[x][y];
                tile.text.setVisible(true);
                tile.border.setFill(null);
                solutionPane.getChildren().add(tile);
            }
        }

        return solutionPane;
    }
    private class Tile extends StackPane {
        private int x, y;
        private boolean hasMine,hasSuperMine;
        private boolean isOpen = false;
        private int flag;

        private Rectangle border = new Rectangle(getTileSize() - 2, getTileSize() - 2);
        private Text text = new Text();

        /**
         * Tile constructor
         * Sets text of each tile
         * Sets the methods executed on left and right mouse-click
         *
         * @param x is the column
         * @param y is the row
         * @param hasMine shows if it has Mine
         * @param hasSuperMine shows if Mine is Supermine
         */
        public Tile(int x, int y, boolean hasMine, boolean hasSuperMine) {
            this.x = x;
            this.y = y;
            this.hasMine = hasMine;
            this.hasSuperMine = hasSuperMine;
            this.flag=0;
            border.setStroke(Color.LIGHTGRAY);

            text.setFont(Font.font(18));
            text.setText(hasMine ? "X" : "");
            if(hasSuperMine){
                text.setText("S");
                text.setFill(Color.PURPLE);
            }
            text.setVisible(false);

            getChildren().addAll(border, text);

            setTranslateX(x * getTileSize());
            setTranslateY(y * getTileSize());

            setOnMouseClicked(e -> {
                if(frz==1)return;
                if (e.getButton() == MouseButton.SECONDARY) {
                    flag();
                }
                else {
                    if(!hasMine){
                        clicks++;
                    }
                    try {
                        open();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }

        /**
         * Method that marks and un-marks the tile as flag on right click
         * If tile has supermine and the player has successfully tried up to 4 times it reveals the row and column of the tile
         *
         */
        public void flag() {
            if (this.flag==0 && counter<mines) {
                if(this.hasSuperMine && clicks<5){
                    getRowCol(this).forEach(tile -> {
                        if (tile.isOpen) return;
                        if (tile.flag==1) {
                            counter--;
                            flagLabel.setText(Integer.toString(counter));
                        }
                        tile.isOpen = true;
                        tile.text.setVisible(true);
                        tile.border.setFill(null);
                        if (!tile.hasMine) {tracker++;}
                        if (tile.hasMine && !(tile.hasSuperMine)){
                            tile.text.setFill(Color.RED);
                        }
                    });
                }
                border.setFill(Color.RED);
                counter++;
                flagLabel.setText(Integer.toString(counter));
                this.flag=1;
            }
            else if (this.flag==1) {
                border.setFill(Color.BLACK);
                counter--;
                flagLabel.setText(Integer.toString(counter));
                this.flag=0;
            }
        }

        /**
         * Method that opens the tile on left click
         * If tile has mine the player loses and the game is over
         * If the tile doesn't have mine the tile is revealed
         * If the tile is empty the neighbors are also revealed
         * If all tiles have been opened the player wins
         * @throws IOException
         * Failed to load fxml file for scene
         */
        public void open() throws IOException {
            if (isOpen)
                return;

            if (hasMine) {
                gameover();
                registerRound();
                return;
            }
            if (flag==1){
                counter--;
                flagLabel.setText(Integer.toString(counter));
            }

            isOpen = true;
            text.setVisible(true);
            border.setFill(null);
            tracker++;
            if (tracker==win){
                frz=1;
                winner=true;
                System.out.println("You won");
                Stage popupStage = new Stage();
                VBox popupLayout = new VBox();
                Label popupLabel = new Label("YOU WON!");
                popupLayout.getChildren().add(popupLabel);
                popupLayout.setAlignment(Pos.CENTER);
                Scene popupScene = new Scene(popupLayout,300,200);
                popupStage.setTitle("Congratulations");
                popupStage.setScene(popupScene);
                popupStage.show();
                registerRound();
                return;
            }

            if (text.getText().isEmpty()) {
                getNeighbors(this).forEach(tile -> {
                    try {
                        tile.open();
                    } catch (Exception e) {
                        // do nothing
                    }
                });
            }
        }
    }
    private List<Tile> getNeighbors(Tile tile) {
        List<Tile> neighbors = new ArrayList<>();

        int[] points = new int[]{
                -1, -1,
                -1, 0,
                -1, 1,
                0, -1,
                0, 1,
                1, -1,
                1, 0,
                1, 1
        };

        for (int i = 0; i < points.length; i++) {
            int dx = points[i];
            int dy = points[++i];

            int newX = tile.x + dx;
            int newY = tile.y + dy;

            if (newX >= 0 && newX < getXtiles()
                    && newY >= 0 && newY < getYtiles()) {
                neighbors.add(grid[newX][newY]);
            }
        }

        return neighbors;
    }

    private List<Tile> getRowCol(Tile tile) {
        List<Tile> rowcol = new ArrayList<>();

            int newX = tile.x;
            int newY = tile.y;

            for (int i =0; i < getYtiles(); i++) {
                rowcol.add(grid[newX][i]);
            }
            for (int i =0; i < getXtiles(); i++) {
                rowcol.add(grid[i][newY]);
            }
            rowcol.remove(tile);

        return rowcol;
    }

    /**
     * Method that creates .txt file and saves it to medialab designated folder
     * Gets text from the texts fields of the fxml
     * Writes text to the file
     *
     * @param actionEvent on-click button event that triggers the method
     */
    public void createFile(ActionEvent actionEvent) {
        String fileName = scenarioField.getText();
        String mines = minesField.getText() + "\n";
        String time = timeField.getText() + "\n";
        String level = levelChoice.getValue() + "\n";
        String supermine = supermineChoice.getValue();
        String filePath = "src/main/resources/medialab/" + fileName + ".txt";

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(level);
            writer.write(mines);
            writer.write(time);
            writer.write(supermine);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Method that loads .txt file from medialab designated folder
     * Reads the file and passes its content to the values: level,mines,clock,supermine that are needed for the game to start
     *
     * @param actionEvent on-click button event that triggers the method
     * @throws InvalidDescriptionException
     * File doesn't have 4 lines
     * @throws InvalidValueException
     * The values in the file are out of range
     */
    public void loadFile(ActionEvent actionEvent) throws InvalidDescriptionException,InvalidValueException {
        frz=1;
        int i=0;
        String fileName = loadField.getText();
        StringBuilder sb = new StringBuilder();
        String path = "src/main/resources/medialab/" + fileName +".txt";
        Path filePath = Paths.get(path);
        if (Files.exists(filePath)) {
            try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(path).toAbsolutePath().toString()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    check[i]=line;
                    if (line.isEmpty()) {
                        throw new InvalidDescriptionException();
                    }
                    i++;
                    sb.append(line).append("\n");
                }
                level=Integer.parseInt(check[0]);
                mines=Integer.parseInt(check[1]);
                clock=Integer.parseInt(check[2]);
                totaltime=Integer.parseInt(check[2]);
                supermine=Integer.parseInt(check[3]);
                if(level==1){
                    level1=true;
                    if(mines<9 || mines>11){throw new InvalidValueException();}
                    else if(clock<120 || clock>180){throw new InvalidValueException();}
                    else if(supermine==1){throw new InvalidValueException();}
                }
                if(level==2){
                    level1=false;
                    if(mines<35 || mines>45){throw new InvalidValueException();}
                    else if(clock<240 || clock>360){throw new InvalidValueException();}
                    else if(supermine==0){throw new InvalidValueException();}
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else {
            Stage popupStage = new Stage();
            VBox popupLayout = new VBox();
            Label popupLabel = new Label("ERROR:There is no file with this Scenario ID.");
            popupLayout.getChildren().add(popupLabel);
            popupLayout.setAlignment(Pos.CENTER);
            Scene popupScene = new Scene(popupLayout,300,200);
            popupStage.setTitle("Invalid Scenario ID");
            popupStage.setScene(popupScene);
            popupStage.show();
        }
        Stage stage = (Stage) loadButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Method that saves the last 5 rounds from most to least recent to the file rounds in medialab designated folder
     * Reads file named rounds
     * Overwrites file starting with the most recent and then the 4 older rounds, discarding the 5th oldest.
     *
     * @throws IOException
     * Failed to load fxml file for scene
     */
    public void registerRound() throws IOException {
        int i=0;
        StringBuilder sb = new StringBuilder();
        String path = "src/main/resources/medialab/rounds";
            try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(path).toAbsolutePath().toString()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    rounds[i] = line;
                    if (line.isEmpty()) {
                        throw new InvalidDescriptionException();
                    }
                    i++;
                    sb.append(line).append("\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            String won=(winner ?"User":"Computer");
            String curround="Mines: "+mines+", Clicks: "+clicks+", Time: "+totaltime+", Winner: "+won;
        File file = new File(path);
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        if (i<5) {
            bw.write(curround);
            bw.newLine();
            for (int j = 0; j < i; j++) {
                bw.write(rounds[j]);
                bw.newLine();
            }
        }
        else{
            bw.write(curround);
            bw.newLine();
            for (int j = 0; j < 4; j++) {
                bw.write(rounds[j]);
                bw.newLine();
            }
        }
        bw.close();
        fw.close();
    }

    /**
     * Method that triggers a pop-up window for game over
     *
     */
    public void gameover(){
        frz=1;
        System.out.println("Game Over");
        Stage popupStage = new Stage();
        VBox popupLayout = new VBox();
        Label popupLabel = new Label("GAME OVER");
        popupLayout.getChildren().add(popupLabel);
        popupLayout.setAlignment(Pos.CENTER);
        Scene popupScene = new Scene(popupLayout,300,200);
        popupStage.setTitle("Game Over");
        popupStage.setScene(popupScene);
        popupStage.show();
    }

    /**
     * Method that triggers a pop-up window for "create" asset of the menu bar
     *
     *@param actionEvent on-click button event that triggers the method
     *@throws IOException
     *Failed to load fxml file for scene
     */

    public void create(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("CreatePopup.fxml"));
        Parent root=fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("Create Scenario");
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Method that triggers a pop-up window for "load" asset of the menu bar
     *
     *@param actionEvent on-click button event that triggers the method
     *@throws IOException
     *Failed to load fxml file for scene
     */
    public void load(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("LoadPopup.fxml"));
        Parent root=fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("Load Scenario");
        stage.setScene(new Scene(root));
        stage.show();
    }
    /**
     * Method that terminates the application for "exit" asset of the menu bar
     *
     *@param actionEvent on-click button event that triggers the method
     */
    public void exit(ActionEvent actionEvent) {
        System.exit(0);
    }

    /**
     * Method for "solution" asset of the menu bar
     * Loads and shows the solution of current game in a new scene
     *
     *@param actionEvent on-click button event that triggers the method
     *@throws IOException
     *Failed to load fxml file for scene
     */
    public void solution(ActionEvent actionEvent) throws IOException {
        registerRound();
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("TopScreen.fxml"));
        Scene scene1 = new Scene(solution());
        Scene scene2 = new Scene(fxmlLoader.load(), 320, 240);
        VBox root = new VBox();
        root.getChildren().addAll(scene2.getRoot(), scene1.getRoot());
        flagLabel = (Label) fxmlLoader.getNamespace().get("flagLabel");

        Scene combinedScene = new Scene(root, 360, 430);

        Stage stage = (Stage) menuBar.getScene().getWindow();
        stage.setTitle("MediaLab MineSweeper");
        stage.setScene(combinedScene);
    }

    /**
     * Method that triggers a pop-up window for "rounds" asset of the menu bar
     * Displays content of "rounds" file
     *
     *@param actionEvent on-click button event that triggers the method
     *@throws IOException
     *Failed to load fxml file for scene
     */
    public void rounds(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("RoundsPopup.fxml"));
        Parent root=fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("Rounds");
        stage.setScene(new Scene(root,600,300));
        stage.show();
        String path = "src/main/resources/medialab/rounds";
        roundLabel = (Label) fxmlLoader.getNamespace().get("roundLabel");
        try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(path).toAbsolutePath().toString()))) {
            String content = "";
            String line;
            while ((line = br.readLine()) != null) {
                content += line + "\n";
            }
            roundLabel.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that starts the game for "start" asset of the menu bar
     * Sets off the timer of the scene
     *
     *@param actionEvent on-click button event that triggers the method
     *@throws IOException
     *Failed to load fxml file for scene
     */
    public void start(ActionEvent actionEvent) throws IOException {
        frz=0;
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("TopScreen.fxml"));
        Scene scene1 = new Scene(createGame());
        Scene scene2 = new Scene(fxmlLoader.load(), 320, 240);
        VBox root = new VBox();
        root.getChildren().addAll(scene2.getRoot(), scene1.getRoot());
        flagLabel = (Label) fxmlLoader.getNamespace().get("flagLabel");
        minesLabel = (Label) fxmlLoader.getNamespace().get("minesLabel");
        minesLabel.setText(Integer.toString(mines));

        Scene combinedScene = new Scene(root, 360, 432);

        Stage stage = (Stage) menuBar.getScene().getWindow();
        stage.setTitle("MediaLab MineSweeper");
        stage.setScene(combinedScene);
        timeLabel = (Label) fxmlLoader.getNamespace().get("timeLabel");
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), event -> {
            if(frz==1){
                timeline.stop();
            }
            if(frz==0 && clock > 0) {
                clock--;
            }
            if (clock == 0) {
                gameover();
                timeline.stop();
                try {
                    registerRound();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(frz==0) {
                timeLabel.setText(Integer.toString(clock));
            }
        }));
        timeline.play();

    }
}