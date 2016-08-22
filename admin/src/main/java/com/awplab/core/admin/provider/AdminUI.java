package com.awplab.core.admin.provider;

import com.awplab.core.admin.AdminUIConfiguration;
import com.awplab.core.admin.AdminView;
import com.awplab.core.admin.AdminViewProvider;
import com.awplab.core.vaadin.service.BasicAuthRequired;
import com.awplab.core.vaadin.service.VaadinProvider;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.ipojo.annotations.Component;

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

    @Property(name = AdminUIConfiguration.PROPERTY_TITLE)
    private String title;

    @Property(name = AdminUIConfiguration.PROPERTY_CATEGORIES)
    private String[] categories;


    @Updated
    private void update() {
        updateMenuAndNavigator();
    }


    private Subject subject;


    private Map<AdminViewProvider, AdminView> providers = Collections.synchronizedMap(new HashMap<>());

    @Bind(optional = true, aggregate = true)
    private void bindAdminViewProvider(AdminViewProvider adminViewProvider) {
        providers.put(adminViewProvider, null);
        updateMenuAndNavigator();
    }

    @Unbind(optional = true, aggregate = true)
    private void unbindAdminViewProvider(AdminViewProvider adminViewProvider) {
        AdminView adminView = providers.remove(adminViewProvider);
        if (navigator != null) navigator.removeView(adminView.getNavigatorViewName());
        updateMenuAndNavigator();
    }




    private void updateMenuAndNavigator() {
        if (navigator == null) return;


        doAccess(() -> {

            String selectedViewName = navigator.getCurrentView() != null ? ((AdminView)navigator.getCurrentView()).getNavigatorViewName() : null;
            boolean foundSelection = false;

            HashMap<AdminViewProvider, AdminView> updates = new HashMap<>();
            for (AdminViewProvider viewProvider : providers.keySet()) {
                AdminView view = providers.get(viewProvider);
                if (view == null) {
                    view = viewProvider.createView(subject);
                    updates.put(viewProvider, view);
                    navigator.addView(view.getNavigatorViewName(), view);
                }
            }
            providers.putAll(updates);

            ArrayList<AdminView> views = new ArrayList<AdminView>(providers.values());
            views.sort((o1, o2) -> {

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
            for (AdminView adminView : views) {
                if (adminView.getCategory().isPresent() &&
                        (lastCategory == null || !adminView.getCategory().get().equals(lastCategory))) {

                    label = new Label(adminView.getCategory().get(), ContentMode.HTML);
                    label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
                    label.addStyleName(ValoTheme.LABEL_H4);
                    label.setSizeUndefined();
                    menuItemsLayout.addComponent(label);
                    lastCategory = adminView.getCategory().get();
                }

                Button b = new Button();
                b.addClickListener((Button.ClickListener) event -> navigator.navigateTo(adminView.getNavigatorViewName()));

                adminView.setMenuButton(b);
                adminView.updateMenuButton();
                b.setHtmlContentAllowed(true);
                b.setPrimaryStyleName(ValoTheme.MENU_ITEM);
                if (selectedViewName != null && selectedViewName.equals(adminView.getNavigatorViewName())) {
                    foundSelection = true;
                    b.addStyleName("selected");
                }
                menuItemsLayout.addComponent(b);

            }

            if (views.size() > 0) {

                String f = Page.getCurrent().getUriFragment();
                if ((selectedViewName != null && !foundSelection) || (f == null || f.equals(""))) {
                    navigator.navigateTo(views.get(0).getNavigatorViewName());
                }
                navigator.setErrorView(views.get(0));
            }




        });
    }



    @Override
    protected void init(VaadinRequest request) {

        subject = BasicAuthRequired.getSubject(request).orElse(null);

        Responsive.makeResponsive(this);

        setContent(root);
        root.setWidth("100%");

        root.addMenu(buildMenu());
        addStyleName(ValoTheme.UI_WITH_MENU);

        navigator = new Navigator(this, viewDisplay);

        navigator.addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {
                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {

                for (AdminView adminView : providers.values()) {
                    if (adminView.getNavigatorViewName().equals(event.getViewName())) {
                        adminView.getMenuButton().addStyleName("selected");
                    }
                    adminView.getMenuButton().removeStyleName("selected");
                }

            }
        });
    }

    private CssLayout buildMenu() {

        HorizontalLayout top = new HorizontalLayout();
        top.setWidth("100%");
        top.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        top.addStyleName(ValoTheme.MENU_TITLE);
        menu.addComponent(top);

        Button showMenu = new Button("Menu", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (menu.getStyleName().contains("valo-menu-visible")) {
                    menu.removeStyleName("valo-menu-visible");
                } else {
                    menu.addStyleName("valo-menu-visible");
                }
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
