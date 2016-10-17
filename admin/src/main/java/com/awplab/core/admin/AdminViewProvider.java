package com.awplab.core.admin;

import com.awplab.core.vaadin.service.VaadinProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

import javax.security.auth.Subject;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by andyphillips404 on 8/10/16.
 */
public abstract class AdminViewProvider implements ViewProvider {


    @Override
    public String getViewName(String viewAndParameters) {
        if (null == viewAndParameters) {
            return null;
        }
        if (viewAndParameters.equals(getName())
                || viewAndParameters.startsWith(getName() + "/")) {
            return getName();
        }
        return null;
    }

    public abstract String getName();


    @Override
    public View getView(String viewName) {
        if (getName().equals(viewName)) {
            currentView = createView(((AdminUI)UI.getCurrent()).getSubject());
            return currentView;
        }
        return null;
    }

    private View currentView;

    public View getCurrentView() {
        return currentView;
    }

    protected abstract View createView(Subject subject);


    private Button menuButton;

    public Button getMenuButton() {
        return menuButton;
    }

    public void setMenuButton(Button menuButton) {
        this.menuButton = menuButton;
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

    public <T extends Component> void doAccessCurrentView(Consumer<T> consumer, final Class<T> viewClass) {
        T currentView = getCurrentView(viewClass);
        if (currentView != null && currentView.getUI() != null) {
            VaadinProvider.doAccess(currentView.getUI(), new Runnable() {
                @Override
                public void run() {
                    consumer.accept(getCurrentView(viewClass));
                }
            });
        }
    }

    public <T extends Component> T getCurrentView(Class<T> viewClass) {
        if (getCurrentView() != null && viewClass.isAssignableFrom(getCurrentView().getClass())) {
            return (T) getCurrentView();
        }

        return null;
    }

    public void pushUpdateMenuButton() {
        if (getMenuButton() != null && getMenuButton().getUI() != null) {
            VaadinProvider.doAccess(getMenuButton().getUI(), this::updateMenuButton);
        }
    }

    public void updateMenuButton() {
        if (getMenuButton() != null) {
            if (this.getMenuBadge().isPresent()) {
                getMenuButton().setCaption(getMenuTitle() + "<span class=\"valo-menu-badge\">" + getMenuBadge().get() + "</span>");
            } else {
                getMenuButton().setCaption(getMenuTitle());
            }
            if (this.getMenuIcon().isPresent()) getMenuButton().setIcon(this.getMenuIcon().get());
        }
    }

    public static String jacksonHtml(ObjectMapper objectMapper, Object object, boolean stripOpeningBrackets, int indent) throws JsonProcessingException {
        String ret = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        if (stripOpeningBrackets) ret = ret.replaceAll("(?s)^(\\{|\\[)(\n )(.*?)(\n)(\\}|\\])$", "$3").trim();
        String indentStr = "";
        if (indent > 0) {
            indentStr = new String(new char[indent]).replace("\0", "&nbsp;");
        }
        return  indentStr + ret.trim().replaceAll("[,]?\n" + (stripOpeningBrackets ? "  " : ""), "<br>"+indentStr).replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").replaceAll(" ", "&nbsp;");


    }
}
