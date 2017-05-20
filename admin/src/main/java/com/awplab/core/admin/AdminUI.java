package com.awplab.core.admin;

import com.awplab.core.admin.events.AdminEventTopics;
import com.awplab.core.admin.provider.AdminVaadinProvider;
import com.awplab.core.common.EventAdminHelper;
import com.awplab.core.vaadin.service.BasicAuthRequired;
import com.awplab.core.vaadin.service.VaadinProvider;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.osgi.framework.*;
import org.osgi.service.event.EventHandler;

import javax.security.auth.Subject;
import java.util.*;

//import com.vaadin.shared.ui.label.ContentMode;

/**
 * Created by andyphillips404 on 8/12/16.
 */

@Push
//@PreserveOnRefresh
@Theme("valo")
public class AdminUI extends UI implements EventHandler {


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


    private Subject subject;

    private Map<AdminProvider, Button> buttons = Collections.synchronizedMap(new HashMap<>());

    private Optional<AdminVaadinProvider> getVaadinProvider() {
        String filterString = "(" + Constants.OBJECTCLASS + "=" + AdminVaadinProvider.class.getName() + ")";
        try {
            BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
            Optional<ServiceReference<VaadinProvider>> optional = context.getServiceReferences(VaadinProvider.class, filterString).stream().findFirst();
            if (optional.isPresent()) {
                AdminVaadinProvider adminVaadinProvider = (AdminVaadinProvider)context.getService(optional.get());
                return Optional.of(adminVaadinProvider);
            }
            return Optional.empty();
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    private Set<AdminProvider> getProviders() {
        Optional<AdminVaadinProvider> adminVaadinProvider = getVaadinProvider();
        if (adminVaadinProvider.isPresent()) {
            return adminVaadinProvider.get().getProviders();
        }
        return Collections.emptySet();
    }


    @Override
    public void handleEvent(org.osgi.service.event.Event event) {
        updateMenuAndNavigator();
    }

    public Subject getSubject() {
        return subject;
    }

    private void updateMenuAndNavigator() {

        if (navigator == null) return;


        String selectedViewName = navigator.getState(); //(navigator.getCurrentView() != null && !(navigator.getCurrentView() instanceof NoAdminErrorView)) ? ((AdminViewProvider) navigator.getCurrentView()).getName() : null;
        //boolean foundSelection = false;

        ArrayList<AdminProvider> viewProviders = new ArrayList<>(getProviders());
        List<String> categories = new ArrayList<>();
        getVaadinProvider().ifPresent(adminVaadinProvider -> categories.addAll(Arrays.asList(adminVaadinProvider.getCategories())));

        viewProviders.sort((o1, o2) -> {

            int o1CatPos = o1.getCategory().isPresent() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            int o2CatPos = o2.getCategory().isPresent() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            if (categories.size() > 0) {
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
        buttons.clear();

        Label label;
        String lastCategory = null;
        for (AdminProvider adminProvider : viewProviders) {
            if (adminProvider.getCategory().isPresent() &&
                    (lastCategory == null || !adminProvider.getCategory().get().equals(lastCategory))) {

                label = new Label(adminProvider.getCategory().get(), ContentMode.HTML);
                label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
                label.addStyleName(ValoTheme.LABEL_H4);
                label.setSizeUndefined();
                menuItemsLayout.addComponent(label);
                lastCategory = adminProvider.getCategory().get();
            }

            Button b = new Button();
            b.addClickListener((Button.ClickListener) event -> navigator.navigateTo(adminProvider.getName()));

            buttons.put(adminProvider, b);
            updateMenuButton(adminProvider);

            b.setPrimaryStyleName(ValoTheme.MENU_ITEM);
            if (selectedViewName != null && selectedViewName.equals(adminProvider.getName())) {
                b.addStyleName("selected");
            }
            menuItemsLayout.addComponent(b);

        }

    }

    public void updateMenuButton(AdminProvider adminProvider) {
        if (buttons.containsKey(adminProvider)) {
            if (adminProvider.getMenuBadge().isPresent()) {
                buttons.get(adminProvider).setCaption(adminProvider.getMenuTitle() + "<span class=\"valo-menu-badge\">" + adminProvider.getMenuBadge().get() + "</span>");
            } else {
                buttons.get(adminProvider).setCaption(adminProvider.getMenuTitle());
            }
            if (adminProvider.getMenuIcon().isPresent()) buttons.get(adminProvider).setIcon(adminProvider.getMenuIcon().get());
        }
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

                for (AdminProvider adminProvider : getProviders()) {
                    if (adminProvider == null) continue;

                    if (adminProvider.getName().equals(event.getViewName())) {
                        buttons.get(adminProvider).addStyleName("selected");

                    }
                    else {
                        buttons.get(adminProvider).removeStyleName("selected");
                    }
                }

            }
        });

        navigator.setErrorView(new NoAdminErrorView());

        for (AdminProvider adminProvider : getProviders()) {
            navigator.addProvider(adminProvider);
        }

        root.addMenu(buildMenu());

        EventAdminHelper.registerForEvent(this, AdminEventTopics.ANY);

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

        String titleString = getVaadinProvider().map(AdminVaadinProvider::getTitle).orElse("Admin Title");

        Label title = new Label("<h3>" + titleString + "</h3>",
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
