package game_interface;

import game_interface.Constants.Color;
import game_interface.Constants.Value;

public abstract class Card {
    private Color color=null;
    private Value value=null;

    public Card(Color color, Value value) {
        this.color = color;
        this.value = value;
    }

    public Color getColor() {
        return color;
    }

    public Value getValue() {
        return value;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setValue(Value value) {
        this.value = value;
    }

}
