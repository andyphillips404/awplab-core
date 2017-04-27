package com.awplab.core.admin;

import com.awplab.core.admin.provider.ValoMenuLayout;
import com.awplab.core.vaadin.service.BasicAuthRequired;
import com.awplab.core.vaadin.service.VaadinProvider;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
//import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.ipojo.annotations.Component;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.util.*;

/**
 * Created by andyphillips404 on 8/12/16.
 */

@Push
@PreserveOnRefresh
@Component(immediate = true, managedservice = AdminUIConfiguration.CONFIG_MANAGED_SERVICE_NAME)
@Theme("valo")
public class AdminUI extends UI {


    ValoMenuLayout root = new ValoMenuLayout();

    ComponentContainer viewDisplay = root.getContentContainer();

    CssLayout menu = new CssLayout();

    CssLayout menuItemsLayout = new CssLayout();
    {
        menu.setId("testMenu");
    }

    private Navigator navigator;


    private void doAccess(Runnable runnable) {
        VaadinProvider.doAccess(this, runnable);
    }

    @Property(name = AdminUIConfiguration.PROPERTY_TITLE, value = "Admin Portal")
    private String title;

    @Property(name = AdminUIConfiguration.PROPERTY_CATEGORIES)
    private String[] categories;


    @Updated
    private void update() {
    }


    private Subject subject;


    private Map<AdminProvider, AdminViewProvider> providers = Collections.synchronizedMap(new HashMap<>());

    @Bind(optional = true, aggregate = true)
    private void bindAdminViewProvider(AdminProvider adminProvider) {
        AdminViewProvider viewProvider = adminProvider.createViewProvider(this);
        if (providers.values().stream().filter(adminViewProvider -> {return adminViewProvider.getName().equals(viewProvider.getName());}).findAny().isPresent()) {
            LoggerFactory.getLogger(AdminUI.class).error("Unable to add provider, duplicate name: " + viewProvider.getName());
            return;
        }

        providers.put(adminProvider, adminProvider.createViewProvider(this));
        if (navigator != null) {
            navigator.addProvider(providers.get(adminProvider));
            doAccess(this::updateMenuAndNavigator);
        }
    }

    @Unbind(optional = true, aggregate = true)
    private void unbindAdminViewProvider(AdminProvider adminProvider) {
        AdminViewProvider adminViewProvider = providers.remove(adminProvider);
        if (adminViewProvider == null) return;

        if (navigator != null) {
            navigator.removeProvider(adminViewProvider);
            doAccess(this::updateMenuAndNavigator);
        }
    }


    public Subject getSubject() {
        return subject;
    }

