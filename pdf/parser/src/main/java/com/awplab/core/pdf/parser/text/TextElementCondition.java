package com.awplab.core.pdf.parser.text;

import java.awt.geom.Rectangle2D;

/**
 * Created by andyphillips404 on 1/16/14.
 */
public abstract class TextElementCondition {

    public abstract boolean conditionMet(TextElement textElement);

    public static TextElementCondition all() {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return true;
            }
        };
    }

    public static TextElementCondition startY(final float y, final float tolerance) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (textElement.getStartY() >= (y - tolerance) && textElement.getStartY() <= (y + tolerance));
            }
        };
    }

    public static TextElementCondition startX(final float x, final float tolerance) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (textElement.getStartX() >= (x - tolerance) && textElement.getStartX() <= (x + tolerance));
            }
        };
    }

    public static TextElementCondition endX(final float x, final float tolerance) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (textElement.getEndX() >= (x - tolerance) && textElement.getEndX() <= (x + tolerance));
            }
        };
    }

    public static TextElementCondition after(final float startY, final int inPageNumber) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return ((textElement.getPageNumber() == inPageNumber && textElement.getStartY() > startY) || textElement.getPageNumber() > inPageNumber);
            }
        };
    }

    public static TextElementCondition fontNameContains(final String caseInsensitiveContains) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (textElement.getFont().getFontDescriptor().getFontName().toUpperCase().contains(caseInsensitiveContains.toUpperCase()));
            }
        };
    }

    public static TextElementCondition after(final float startY) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (textElement.getStartY() > startY);
            }
        };
    }

    public static TextElementCondition before(final float bottomY) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (textElement.getStartY() <= bottomY);
            }
        };
    }

    public static TextElementCondition before(final float bottomY, final int endPageNumber) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (textElement.getStartY() <= bottomY && textElement.getPageNumber() <= endPageNumber);
            }
        };
    }

    public static TextElementCondition withinRectangle(final Rectangle2D.Float rectangle) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (rectangle.contains(textElement.getBounds()));
            }
        };
    }

    public static TextElementCondition withinRectangleInPage(int pageNumber, final Rectangle2D.Float rectangle) {
        return TextElementCondition.and(TextElementCondition.withinRectangle(rectangle), TextElementCondition.inPageNumber(pageNumber));
    }

    public static TextElementCondition textMatches(final String regex) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (textElement.getText().matches(regex));
            }
        };
    }


    public static TextElementCondition inPageNumber(final int pageNumber) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return textElement.getPageNumber() == pageNumber;
            }
        };
    }

    public static TextElementCondition greaterThanPageNumber(final int pageNumber) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return textElement.getPageNumber() > pageNumber;
            }
        };
    }

    public static TextElementCondition greaterThanEqualPageNumber(final int pageNumber) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return textElement.getPageNumber() >= pageNumber;
            }
        };
    }

    public static TextElementCondition centerX(final float centerX, final float tolerance) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (textElement.getCenterX() >= (centerX - tolerance) && (textElement.getCenterX() <= (centerX + tolerance)));
            }
        };
    }
    public static TextElementCondition startAfterX(final float x) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (textElement.getStartX() > x);
            }
        };
    }

    public static TextElementCondition endY(final float endY, final float tolerance) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return ((textElement.getEndY() >= (endY - tolerance)) && (textElement.getEndY() <= (endY + tolerance)));
            }
        };
    }

    public static TextElementCondition afterElementSameLine(final TextElement element, final float toleranceY) {
        return TextElementCondition.and(TextElementCondition.inPageNumber(element.getPageNumber()), TextElementCondition.startAfterX(element.getEndX()), TextElementCondition.endY(element.getEndY(), toleranceY));
    }

    public static TextElementCondition beforeElementsOnTheirPage(final TextElements footerElements) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                for (TextElement footerElement : footerElements) {
                    if (footerElement.getPageNumber() == textElement.getPageNumber() && textElement.getEndY() > footerElement.getStartY()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static TextElementCondition afterElementsOnTheirPage(final TextElements headerElements) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                for (TextElement headerElement : headerElements) {
                    if (headerElement.getPageNumber() == textElement.getPageNumber() && textElement.getStartY() < headerElement.getEndY()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static TextElementCondition rectangleHeight(final float height, final float tolerance) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return textElement.getBounds().getHeight() <= (height + tolerance) && textElement.getBounds().getHeight() >= (height - tolerance);
            }
        };
    }

    public static TextElementCondition allElementsExcept(final TextElement... ignoreElements) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                for (TextElement ignoreElement : ignoreElements) {
                    if (textElement == ignoreElement) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static TextElementCondition falseIfLimitMet(final int limit, final TextElementCondition condition) {

        return new TextElementCondition() {
            private int totalMetCount = 0;

            @Override
            public boolean conditionMet(TextElement textElement) {
                if (totalMetCount >= limit) return false;

                boolean ret = condition.conditionMet(textElement);
                if (ret == true) totalMetCount++;
                return ret;
            }
        };
    }

    

    public static TextElementCondition not(final TextElementCondition condition) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return !condition.conditionMet(textElement);
            }
        };
    }

    public static TextElementCondition and(final TextElementCondition... conditions) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                for (TextElementCondition condition : conditions) {
                    if (!condition.conditionMet(textElement)) return false;
                }
                return true;
            }
        };
    }

    public static TextElementCondition or(final TextElementCondition... conditions) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                for (TextElementCondition condition : conditions) {
                    if (condition.conditionMet(textElement)) return true;
                }
                return false;
            }
        };
    }

    public static TextElementCondition equalsElements(final TextElement... elements) {

        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                for (TextElement testElement : elements) {
                    if (testElement == null) return false;

                    if (testElement.equals(textElement)) return true;
                }

                return false;
            }
        };
    }

    public static TextElementCondition betweenX(final float startX, final float endX) {
        return new TextElementCondition() {
            @Override
            public boolean conditionMet(TextElement textElement) {
                return (textElement.getStartX() >= startX && textElement.getEndX() <= endX);
            }
        };
    }
}
