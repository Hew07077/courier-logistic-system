package admin.management;

import javax.swing.*;

public interface ManagementModule {
    JPanel getMainPanel();
    JPanel getRefreshedPanel();
    void refreshData();
    int getTotalCount();
}