package com.awplab.core.admin.provider;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Created by andyphillips404 on 8/12/16.
 */
public class ValoMenuLayout extends HorizontalLayout {

    CssLayout contentArea = new CssLayout();

    CssLayout menuArea = new CssLayout();

    public ValoMenuLayout() {
        setSizeFull();

        menuArea.setPrimaryStyleName(ValoTheme.MENU_ROOT);

        contentArea.setPrimaryStyleName("valo-content");
        contentArea.addStyleName("v-scrollable");
        contentArea.setSizeFull();

        addComponents(menuArea, contentArea);
        setExpandRatio(contentArea, 1);
    }

    public ComponentContainer getContentContainer() {
        return contentArea;
    }

    public void addMenu(Component menu) {
        menu.addStyleName(ValoTheme.MENU_PART);
        menuArea.addComponent(menu);
    }
}
