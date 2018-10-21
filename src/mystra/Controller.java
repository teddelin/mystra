package mystra;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import mystra.datamodel.ActivityDay;
import mystra.datamodel.ActivityItem;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Controller {
    private List<ActivityItem> activityItems;

    @FXML
    private ListView<ActivityItem> activityItemListView;
    @FXML
    private TextArea itemDetailsTextArea;
    @FXML
    private Label deadlineLabel;
    @FXML
    private BorderPane mainBoarderPane;

    public void initialize() {
        activityItemListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ActivityItem>() {
            @Override
            public void changed(ObservableValue<? extends ActivityItem> observableValue, ActivityItem activityItem, ActivityItem t1) {
                if(t1 != null) {
                    handleChangeListView();
                }
            }
        });

        SortedList<ActivityItem> sortedList = new SortedList<>(ActivityDay.getInstance().getActivityItems(),
                Comparator.comparing(ActivityItem::getHourOfDay));

        activityItemListView.setItems(sortedList);
        activityItemListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        activityItemListView.getSelectionModel().selectFirst();

        activityItemListView.setCellFactory(new Callback<ListView<ActivityItem>, ListCell<ActivityItem>>() {
            @Override
            public ListCell<ActivityItem> call(ListView<ActivityItem> activityItemListView) {
                ListCell<ActivityItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(ActivityItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty) {
                            setText(null);
                        } else {
                            int currentHour = LocalDateTime.now().getHour();
                            if(item.getHourOfDay() == currentHour) {
                                setTextFill(Color.RED);
                            } else if(item.getHourOfDay() > currentHour - 2 && item.getHourOfDay() < currentHour + 2 ) {
                                setTextFill(Color.GREEN);
                            }
                            setText(item.getShortDescription());
                        }
                    }
                };
                return cell;
            }
        });
    }

    @FXML
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBoarderPane.getScene().getWindow());
        dialog.setTitle("Add new Todo Item");
        dialog.setHeaderText("Use this dialog to create a new todo item");

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couln't load dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            DialogController controller = fxmlLoader.getController();
            ActivityItem newItem = controller.processResults();
            activityItemListView.getSelectionModel().select(newItem);
        }

    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {
        ActivityItem selectedItem = activityItemListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null) {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                deleteItem(selectedItem);
            }
        }
    }

    private void handleChangeListView() {
        ActivityItem item = activityItemListView.getSelectionModel().getSelectedItem();
        itemDetailsTextArea.setText(item.getDetails());
        deadlineLabel.setText(Integer.toString(item.getHourOfDay()));
    }

    public void deleteItem(ActivityItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Activity");
        alert.setHeaderText("Delete Activity: " + item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm, or cancel.");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && (result.get() == ButtonType.OK)){
            ActivityDay.getInstance().deleteTodoItem(item);
        }
    }
}
