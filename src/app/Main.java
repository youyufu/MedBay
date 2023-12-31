package app;

import data_access.MedicineAPICallsInterface;
import data_access.MedicineAPICallsObject;
import data_access.MedicineDAO;
import data_access.MedicineDataAccessInterface;
import interface_adapter.switchView.SwitchViewController;
import interface_adapter.table.TableViewModel;
import interface_adapter.ViewManagerModel;
import interface_adapter.checklistChecked.ChecklistViewModel;
import interface_adapter.deleteMedicine.DeleteViewModel;
import interface_adapter.enterMedicine.EnterViewModel;
import view.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * The class from which MedBay is run.
 */
public class Main {

    /**
     * Creates and runs MedBay.
     * @param args, arguments for running the program.
     */
    public static void main(String[] args) {
        JFrame application = new JFrame("MedBay");
        application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        CardLayout cardLayout = new CardLayout();
        JPanel views = new JPanel(cardLayout);
        application.add(views);
        ViewManagerModel viewManagerModel = new ViewManagerModel();
        new ViewManager(views, cardLayout, viewManagerModel);
        EnterViewModel enterViewModel = new EnterViewModel();
        DeleteViewModel deleteViewModel = new DeleteViewModel();
        TableViewModel tableViewModel = new TableViewModel();
        ChecklistViewModel checklistViewModel = new ChecklistViewModel();
        LocalDate localDate = LocalDate.now();
        MedicineDataAccessInterface medicineDAO = MedicineDAO.getMedicineDAO(localDate, "./medicine.json");
        SwitchViewController switchViewController = SwitchViewUseCaseFactory.create(viewManagerModel, checklistViewModel);
        MainView mainView = new MainView(switchViewController);
        MedicineAPICallsInterface medicineAPICallsObject = new MedicineAPICallsObject();
        EnterView enterView = EnterUseCaseFactory.create(switchViewController, enterViewModel, checklistViewModel, tableViewModel, viewManagerModel, medicineDAO, medicineAPICallsObject);
        DeleteView deleteView = DeleteUseCaseFactory.create(switchViewController, deleteViewModel, checklistViewModel, tableViewModel, viewManagerModel, medicineDAO);
        TableView tableView = TableViewFactory.create(switchViewController, tableViewModel, medicineDAO, medicineAPICallsObject);
        ChecklistView checklistView = ChecklistUseCaseFactory.create(switchViewController, checklistViewModel, tableViewModel, medicineDAO);
        views.add(mainView, MainView.viewName);
        views.add(enterView, EnterView.viewName);
        views.add(deleteView, DeleteView.viewName);
        views.add(tableView, TableView.viewName);
        views.add(checklistView, ChecklistView.viewName);
        viewManagerModel.setActiveView(MainView.viewName);
        viewManagerModel.firePropertyChanged();
        application.pack();
        application.setVisible(true);
        checklistViewModel.firePropertyChanged();
    }
}
