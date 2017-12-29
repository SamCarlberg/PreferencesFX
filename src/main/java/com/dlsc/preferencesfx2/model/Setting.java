package com.dlsc.preferencesfx2.model;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.preferencesfx2.formsfx.view.controls.DoubleSliderControl;
import com.dlsc.preferencesfx2.formsfx.view.controls.IntegerSliderControl;
import com.dlsc.preferencesfx2.formsfx.view.controls.SimpleComboBoxControl;
import com.dlsc.preferencesfx2.formsfx.view.controls.SimpleControl;
import com.dlsc.preferencesfx2.formsfx.view.controls.SimpleDoubleControl;
import com.dlsc.preferencesfx2.formsfx.view.controls.SimpleIntegerControl;
import com.dlsc.preferencesfx2.formsfx.view.controls.SimpleListViewControl;
import com.dlsc.preferencesfx2.formsfx.view.controls.SimpleTextControl;
import com.dlsc.preferencesfx2.formsfx.view.controls.ToggleControl;
import com.dlsc.preferencesfx2.util.Constants;
import com.dlsc.preferencesfx2.util.StorageHandler;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Setting<F extends Field, P extends Property> {
  private static final Logger LOGGER =
      LogManager.getLogger(Setting.class.getName());

  public static final String MARKED_STYLE_CLASS = "simple-control-marked";
  private String description;
  private F field;
  private P value;
  private boolean marked = false;
  private final EventHandler<MouseEvent> unmarker = event -> unmark();
  private String breadcrumb = "";

  private Setting(String description, F field, P value) {
    this.description = description;
    this.field = field;
    this.value = value;
  }

  public static Setting of(String description, BooleanProperty property) {
    return new Setting<>(
        description,
        Field.ofBooleanType(property)
            .label(description)
            .render(new ToggleControl()),
        property
    );
  }

  public static Setting of(String description, IntegerProperty property) {
    return new Setting<>(
        description,
        Field.ofIntegerType(property)
            .label(description)
            .render(new SimpleIntegerControl()),
        property);
  }

  public static Setting of(String description, DoubleProperty property) {
    return new Setting<>(
        description,
        Field.ofDoubleType(property)
            .label(description)
            .render(new SimpleDoubleControl()),
        property);
  }

  public static Setting of(
      String description, DoubleProperty property, double min, double max, int precision) {
    return new Setting<>(
        description,
        Field.ofDoubleType(property)
            .label(description)
            .render(new DoubleSliderControl(min, max, precision)),
        property);
  }

  public static Setting of(String description, IntegerProperty property, int min, int max) {
    return new Setting<>(
        description,
        Field.ofIntegerType(property)
            .label(description)
            .render(new IntegerSliderControl(min, max)),
        property);
  }

  public static Setting of(String description, StringProperty property) {
    return new Setting<>(
        description,
        Field.ofStringType(property)
            .label(description)
            .render(new SimpleTextControl()),
        property);
  }

  public static <P> Setting of(
      String description, ListProperty<P> items, ObjectProperty<P> selection) {
    return new Setting<>(
        description,
        Field.ofSingleSelectionType(items, selection)
            .label(description)
            .render(new SimpleComboBoxControl<>()),
        selection);
  }

  public static <P> Setting of(
      String description, ObservableList<P> items, ObjectProperty<P> selection) {
    return new Setting<>(
        description,
        Field.ofSingleSelectionType(new SimpleListProperty<>(items), selection)
            .label(description)
            .render(new SimpleComboBoxControl<>()),
        selection);
  }

  /**
   * Creates a combobox with multiselection.
   * At least one element has to be selected at all times.
   */
  public static <P> Setting of(
      String description, ListProperty<P> items, ListProperty<P> selections) {
    return new Setting<>(
        description,
        Field.ofMultiSelectionType(items, selections)
            .label(description)
            .render(new SimpleListViewControl<>()),
        selections);
  }

  /**
   * Creates a combobox with multiselection.
   * At least one element has to be selected at all times.
   */
  public static <P> Setting of(
      String description, ObservableList<P> items, ListProperty<P> selections) {
    return new Setting<>(
        description,
        Field.ofMultiSelectionType(new SimpleListProperty<>(items), selections)
            .label(description)
            .render(new SimpleListViewControl<>()),
        selections);
  }

  /**
   * Creates a setting of a custom defined field.
   *
   * @param description title of the setting
   * @param field       custom Field object from FormsFX
   * @param property    property with relevant value to be bound and saved
   * @return constructed setting
   */
  public static <F extends Field<F>, P extends Property> Setting of(
      String description, F field, P property) {
    return new Setting<>(
        description,
        field.label(description),
        property);
  }

  public void mark() {
    // ensure it's not marked yet - so a control doesn't contain the same styleClass multiple times
    if (!marked) {
      SimpleControl renderer = (SimpleControl) getField().getRenderer();
      Node markNode = renderer.getFieldLabel();
      markNode.getStyleClass().add(MARKED_STYLE_CLASS);
      markNode.setOnMouseExited(unmarker);
      marked = !marked;
    }
  }

  public void unmark() {
    // check if it's marked before removing the style class
    if (marked) {
      SimpleControl renderer = (SimpleControl) getField().getRenderer();
      Node markNode = renderer.getFieldLabel();
      markNode.getStyleClass().remove(MARKED_STYLE_CLASS);
      markNode.removeEventHandler(MouseEvent.MOUSE_EXITED, unmarker);
      marked = !marked;
    }
  }

  public String getDescription() {
    return description;
  }

  public P valueProperty() {
    return value;
  }

  public F getField() {
    return field;
  }

  public void saveSettingValue(StorageHandler storageHandler) {
    storageHandler.saveObject(breadcrumb, value.getValue());
  }

  public void loadSettingValue(StorageHandler storageHandler) {
    if (value instanceof ListProperty) {
      value.setValue(
          storageHandler.loadObservableList(breadcrumb, (ObservableList) value.getValue())
      );
    } else {
      value.setValue(storageHandler.loadObject(breadcrumb, value.getValue()));
    }
  }

  public void addToBreadcrumb(String breadCrumb) {
    this.breadcrumb = breadCrumb + Constants.BREADCRUMB_DELIMITER + description;
  }

  public String getBreadcrumb() {
    return breadcrumb;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Setting<?, ?> setting = (Setting<?, ?>) o;
    return Objects.equals(breadcrumb, setting.breadcrumb);
  }

  @Override
  public int hashCode() {
    return Objects.hash(breadcrumb);
  }

  @Override
  public String toString() {
    return breadcrumb;
  }
}
