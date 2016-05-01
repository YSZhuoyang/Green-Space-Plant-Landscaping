package Basicsource;

import static Support.Resources.*;

import java.io.File;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * @author Yu Sangzhuoyang
 * @version 6.13
 */
public class ControlPanel extends Application
{
	private Scene scene;
	private TabPane tabPane;
	private Tab plantTab;
	private Tab importModelTab;
	
	private StackPane upperHalf;
	private StackPane bottomHalf;
	private GridPane upperGrid;
	private GridPane bottomGrid;
	private VBox vBoxPlant;
	private VBox vBoxImportModel;
	private Label upperSubtitle;
	private Label bottomSubtitle;
	private Label labelDensity;
	private Label labelPerRow;
	private Label labelColor;
	private Label labelWidth;
	private Label labelCM;
	private Label objPathLabel;
	private Label texPathLabel;
	private ComboBox<String> comboBoxDensity;
	private ComboBox<String> comboBoxColor;
	private ComboBox<String> comboBoxWidth;
	private Button buttons[];
	private Button openObjButton;
	private Button openTexButton;
	private Button importButton;
	
	private FileChooser fileChooser;
	
	public ControlPanel()
	{
		vBoxPlant = new VBox();
		vBoxImportModel = new VBox();
		tabPane = new TabPane();
		plantTab = new Tab();
		importModelTab = new Tab();
		
		upperHalf = new StackPane();
		bottomHalf = new StackPane();
		upperGrid = new GridPane();
		bottomGrid = new GridPane();
		upperSubtitle = new Label();
		bottomSubtitle = new Label();
		labelDensity = new Label();
		labelPerRow = new Label();
		labelColor = new Label();
		labelWidth = new Label();
		labelCM = new Label();
		objPathLabel = new Label();
		texPathLabel = new Label();
		comboBoxDensity = new ComboBox<String>();
		comboBoxColor = new ComboBox<String>();
		comboBoxWidth = new ComboBox<String>();
		buttons = new Button[6];
		openObjButton = new Button();
		openTexButton = new Button();
		importButton = new Button();
		
		fileChooser = new FileChooser();
		
		for (int i = 0; i < 6; i++)
		{
			Image image = new Image(getClass().getResourceAsStream("images/gui/t" + (i + 1) + ".png"));
			ImageView imageView = new ImageView(image);
			imageView.setFitWidth(70);
			imageView.setFitHeight(60);
			
			buttons[i] = new Button();
			buttons[i].setMinSize(60, 60);
			buttons[i].setGraphic(imageView);
		}
		
		setPlantTab();
		setImportModelTab();
		
		scene = new Scene(tabPane, CONTROLPANEL_WIDTH, CONTROLPANEL_HEIGHT);
		scene.getStylesheets().add("Basicsource/style.css");
		
		//launch user interface
		Platform.runLater(() -> 
		{
			try
			{
				start(new Stage());
			}
			catch (Exception e)
			{
				e.printStackTrace();
		    }
		});
	}
	
	public void setPlantTab()
	{
		setUpperPane();
		setBottomPane();
		
		vBoxPlant.getChildren().addAll(upperHalf, bottomHalf);
		
		plantTab.setText("Plants");
		plantTab.setContent(vBoxPlant);
		
		tabPane.getTabs().add(plantTab);
	}
	
	public void setImportModelTab()
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import obj file");
		
		GridPane importGridPane = new GridPane();
		importGridPane.setId("grid");
		openObjButton.setText("open obj file");
		objPathLabel.setText("Path: Empty");
		importGridPane.addRow(0, objPathLabel, openObjButton);
		
		openTexButton.setText("open texture file");
		texPathLabel.setText("Path: Empty");
		importGridPane.addRow(1, texPathLabel, openTexButton);
		
		importButton.setText("Import");
		importGridPane.getChildren().add(importButton);
		GridPane.setConstraints(importButton, 0, 2, 2, 1, HPos.CENTER, VPos.BOTTOM);
		
		Label importLabel = new Label("Import model");
		importLabel.setId("subtitle");
		StackPane importStackPane = new StackPane();
		importStackPane.setId("stackPane");
		StackPane.setAlignment(importLabel, Pos.TOP_CENTER);
		importStackPane.getChildren().addAll(importLabel, importGridPane);
		
		vBoxImportModel.getChildren().add(importStackPane);
		
		importModelTab.setText("Import model");
		importModelTab.setContent(vBoxImportModel);
		
