package com.awplab.core.admin;

import com.awplab.core.vaadin.service.VaadinProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;

import java.util.Optional;

/**
 * Created by andyphillips404 on 8/12/16.
 */
public abstract class AdminView extends VerticalLayout implements View {

    final private String navigatorViewName;

    public AdminView(String navigatorViewName) {
        this.navigatorViewName = navigatorViewName;
    }

    public String getNavigatorViewName() {
        return navigatorViewName;
    }



    private Button menuButton;

    public Button getMenuButton() {
        return menuButton;
    }

    public void setMenuButton(Button menuButton) {
        this.menuButton = menuButton;
    }


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }

    public abstract String getMenuTitle();


    public Optional<Integer> getPositionInCategory() {
        return Optional.empty();
    }

    public Optional<String> getCategory() {
        return Optional.empty();
    }


    public Optional<Resource> getMenuIcon() {
        return Optional.empty();
    }

    public Optional<String> getMenuBadge() {
        return Optional.empty();
    }

    public void updateMenuButton() {

        VaadinProvider.doAccess(getUI(), () -> {
            if (getMenuBadge().isPresent()) {
                menuButton.setCaption(getMenuTitle() + "<span class=\"valo-menu-badge\">" + getMenuBadge().get() + "</span>");
            }
            else {
                menuButton.setCaption(getMenuTitle());
            }
            if (getMenuIcon().isPresent()) menuButton.setIcon(getMenuIcon().get());
        });
    }
}
