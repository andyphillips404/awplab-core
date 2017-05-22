package com.awplab.core.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.Resource;
import com.vaadin.ui.UI;

import javax.security.auth.Subject;
import java.util.Optional;

/**
 * Created by andyphillips404 on 8/31/16.
 */
public interface AdminProvider extends ViewProvider {

    @Override
    default String getViewName(String viewAndParameters) {
        if (null == viewAndParameters) {
            return null;
        }
        if (viewAndParameters.equals(getName())
                || viewAndParameters.startsWith(getName() + "/")) {
            return getName();
        }
        return null;
    }

    String getName();

    @Override
    default View getView(String viewName) {
        if (getName().equals(viewName)) {
            View currentView = createView(((AdminUI) UI.getCurrent()).getSubject());
            return currentView;
        }
        return null;
    }


    View createView(Subject subject);

    String getMenuTitle();

    default Optional<Integer> getPositionInCategory() {
        return Optional.empty();
    }

    default Optional<String> getCategory() {
        return Optional.empty();
    }


    default Optional<Resource> getMenuIcon() {
        return Optional.empty();
    }

    default Optional<String> getMenuBadge() {
        return Optional.empty();
    }

    static String jacksonHtml(ObjectMapper objectMapper, Object object, boolean stripOpeningBrackets, int indent) throws JsonProcessingException {
        String ret = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        if (stripOpeningBrackets) ret = ret.replaceAll("(?s)^(\\{|\\[)(\n )(.*?)(\n)(\\}|\\])$", "$3").trim();
        String indentStr = "";
        if (indent > 0) {
            indentStr = new String(new char[indent]).replace("\0", "&nbsp;");
        }
        return  indentStr + ret.trim().replaceAll("[,]?\n" + (stripOpeningBrackets ? "  " : ""), "<br>"+indentStr).replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").replaceAll(" ", "&nbsp;");


    }
}