		tabPane.getTabs().add(importModelTab);
	}
	
	public void setButtonEvent(Stage stage)
	{
		buttons[0].setOnAction((ActionEvent event) -> 
		{
			selectedPlant = plantTopologies[0];
			state = State.TREEMODELSELECTED;
		});
		
		buttons[1].setOnAction((ActionEvent event) -> 
		{
			selectedPlant = plantTopologies[1];
			state = State.TREEMODELSELECTED;
		});
		
		buttons[2].setOnAction((ActionEvent event) -> 
		{
			selectedPlant = plantTopologies[2];
			state = State.TREEMODELSELECTED;
		});
		
		buttons[3].setOnAction((ActionEvent event) -> 
		{
			selectedPlant = plantTopologies[3];
			state = State.TREEMODELSELECTED;
		});
		
		buttons[4].setOnAction((ActionEvent event) -> 
		{
			selectedPlant = plantTopologies[4];
			state = State.TREEMODELSELECTED;
		});
		
		buttons[5].setOnAction((ActionEvent event) -> 
		{
			selectedPlant = plantTopologies[5];
			state = State.TREEMODELSELECTED;
		});
		
		openObjButton.setOnAction((ActionEvent e) ->
		{
            File file = fileChooser.showOpenDialog(stage);
            
            if (file != null)
            {
            	openObjFile(file);
            }
		});
		
		openTexButton.setOnAction((ActionEvent e) ->
		{
            File file = fileChooser.showOpenDialog(stage);
            
            if (file != null)
            {
            	openTexFile(file);
            }
		});
		
		importButton.setOnAction((ActionEvent e) -> 
		{
			objFilePath = objPathLabel.getText();
			texFilePath = texPathLabel.getText();
			
			if (!objFilePath.equals("Path: Empty") && !texFilePath.equals("Path: Empty"))
			{
				objPathLabel.setText("Path: Empty");
				texPathLabel.setText("Path: Empty");
				
				state = State.NEWMODELIMPORTED;
			}
		});
	}
	
	private void openObjFile(File file)
	{
		objPathLabel.setText(file.getPath());
	}
	
	private void openTexFile(File file)
	{
		texPathLabel.setText(file.getPath());
	}
	
	public void setComboBoxEvent()
	{
		comboBoxDensity.setOnAction((ActionEvent event) -> 
		{
			density = Integer.parseInt(comboBoxDensity.getValue());
			
			System.out.println("Density: " + comboBoxDensity.getValue());
		});
		
		comboBoxColor.setOnAction((ActionEvent event) -> 
		{
			if (comboBoxColor.getValue().equals("green"))
			{
				color = Color.GREEN;
			}
			else
			{
				color = Color.PURPLE;
			}
			
			System.out.println(comboBoxColor.getValue());
		});
		
		comboBoxWidth.setOnAction((ActionEvent event) -> 
		{
			width = Integer.parseInt(comboBoxWidth.getValue());
			
			System.out.println(comboBoxWidth.getValue());
		});
	}
	
	public void setUpperPane()
	{
		upperSubtitle.setText("Trees");
		upperSubtitle.setId("subtitle");
		
		upperGrid.setId("grid");
		upperGrid.addRow(0, buttons[0], buttons[1], buttons[2]);
		upperGrid.addRow(1, buttons[3], buttons[4], buttons[5]);
		
		upperHalf.setId("stackPane");
		StackPane.setAlignment(upperSubtitle, Pos.TOP_CENTER);
		upperHalf.getChildren().addAll(upperSubtitle, upperGrid);
	}
	
	public void setBottomPane()
	{
		bottomGrid.setId("grid");
		bottomSubtitle.setText("Shrubs");
		bottomSubtitle.setId("subtitle");
		
		labelDensity.setText("Density: ");
		labelDensity.setId("shrub_specification");
		
		comboBoxDensity.setId("density");
		comboBoxDensity.setValue("5");
		comboBoxDensity.getItems().addAll(
				"5", 
				"7", 
				"9"
		);
		
		labelPerRow.setText("per row");
		labelPerRow.setId("text");
		
		labelColor.setText("Color: ");
		labelColor.setId("shrub_specification");
		
		comboBoxColor.setId("color");
		comboBoxColor.setValue("green");
		comboBoxColor.getItems().addAll(
				"green", 
				"purple"
		);
		
		labelWidth.setText("Width: ");
		labelWidth.setId("shrub_specification");
		
		comboBoxWidth.setId("width");
		comboBoxWidth.setValue("60");
		comboBoxWidth.getItems().addAll(
				"60", 
				"70", 
				"80", 
				"90"
		);
		
		labelCM.setText("cm");
		labelCM.setId("text");
		
		bottomGrid.addRow(0, labelDensity, comboBoxDensity, labelPerRow);
		bottomGrid.addRow(1, labelColor, comboBoxColor);
		bottomGrid.addRow(2, labelWidth, comboBoxWidth, labelCM);
		
		bottomHalf.setId("stackPane");
		StackPane.setAlignment(bottomSubtitle, Pos.TOP_CENTER);
		bottomHalf.getChildren().addAll(bottomSubtitle, bottomGrid);
	}
	
	public void start(Stage pStage) throws Exception
	{
		setButtonEvent(pStage);
		setComboBoxEvent();
		
		pStage.setTitle(TITLE);
		pStage.setScene(scene);
		pStage.setX(CONTROLPANEL_LOCATION_X);
		pStage.setY(CONTROLPANEL_LOCATION_Y);
		pStage.show();
	}
}