    private void updateMenuAndNavigator() {

        if (navigator == null) return;


        String selectedViewName = navigator.getState(); //(navigator.getCurrentView() != null && !(navigator.getCurrentView() instanceof NoAdminErrorView)) ? ((AdminViewProvider) navigator.getCurrentView()).getName() : null;
        boolean foundSelection = false;

        ArrayList<AdminViewProvider> viewProviders = new ArrayList<AdminViewProvider>(providers.values());
        viewProviders.sort((o1, o2) -> {

            int o1CatPos = o1.getCategory().isPresent() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            int o2CatPos = o2.getCategory().isPresent() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            if (categories != null && categories.length > 0) {
                if (o1.getCategory().isPresent() && Arrays.asList(categories).contains(o1.getCategory().get())) {
                    o1CatPos = Arrays.asList(categories).indexOf(o1.getCategory().get());
                }
                if (o2.getCategory().isPresent() && Arrays.asList(categories).contains(o2.getCategory().get())) {
                    o2CatPos = Arrays.asList(categories).indexOf(o2.getCategory().get());
                }
            }

            int compare = Integer.compare(o1CatPos, o2CatPos);
            if (compare != 0) return compare;

            compare = o1.getCategory().orElse(" ").compareTo(o2.getCategory().orElse(" "));
            if (compare != 0) return compare;

            return Integer.compare(o1.getPositionInCategory().orElse(Integer.MAX_VALUE), o2.getPositionInCategory().orElse(Integer.MAX_VALUE));

        });

        menuItemsLayout.removeAllComponents();

        Label label = null;
        String lastCategory = null;
        for (AdminViewProvider adminViewProvider : viewProviders) {
            if (adminViewProvider.getCategory().isPresent() &&
                    (lastCategory == null || !adminViewProvider.getCategory().get().equals(lastCategory))) {

                label = new Label(adminViewProvider.getCategory().get(), ContentMode.HTML);
                label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
                label.addStyleName(ValoTheme.LABEL_H4);
                label.setSizeUndefined();
                menuItemsLayout.addComponent(label);
                lastCategory = adminViewProvider.getCategory().get();
            }

            Button b = new Button();
            b.addClickListener((Button.ClickListener) event -> navigator.navigateTo(adminViewProvider.getName()));

            adminViewProvider.setMenuButton(b);
            adminViewProvider.updateMenuButton();
            b.setHtmlContentAllowed(true);
            b.setPrimaryStyleName(ValoTheme.MENU_ITEM);
            if (selectedViewName != null && selectedViewName.equals(adminViewProvider.getName())) {
                foundSelection = true;
                b.addStyleName("selected");
            }
            menuItemsLayout.addComponent(b);

        }

        /*
        if (viewProviders.size() > 0) {
            String f = Page.getCurrent().getUriFragment();
            if ((selectedViewName != null && !foundSelection) || (f == null || f.equals(""))) {
                navigator.navigateTo(viewProviders.get(0).getName());
            }
        }
        */

    }


    private class NoAdminErrorView extends HorizontalLayout implements View {
        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {


        }

        public NoAdminErrorView() {
            Label error = new Label("(Please select an area from the left)");

            error.setSizeFull();

            this.addComponent(error);
        }
    }

    @Override
    protected void init(VaadinRequest request) {

        subject = BasicAuthRequired.getSubject(request).orElse(null);

        Responsive.makeResponsive(this);

        setContent(root);
        root.setWidth("100%");

        addStyleName(ValoTheme.UI_WITH_MENU);

        navigator = new Navigator(this, viewDisplay);

        navigator.addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {
                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {

                for (AdminViewProvider adminView : providers.values()) {
                    if (adminView == null) continue;

                    if (adminView.getName().equals(event.getViewName())) {
                        adminView.getMenuButton().addStyleName("selected");
                    }
                    else {
                        adminView.getMenuButton().removeStyleName("selected");
                    }
                }

            }
        });

        navigator.setErrorView(new NoAdminErrorView());

        for (AdminViewProvider adminViewProvider : providers.values()) {
            navigator.addProvider(adminViewProvider);
        }

        root.addMenu(buildMenu());

        //updateMenuAndNavigator();
    }

    private CssLayout buildMenu() {

        HorizontalLayout top = new HorizontalLayout();
        top.setWidth("100%");
        top.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        top.addStyleName(ValoTheme.MENU_TITLE);
        menu.addComponent(top);

        Button showMenu = new Button("Menu", (Button.ClickListener) event -> {
            if (menu.getStyleName().contains("valo-menu-visible")) {
                menu.removeStyleName("valo-menu-visible");
            } else {
                menu.addStyleName("valo-menu-visible");
            }
        });
        showMenu.addStyleName(ValoTheme.BUTTON_PRIMARY);
        showMenu.addStyleName(ValoTheme.BUTTON_SMALL);
        showMenu.addStyleName("valo-menu-toggle");
        showMenu.setIcon(FontAwesome.LIST);
        menu.addComponent(showMenu);

        Label title = new Label("<h3>" + this.title + "</h3>",
                ContentMode.HTML);
        title.setSizeUndefined();
        top.addComponent(title);
        top.setExpandRatio(title, 1);

        menuItemsLayout.setPrimaryStyleName("valo-menuitems");
        menu.addComponent(menuItemsLayout);

        updateMenuAndNavigator();

        return menu;
    }

}
